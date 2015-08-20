(ns ^:figwheel-always witan.ui.controllers.input
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [witan.schema.core :refer [Projection]]
              [witan.ui.data :as d]))

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
        is-toggled   (contains? (-> cursor :projections-meta :expanded) [db-id id])
        fn           (if is-toggled disj conj)
        new-state    (om/transact! cursor [:projections-meta :expanded] #(fn % [db-id id]))
        all-expanded (-> @new-state :projections-meta :expanded)
        toggled-on   (mapv #(vector :db/id (first %)) all-expanded)]
    (om/update! cursor :projections (d/fetch-projections {:expand toggled-on}))))
