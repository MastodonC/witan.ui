(ns witan.ui.title
  (:require [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]))

(defn set!
  [& msg]
  (let [logged-in? (data/get-in-app-state :app/user :kixi.user/id)
        title (if logged-in?
                (str "Witan - " (clojure.string/join " " msg))
                "Witan")]
    (set! (.. js/window -document -title) title)
    (log/debug "Title set to:" title)))
