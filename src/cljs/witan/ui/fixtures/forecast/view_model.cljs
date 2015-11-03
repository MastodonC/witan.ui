(ns ^:figwheel-always witan.ui.fixtures.forecast.view-model
    (:require [om.core :as om :include-macros true]
              [witan.ui.services.data :as data]
              [venue.core :as venue :include-macros true]
              [witan.ui.util :as util])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn reset-cursor!
  [cursor]
  (om/update! cursor :forecast             nil)
  (om/update! cursor :edited-forecast      nil)
  (om/update! cursor :model                nil)
  (om/update! cursor :browsing-input       nil)
  (om/update! cursor :upload-file          nil)
  (om/update! cursor :upload-filename      "")
  (om/update! cursor :uploading?           false)
  (om/update! cursor :upload-error?        false)
  (om/update! cursor :upload-success?      false)
  (om/update! cursor :last-upload-filename "")
  (om/update! cursor :data-items           nil)
  (om/update! cursor :selected-data-item   nil)
  (om/update! cursor :creating?            false))

(defn on-initialise
  [owner cursor])

(defn on-activate
  [owner {:keys [id action version]} cursor]
  (when (data/logged-in?)
    (when-not (and (= (:id cursor) id) (= (:version cursor) version))
      (reset-cursor! cursor)
      (venue/request! {:owner   owner
                       :service :service/data
                       :request :fetch-forecast
                       :args    {:id id :version version}
                       :context cursor}))

    (om/update! cursor :id      id)
    (om/update! cursor :action  (or (not-empty action) "input"))
    (om/update! cursor :version version)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :revert-forecast
  [owner _ _ cursor]
  (om/update! cursor :edited-forecast nil))

(defmethod event-handler
  :refresh-forecast
  [owner _ _ cursor]
  (.reload (.-location js/window)))

(defmethod event-handler
  :toggle-browse-input
  [owner _ input cursor]
  (when-not (:uploading? @cursor)
    (let [same?  (= (:browsing-input @cursor) input)
          result (when-not same? input)]
      (when-not same?
        (om/update! cursor :upload-file          nil)
        (om/update! cursor :upload-filename      "")
        (om/update! cursor :upload-error?        false)
        (om/update! cursor :upload-success?      false)
        (om/update! cursor :last-upload-filename "")
        (om/update! cursor :selected-data-item   nil))
      (om/update! cursor :browsing-input result)
      (when result
        (venue/request! {:owner   owner
                         :service :service/data
                         :request :fetch-data-items
                         :args    (:category result)
                         :context cursor})))))

(defmethod event-handler
  :pending-upload
  [owner _ file cursor]
  (let [filename (util/sanitize-filename (.-name file))]
    (log/debug "File selected for upload: " filename)
    (om/update! cursor :upload-file file)
    (om/update! cursor :upload-filename filename)))

(defmethod event-handler
  :pending-upload-type
  [owner _ type cursor]
  (om/update! cursor :upload-type (keyword type)))

(defmethod event-handler
  :upload-file
  [owner _ data-item-name cursor]
  (log/info "Starting upload...")
  (om/update! cursor :uploading? true)
  (om/update! cursor :last-upload-filename data-item-name)
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :upload-data
                   :args    {:category (-> @cursor :browsing-input :category)
                             :file     (:upload-file @cursor)
                             :filename (:upload-filename @cursor)
                             :name     data-item-name
                             :id       (:id @cursor)
                             :version  (:version @cursor)}
                   :context cursor
                   :timeout? false}))

(defmethod event-handler
  :error-reset
  [owner _ _ cursor]
  (om/update! cursor :upload-error? false))

(defmethod event-handler
  :filter-data-items
  [owner _ filter cursor]
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :filter-data-items
                   :args    {:category (-> @cursor :browsing-input :category)
                             :filter   filter}
                   :context cursor}))

(defmethod event-handler
  :select-data-item
  [owner _ item cursor]
  (om/update! cursor :selected-data-item item))

(defmethod event-handler
  :select-input
  [owner _ _ cursor]
  (let [category        (-> @cursor :browsing-input :category)
        edited-forecast (or (:edited-forecast @cursor) (:forecast @cursor))
        input-entry     (hash-map :category category :selected (into {} (util/map-remove-ns (assoc (:selected-data-item @cursor) :edited? true))))
        inputs          (conj (vec (:inputs edited-forecast)) input-entry)
        with-input      (assoc edited-forecast :forecast/inputs inputs)]
    (om/update! cursor :edited-forecast with-input)
    (event-handler owner :toggle-browse-input nil cursor)))

(defmethod event-handler
  :create-forecast-version
  [owner _ _ cursor]
  (om/update! cursor :creating? true)
  (venue/request! {:owner owner
                   :service :service/data
                   :request :add-forecast-version
                   :args (:edited-forecast @cursor)
                   :context cursor}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:fetch-forecast :success]
  [owner _ forecast cursor]
  (om/update! cursor :forecast forecast)
  ;; get the model
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :fetch-model
                   :args    {:id (:forecast/model-id forecast)}
                   :context cursor}))

(defmethod response-handler
  [:fetch-forecast :failure]
  [owner _ error cursor]
  (let [response (condp = error
                   404 "A forecast with this ID or version could not be found."
                   "An unknown error occurred.")]
    (om/update! cursor :error? response)))

(defmethod response-handler
  [:fetch-model :success]
  [owner _ model cursor]
  (om/update! cursor :model model))

(defmethod response-handler
  [:upload-data :success]
  [owner _ data-items cursor]
  (om/update! cursor :uploading?      false)
  (om/update! cursor :upload-file     nil)
  (om/update! cursor :upload-filename "")
  (om/update! cursor :upload-success? true)
  (om/update! cursor :data-items data-items))

(defmethod response-handler
  [:upload-data :failure]
  [owner _ response cursor]
  (om/update! cursor :uploading?    false)
  (om/update! cursor :upload-error? true))

(defmethod response-handler
  [:fetch-data-items :success]
  [owner _ data-items cursor]
  (om/update! cursor :data-items data-items))

(defmethod response-handler
  [:fetch-data-items :failure]
  [owner _ _ cursor]
  (om/update! cursor :data-items nil))

(defmethod response-handler
  [:filter-data-items :success]
  [owner _ data-items cursor]
  (om/update! cursor :data-items data-items))

(defmethod response-handler
  [:filter-data-items :success]
  [owner _ data-items cursor]
  (om/update! cursor :data-items data-items))

(defmethod response-handler
  [:add-forecast-version :success]
  [owner _ {:keys [forecast/forecast-id forecast/version]} cursor]
  (venue/navigate! :views/forecast {:id forecast-id :version version :action "input"}))

(defmethod response-handler
  [:add-forecast-version :failure]
  [owner _ _ cursor]
  (venue/navigate! :views/dashboard))
