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

(defmulti do-login (fn [l] l))
(defmethod do-login true
  [_]
  (log-event "Switching ON Intercom")
  (.Intercom js/window "boot"))

(defmethod do-login false
  [_]
  (log-event "Switching OFF Intercom")
  (.Intercom js/window "boot"))

(util/inline-subscribe!
 :api/user-logged-in
 #(do-login true))

(util/inline-subscribe!
 :api/user-logged-out
 #(do-login false))
