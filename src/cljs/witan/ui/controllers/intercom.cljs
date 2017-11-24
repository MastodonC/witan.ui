(ns witan.ui.controllers.intercom
  (:require [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defn do-update
  []
  (.Intercom js/window "update"))

(defn on-user-logged-in
  [{:keys [args]}]
  (let [{:keys [kixi.user/id
                kixi.user/username
                kixi.user/name]} args]
    (log/info "Intercom ON")
    (.Intercom js/window "boot"
               (clj->js {:app_id "m2kfb5n7"
                         :user_id id
                         :email username
                         :name name
                         :widget {:activator "#IntercomDefaultWidget"}}))
    (.setInterval js/window do-update (* 1000 60 4))))

(defn event-to-intercom [event-label payload]
  (.Intercom js/window
             "trackEvent"
             event-label
             (clj->js payload)))

(defn create-activity-payload
  [payload message]
  (-> payload
      (dissoc :message)
      (assoc :message-type (or (:kixi.comms.message/type message)
                               (:kixi.message/type message)
                               "Unknown"))
      (assoc :message-key  (str (or (:kixi.comms.event/key message)
                                    (:kixi.event/type message)
                                    (:kixi.comms.command/key message)
                                    (:kixi.command/type message)
                                    "n/a")))
      (assoc :message-id   (or (:kixi.comms.event/id message)
                               (:kixi.event/id message)
                               (:kixi.comms.command/id message)
                               (:kixi.command/id message)
                               "n/a"))))




(defn on-panic-event
  [{:keys [args]}]
  (event-to-intercom "panic" args))

(defn on-activity-finished
  [{:keys [args]}]
  (let [{:keys [activity message] :as payload} args]
    (event-to-intercom
     (name activity)
     (create-activity-payload payload message))))

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :open-new
  [event _]
  (.Intercom js/window "showNewMessage"))

(defonce subscriptions
  (when (cljs-env :intercom)
    (data/subscribe-topic :data/user-logged-in on-user-logged-in)
    (data/subscribe-topic :activity/activity-finished on-activity-finished)
    (data/subscribe-topic :data/panic on-panic-event)))
