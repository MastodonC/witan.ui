(ns ^:figwheel-always witan.ui.fixtures.dashboard.view-model
    (:require [cljs.core.async :refer [<! chan]]
              [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [witan.schema.core :refer [Forecast]]
              [witan.ui.util :as util]
              [witan.ui.services.data :as data]
              [venue.core :as venue :include-macros true])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))


(defn update-forecasts!
  ([owner cursor]
   (update-forecasts! owner cursor {}))
  ([owner cursor {:keys [expanded filter]}]
   (let [toggled-on (mapv #(vector :db/id (first %)) expanded)]
     (venue/request! {:owner owner
                      :service :service/data
                      :request :filter-forecasts
                      :args {:expand toggled-on
                             :filter filter}
                      :context cursor}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-initialise
  [owner cursor])

(defn on-activate
  [owner args cursor]
  (when (data/logged-in?)
    (om/update! cursor :refreshing? true)
    (venue/request! {:owner owner
                     :service :service/data
                     :request :fetch-forecasts
                     :context cursor})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn api-failure!
  [msg]
  (log/severe "API failure:" msg))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :event/filter-forecasts
  [owner _ filter-text cursor]
  (let [new-filter (not-empty filter-text)]
    (om/update! cursor :filter new-filter)
    (update-forecasts! owner cursor {:filter new-filter
                               :expanded (:expanded @cursor)})))

(defmethod event-handler
  :event/toggle-tree-view
  [owner _ forecast cursor]
  (let [db-id        (:db/id forecast)
        id           (:forecast/version-id forecast)
        expanded     (:expanded @cursor)
        toggled?     (contains? expanded [db-id id])
        dfn          (if toggled? disj conj)
        new-expanded (dfn expanded [db-id id])]
    (om/update! cursor :expanded new-expanded)
    (update-forecasts! owner cursor {:filter (:filter @cursor)
                               :expanded new-expanded})))

(defmethod event-handler
  :event/select-forecast
  [owner _ forecast cursor]
  (om/update! cursor :selected (vector (:db/id forecast) (:forecast/version-id forecast))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:fetch-forecasts :success]
  [owner _ resp cursor]
  (update-forecasts! owner cursor {:filter (:filter @cursor)
                                   :expanded (:expanded @cursor)}))

(defmethod response-handler
  [:filter-forecasts :success]
  [owner _ {:keys [forecasts has-ancestors]} cursor]
  (om/update! cursor :forecasts forecasts)
  (om/update! cursor :has-ancestors has-ancestors)
  (om/update! cursor :refreshing? false))

(defmethod response-handler
  [:fetch-forecasts :failure]
  [owner _ _ cursor]
  (om/update! cursor :refreshing? true))

(defmethod response-handler
  :default
  [owner _ response cursor])
