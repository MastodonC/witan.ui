(ns witan.ui.controllers.intercom
  (:require [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

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
                         :widget {:activator "#IntercomDefaultWidget"}}))))

(defonce subscriptions
  (when (cljs-env :intercom)
    (data/subscribe-topic :data/user-logged-in on-user-logged-in)))
