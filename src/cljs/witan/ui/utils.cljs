(ns witan.ui.utils
  (:require [cljsjs.moment]
            [witan.ui.data :as data]
            [cljs.reader :as reader])
  (:require-macros [cljs-log.core :as log]))

(defn iso-time-as-moment
  [time]
  (.calendar (js/moment. (str time) "YYYYMMDD HHmmss")))

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
