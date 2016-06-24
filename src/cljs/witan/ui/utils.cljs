(ns witan.ui.utils
  (:require [cljsjs.moment]))

(defn iso-time-as-moment
  [time]
  (.calendar (js/moment. time "YYYYMMDD HHmmss")))
