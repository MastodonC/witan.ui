(ns ^:figwheel-always witan.ui.controllers.input
  (:require [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [witan.schema.core :refer [Projection]]
            [witan.ui.data :as d]))

(defn fetch-visible-projections
  [state]
  (let [all-expanded (-> state :projections-meta :expanded)
        filter (-> state :projections-meta :filter)
        toggled-on (mapv #(vector :db/id (first %)) all-expanded)]
    (d/fetch-projections {:expand toggled-on
                          :filter filter})))

(defmulti handler
  (fn [[event args] cursor] event))

(defmethod handler
  :event/select-projection
  [[event args] cursor]
  (s/validate Projection args)
  (om/update! cursor [:projections-meta :selected] (vector (:db/id args) (:id args))))

(defmethod handler
  :event/toggle-tree-view
  [[event args] cursor]
  (s/validate Projection args)
  (let [db-id        (-> args :db/id)
        id           (-> args :id)
        toggled?     (contains? (-> cursor :projections-meta :expanded) [db-id id])
        fn           (if toggled? disj conj)
        new-state    (om/transact! cursor [:projections-meta :expanded] #(fn % [db-id id]))]
    (om/update! cursor :projections (fetch-visible-projections @new-state))))

(defmethod handler
  :event/filter-projections
  [[event args] cursor]
  (s/validate s/Str args)
  (let [new-filter (not-empty args)
        new-state (om/update! cursor [:projections-meta :filter] new-filter)]
    (om/update! cursor :projections (fetch-visible-projections @new-state))))
