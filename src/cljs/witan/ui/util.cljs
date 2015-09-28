(ns ^:figwheel-always witan.ui.util
    (:require [cljs.core.async :refer [<! chan]]
              [venue.core :as venue])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log]))

(def state (atom {:logged-in? false}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn contains-str
  "Performs a case-insensitive substring match"
  [source match]
  (not= -1 (.indexOf (.toLowerCase source) (.toLowerCase match))))

(defn contains-str-regex
  "Performs a case-insensitive regex match"
  [source pattern]
  (boolean (re-find (js/RegExp. pattern "i") source)))

(defn goto-window-location!
  "Sends the window to the specified location"
  [location]
  (set! (.. js/document -location -href) location))

(defn inline-subscribe!
  "Sugar for adding a subscription to the venue message bus"
  [topic fnc]
  (let [ch (chan)]
    (venue/subscribe! topic ch)
    (go-loop []
      (let [{:keys [content]} (<! ch)]
        (fnc content))
      (recur))))

(defn add-ns
  "Adds a namespace to a keyword"
  [ns key]
  (let [sns (name ns)
        skey (name key)]
    (keyword sns skey)))

(defn map-add-ns
  "Adjusts an entire map by adding namespace to all the keys"
  [ns m]
  (map (fn [[k v]] (hash-map (add-ns ns k) v)) m))
