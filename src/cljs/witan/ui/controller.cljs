(ns witan.ui.controller
  (:require [witan.ui.controllers.user :as user]
            [witan.ui.controllers.workspace :as workspace]
            [cljs.core.async :refer [<! chan put!]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]
                   [cljs-log.core :as log]))

(defn event->handler
  [event]
  (let [nsp (namespace event)]
    (get {"user"      user/handle
          "workspace" workspace/handle}
         nsp)))

(defonce event-chan (chan))

(go-loop []
  (let [{:keys [owner event args]} (<! event-chan)
        handler (event->handler event)
        un-ns-event (-> event name keyword)]
    (handler un-ns-event owner args))
  (recur))

(defn raise!
  ([owner event]
   (raise! owner event {}))
  ([owner event args]
   (let [payload (merge {:owner owner :event event} (when (not-empty args) {:args args}))]
     (log/debug "Raising event" event args)
     (put! event-chan payload))))
