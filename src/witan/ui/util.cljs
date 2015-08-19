(ns ^:figwheel-always witan.ui.util
    (:require [witan.ui.data :refer [app-state]]))

(defn get-string
  [keyword]
  (-> @app-state :strings keyword))
