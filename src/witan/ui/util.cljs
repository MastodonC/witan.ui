(ns ^:figwheel-always witan.ui.util
    (:require [witan.ui.data :refer [app-state]]))

(defn get-string
  [keyword]
  (-> @app-state :strings keyword))

(defn prependtial [f & args1]
  (fn [& args2]
    (apply f (concat args2 args1))))
