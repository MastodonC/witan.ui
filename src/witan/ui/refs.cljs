(ns ^:figwheel-always witan.ui.refs
    (:require [om.core :as om :include-macros true]
              [witan.ui.data :refer [app-state]]))

(defn selected-projection
  []
  (om/ref-cursor (:selected-projection (om/root-cursor app-state))))
