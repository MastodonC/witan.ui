(ns ^:figwheel-always witan.ui.util)

(defn get-string
  [cursor keyword]
  (-> cursor :strings keyword))
