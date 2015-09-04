(ns witan.ui.async
  (:require [cljs.core.async :as async]
            [om.core :as om :include-macros true]
            [cljs.core.async :refer [chan close!]]
            [witan.schema.core :refer [Events]]))

(defn timeout [ms]
  (let [c (chan)]
    (js/setTimeout (fn [] (close! c)) ms)
    c))

(defn put!
  ([port val]
   (async/put! port val))
  ([port event args]
   ;; FIXME don't do this check in prod?
   (if (contains? Events event)
     (async/put! port [event args])
     (throw (js/Error. (str "An unregistered event was raised - " event))))))

(defn raise!
  [owner event data]
  (let [c (om/get-shared owner [:comms :input])]
    (put! c event data)))
