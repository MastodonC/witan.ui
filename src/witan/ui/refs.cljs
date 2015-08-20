(ns ^:figwheel-always witan.ui.refs
    (:require [om.core :as om :include-macros true]
              [witan.ui.data :refer [app-state]]))

(defn projections-meta
  []
  (-> app-state
      om/root-cursor
      :projections-meta
      om/ref-cursor))
