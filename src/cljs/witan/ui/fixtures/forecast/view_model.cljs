(ns ^:figwheel-always witan.ui.fixtures.forecast.view-model
    (:require [om.core :as om :include-macros true]
              [witan.ui.services.data :as data]
              [venue.core :as venue :include-macros true]
              [cljs.core.async :refer [timeout <! chan put!]]
              [witan.ui.util :as util])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]
                     [cljs.core.async.macros :refer [go alt!]]))

(defonce page-exit-ch (chan))

(defn reset-cursor!
  [cursor]
  (om/update! cursor :forecast             nil)
  (om/update! cursor :edited-forecast      nil)
  (om/update! cursor :missing-required     #{})
  (om/update! cursor :model                nil)
  (om/update! cursor :browsing-input       nil)
  (om/update! cursor :upload-file          nil)
  (om/update! cursor :upload-filename      "")
  (om/update! cursor :uploading?           false)
  (om/update! cursor :upload-error?        false)
  (om/update! cursor :upload-success?      false)
  (om/update! cursor :upload-feedback      "")
  (om/update! cursor :last-upload-filename "")
  (om/update! cursor :data-items           nil)
  (om/update! cursor :selected-data-item   nil)
  (om/update! cursor :creating?            false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

(defn on-deactivate
  [owner cursor]
  (log/debug "Deactivating forecast VM")
  (om/update! cursor :id      nil)
  (om/update! cursor :version nil) ;; reset these
  (put! page-exit-ch true))

(wm/create-standard-view-model! {:on-activate on-activate
                                 :on-deactivate on-deactivate})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn update-required-inputs!
  "Updates `:missing-required` based on forecast and model input difference"
  [cursor]
  (let [categories (some->> @cursor
                            :model
                            :model/input-data
                            (remove #(contains? % :default))
                            (map :category)
                            set)]
    (if (not-empty categories)
      (let [forecast (or (:edited-forecast @cursor) (:forecast @cursor))
            f-categories (some->> forecast
                                  :forecast/inputs
                                  (filter :selected)
                                  (map :category)
                                  set)]
        (om/update! cursor :missing-required (clojure.set/difference categories f-categories)))
      (om/update! cursor :missing-required #{}))))

(defn sort-data-items
  [data-items]
  (sort-by #(str (:data/name %) (:data/version %)) data-items))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :download-file
  [owner _ url cursor]
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :download-file
                   :args    url
                   :context cursor}))

(defmethod event-handler
  :revert-forecast
  [owner _ _ cursor]
  (om/update! cursor :edited-forecast nil))

(defmethod event-handler
  :download-output
  [owner _ args cursor]
  (log/info "Downloading an output:" args)
  (venue/request! {:owner   owner
                   :service :service/analytics
                   :request :track-output-download
                   :args    args}))

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
  [owner _ {:keys [name public?]} cursor]
  (log/info "Starting upload...")
  (om/update! cursor :uploading? true)
  (om/update! cursor :last-upload-filename name)
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :upload-data
                   :args    {:category (-> @cursor :browsing-input :category)
                             :file     (:upload-file @cursor)
                             :filename (:upload-filename @cursor)
                             :name     name
                             :public?  public?}
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

(defn is-new-input?
  [new-data inputs]
  (let [category    (:data/category new-data)
        data-id     (:data/data-id new-data)
        existing-id (some #(if (= (:category %) category) (or
                                                           (-> % :selected :data-id)
                                                           (-> % :default :data-id))) inputs)
        result      (not= data-id existing-id)]
    result))

(defmethod event-handler
  :select-input
  [owner _ _ cursor]
  (let [category        (-> @cursor :browsing-input :category)
        model-inputs    (-> @cursor :model :model/input-data)
        new-data        (:selected-data-item @cursor)
        forecast        (:forecast @cursor)
        other-forecast  (:edited-forecast @cursor)
        edited?         (is-new-input? new-data (util/squash-maps model-inputs (:forecast/inputs forecast) :category))
        edited-forecast (or other-forecast forecast)
        input-entry     (hash-map :category category :selected (util/map-remove-ns (assoc new-data :edited? edited?)))
        inputs          (util/squash-maps model-inputs (:forecast/inputs edited-forecast) :category)
        inputs-ex       (util/squash-maps inputs [input-entry] :category)
        with-input      (assoc edited-forecast :forecast/inputs inputs-ex)]
    (if (some->> inputs-ex
                 (map second)
                 (map val)
                 (some :edited?))
      (om/update! cursor :edited-forecast with-input)
      (om/update! cursor :edited-forecast nil)))
  (update-required-inputs! cursor)
  (event-handler owner :toggle-browse-input nil cursor))

(defmethod event-handler
  :create-forecast-version
  [owner _ _ cursor]
  (om/update! cursor :creating? true)
  (venue/request! {:owner owner
                   :service :service/data
                   :request :add-forecast-version
                   :args (:edited-forecast @cursor)
                   :context cursor
                   :timeout 20000}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:fetch-forecast :success]
  [owner _ forecast cursor]
  (om/update! cursor :forecast forecast)
  (update-required-inputs! cursor)
  ;; get the model
  (venue/request! {:owner   owner
                   :service :service/data
                   :request :fetch-model
                   :args    {:id (:forecast/model-id forecast)}
                   :context cursor})
  ;; setup auto-refresh
  (when (:forecast/in-progress? forecast)
    (go
      (alt!
        (timeout 10000) (venue/request! {:owner   owner
                                         :service :service/data
                                         :request :fetch-forecast
                                         :args    {:id (:forecast/forecast-id forecast) :version (:forecast/version forecast)}
                                         :context cursor})
        page-exit-ch (log/warn "Auto-refresh was cancelled by page exit")))))

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
  (om/update! cursor :model model)
  (update-required-inputs! cursor))

(defmethod response-handler
  [:upload-data :success]
  [owner _ {:keys [data-items new-data-item]} cursor]
  (om/update! cursor :uploading?      false)
  (om/update! cursor :upload-file     nil)
  (om/update! cursor :upload-filename "")
  (om/update! cursor :upload-success? true)
  (om/update! cursor :data-items (sort-data-items data-items))
  (om/update! cursor :selected-data-item new-data-item))

(defmethod response-handler
  [:upload-data :failure]
  [owner _ response cursor]
  (om/update! cursor :uploading?    false)
  (om/update! cursor :upload-error? true)
  (om/update! cursor :upload-feedback (:error response)))

(defmethod response-handler
  [:fetch-data-items :success]
  [owner _ data-items cursor]
  (om/update! cursor :data-items (sort-data-items
                                  (if (-> @cursor :forecast :forecast/public?) (filter :data/public? data-items) data-items))))

(defmethod response-handler
  [:fetch-data-items :failure]
  [owner _ _ cursor]
  (om/update! cursor :data-items nil))

(defmethod response-handler
  [:filter-data-items :success]
  [owner _ data-items cursor]
  (om/update! cursor :data-items (sort-data-items data-items)))

(defmethod response-handler
  [:add-forecast-version :success]
  [owner _ {:keys [forecast/forecast-id forecast/version]} cursor]
  (venue/navigate! :views/forecast {:id forecast-id :version version :action "model"}))

(defmethod response-handler
  [:add-forecast-version :failure]
  [owner _ _ cursor]
  (venue/navigate! :views/dashboard))

(defmethod response-handler
  [:download-file :success]
  [owner _ location cursor]
  (set! (.-location js/window) location))

(defmethod response-handler
  [:download-file :failure]
  [owner _ _ cursor]
  (js/alert "Download failed."))

(defmethod response-handler
  [:track-output-download :success]
  [owner _ _ cursor])
