(ns witan.ui.style.util
  (:require [garden.def :as g]))

(g/defcssfn url)

(defn transition
  [& args]
  (->> args
       (partition 2 )
       (map (fn [[attr time]] (str (name attr) " " time)))
       (clojure.string/join ", ")))
