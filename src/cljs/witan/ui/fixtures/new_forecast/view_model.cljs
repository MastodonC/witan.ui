(ns ^:figwheel-always witan.ui.fixtures.new-forecast.view-model
    (:require [om.core :as om :include-macros true]
              [witan.ui.services.data :as data]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn select-model!
  [cursor model]
  (om/update! cursor :selected-model model))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-initialise
  [owner cursor])

(defn on-activate
  [owner args cursor]
  (om/update! cursor :working? false)
  (when (data/logged-in?)
    (venue/request! {:owner owner
                     :service :service/data
                     :request :fetch-models
                     :context cursor})))

;;;

(defmethod event-handler
  :event/create-forecast
  [owner _ args cursor]
  (when-not (:working? @cursor)
    (om/update! cursor :working? true)
    (venue/request! {:owner owner
                     :service :service/data
                     :request :add-forecast
                     :args args
                     :context cursor})))

(defmethod event-handler
  :event/select-model
  [owner _ {:keys [name version]} cursor]
  (let [selected-model (some #(if (= (:model/name %) name) %) (:models @cursor))]
    (select-model! cursor selected-model)))

;;;

(defmethod response-handler
  [:fetch-models :success]
  [owner _ models cursor]
  (om/update! cursor :models models)
  (let [model (first models)]
    (select-model! cursor model)))

(defmethod response-handler
  [:fetch-models :failure]
  [owner _ models cursor]
  (om/update! cursor :error :fetch-models))

(defmethod response-handler
  [:add-forecast :success]
  [owner _ model-id cursor]
  (om/update! cursor :working? false)
  (om/update! cursor :success? true)
  (venue/navigate! :views/forecast {:id model-id :action "input"}))

(defmethod response-handler
  [:add-forecast :failure]
  [owner _ models cursor]
  (om/update! cursor :working? false)
  (om/update! cursor :error :add-forecast))
