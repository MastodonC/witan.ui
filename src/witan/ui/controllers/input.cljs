(ns ^:figwheel-always witan.ui.controllers.input
    (:require [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [witan.schema.core :refer [Projection]]))

(defmulti handler
  (fn [[event args] cursor] event))

(defmethod handler
  :event/select-projection
  [[event args] cursor]
  (s/validate Projection args)
  ;; we don't require backend for selection, so just update the cursor
  (om/update! cursor :selected-projection args))
