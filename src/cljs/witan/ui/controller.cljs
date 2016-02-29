(ns witan.ui.controller
  (:require [witan.ui.controllers.user :as user]
            [cljs.core.async :refer [<! chan put!]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]))

(defn event->handler
  [event]
  (let [nsp (namespace event)]
    (get {"user" user/handle}
         nsp)))

(defonce event-chan (chan))

(go-loop []
  (let [{:keys [owner event args]} (<! event-chan)
        handler (event->handler event)
        un-ns-event (-> event name keyword)]
    (handler un-ns-event owner args))
  (recur))

(defn raise!
  [owner event args]
  (put! event-chan {:owner owner :event event :args args}))
