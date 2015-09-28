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
  [location]
  (set! (.. js/document -location -href) location))

(defn inline-subscribe!
  [topic fnc]
  (let [ch (chan)]
    (venue/subscribe! topic ch)
    (go-loop []
      (let [{:keys [content]} (<! ch)]
        (fnc content))
      (recur))))
