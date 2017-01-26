(ns witan.ui.title
  (:require [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]))

(def buffered-title (atom nil))

(defn set-title!
  [& msg]
  (let [logged-in? (data/get-in-app-state :app/user :kixi.user/id)
        title (if logged-in?
                (do
                  (reset! buffered-title nil)
                  (str "Witan - " (clojure.string/join " " msg)))
                (do
                  (reset! buffered-title msg)
                  "Witan"))]
    (set! (.. js/window -document -title) title)
    (log/debug "Title set to:" title)))

(defn on-user-logged-in
  [{:keys [args]}]
  (let [{:keys [kixi.user/id]} args
        {:keys [route/path]} (data/get-app-state :app/route)]
    (when @buffered-title
      (apply set-title! @buffered-title))))

(defonce subscriptions
  (do
    (data/subscribe-topic :data/user-logged-in on-user-logged-in)))
