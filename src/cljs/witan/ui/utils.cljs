(ns witan.ui.utils
  (:require [cljsjs.moment]
            [witan.ui.data :as data]
            [cljs.reader :as reader]
            [cljs-time.core :as t]
            [cljs-time.format :as tf])
  (:require-macros [cljs-log.core :as log]))

(defn iso-time-as-moment
  [time]
  (.calendar (js/moment. (str time) "YYYYMMDD HHmmss")))

(defn jstime->str
  [time]
  (tf/unparse (tf/formatters :basic-date-time) time))

(defn query-param
  [k]
  (-> (data/get-app-state :app/route) :route/query (get k)))

(defn query-param-int
  ([k]
   (reader/parse-int (query-param k)))
  ([k mn mx]
   (-> (query-param k)
       (reader/parse-int)
       (min mx)
       (max mn))))
