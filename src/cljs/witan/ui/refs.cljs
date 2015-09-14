(ns ^:figwheel-always witan.ui.refs
  (:require [om.core :as om :include-macros true]
            [witan.ui.data :refer [app-state]]))

(defn forecasts-meta
  []
  (-> app-state
      om/root-cursor
      :view-state
      :forecasts
      om/ref-cursor))
