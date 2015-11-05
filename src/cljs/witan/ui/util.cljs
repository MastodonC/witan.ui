(ns ^:figwheel-always witan.ui.util
    (:require [cljs.core.async :refer [<! chan]]
              [venue.core :as venue]
              [cljs-time.core :as t]
              [cljs-time.format :as tf]
              [witan.ui.strings :refer [get-string]])
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

(defn str-fmt-map
  "String format with map using mustache delimiters, e.g. (str-fmt-map 'hello {{name}}' {:name 'foo'})"
  [s m]
  (reduce (fn [x [k v]] (clojure.string/replace x (str "{{"(name k)"}}") (str v)) ) s m))

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

(defn remove-ns
  "Removes a namespace from a keyword"
  [key]
  (-> key name keyword))

(defn map-add-ns
  "Adjusts an entire map by adding namespace to all the keys"
  [ns m]
  (reduce (fn [a [k v]] (assoc a (add-ns ns k) v)) {} m))

(defn map-remove-ns
  "Adjusts an entire map by removing namespace to all the keys"
  [m]
  (reduce (fn [a [k v]] (assoc a (remove-ns k) v)) {} m))

(defn sanitize-filename
  [filename]
  (.replace filename #".*[\\\/]" ""))

(defn humanize-time
  [time-str]
  (let [now  (t/now)
        time (tf/parse (:date-hour-minute-second tf/formatters) time-str)
        clock (tf/unparse (tf/formatter "HH:mm A") time)
        front (cond
                (= (t/day time) (t/day now)) (get-string :today)
                (= (- (t/day now) (t/day time)) 1) (get-string :yesterday)
                :default (tf/unparse (tf/formatter "MMMM dth") time))]
    (str front ", " clock)))
