(ns witan.ui.services.analytics
  (:require [witan.ui.util :as util]
            [venue.core :as venue]
            [cljs-time.core :as t]
            [cljs-time.format :as tf])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.macros :as wm]))

(wm/create-standard-service!)

(defn log-event
  [message]
  (log/debug "Analytics:" message))

;;;;;;;;;;;;;;;;;;;;;

(defmulti do-login (fn [l response] l))
(defmethod do-login true
  [_ {:keys [id username name]}]
  (log-event "Switching ON Intercom")
  (.Intercom js/window "boot"
             (clj->js {:app_id "hwl3xxh2"
                       :user_id id
                       :email username
                       :name name
                       :widget {:activator "#IntercomDefaultWidget"}})))

(defmethod do-login false
  [_ _]
  (log-event "Switching OFF Intercom")
  (.Intercom js/window "shutdown"))

(util/inline-subscribe!
 :api/user-identified
 #(do-login true %))

(util/inline-subscribe!
 :api/user-logged-out
 #(do-login false))
