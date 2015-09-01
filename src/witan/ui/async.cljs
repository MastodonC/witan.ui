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
  [port val]
  (async/put! port val))

(defn raise!
  [owner event data]
  ;; FIXME don't do this check in prod?
  (if (contains? Events event)
    (let [c (om/get-shared owner [:comms :input])]
      (put! c [event data]))
    (throw (js/Error. (str "An unregistered event was raised - " event)))))
