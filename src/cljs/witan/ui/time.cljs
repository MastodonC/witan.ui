(ns witan.ui.time
  (:require [cljsjs.moment]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]))

(defn iso-time-as-moment
  [time]
  (let [hoffset (- (/ (.getTimezoneOffset (js/Date.)) 60))]
    (.calendar (.add (js/moment. (str time) "YYYYMMDD HHmmss") hoffset "hours"))))

(defn jstime->str
  ([]
   (jstime->str (t/now)))
  ([time]
   (tf/unparse (tf/formatters :basic-date-time) time)))

(defn sleep [msec]
  (let [deadline (+ msec (.getTime (js/Date.)))]
    (while (> deadline (.getTime (js/Date.))))))
