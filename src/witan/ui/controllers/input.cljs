(ns ^:figwheel-always witan.ui.controllers.input
  (:require [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [witan.schema.core :refer [Forecast]]
            [witan.ui.data :as d]))

(defn fetch-visible-forecasts
  [state]
  (let [all-expanded (-> state :forecasts-meta :expanded)
        filter (-> state :forecasts-meta :filter)
        toggled-on (mapv #(vector :db/id (first %)) all-expanded)]
    (d/fetch-forecasts {:expand toggled-on
                          :filter filter})))

(defmulti handler
  (fn [[event args] cursor] event))

(defmethod handler
  :event/select-forecast
  [[event args] cursor]
  (s/validate Forecast args)
  (om/update! cursor [:forecasts-meta :selected] (vector (:db/id args) (:id args))))

(defmethod handler
  :event/toggle-tree-view
  [[event args] cursor]
  (s/validate Forecast args)
  (let [db-id        (-> args :db/id)
        id           (-> args :id)
        toggled?     (contains? (-> cursor :forecasts-meta :expanded) [db-id id])
        fn           (if toggled? disj conj)
        new-state    (om/transact! cursor [:forecasts-meta :expanded] #(fn % [db-id id]))]
    (om/update! cursor :forecasts (fetch-visible-forecasts @new-state))))

(defmethod handler
  :event/filter-forecasts
  [[event args] cursor]
  (s/validate s/Str args)
  (let [new-filter (not-empty args)
        new-state (om/update! cursor [:forecasts-meta :filter] new-filter)]
    (om/update! cursor :forecasts (fetch-visible-forecasts @new-state))))
