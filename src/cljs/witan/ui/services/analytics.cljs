(ns witan.ui.services.analytics
  (:require [witan.ui.util :as util]
            [cljs.core.async :refer [close!]]
            [venue.core :as venue]
            [cljs-time.core :as t]
            [cljs-time.format :as tf])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.macros :as wm]))

(def analytics-state (atom {}))

(defn log-event
  [message]
  (log/debug "Analytics:" message))

(defn on-initialise
  []
  (log-event "System initialised."))

(defn request-handler
  [event args result-ch]
  (close! result-ch))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler request args response-ch))
    venue/IInitialise
    (initialise [owner _]
      (on-initialise))))

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

(defn change-view
  [{:keys [target id args] :as view-event}]
  (when (and (= target :app) (not= (:view-event @analytics-state) view-event))
    (swap! analytics-state #(assoc % :view-event view-event))
    (.Intercom js/window "trackEvent" "pageChange")))

;;;;;;;;;;;;;;;;;;;;;

(util/inline-subscribe!
 :api/user-identified
 #(do-login true %))

(util/inline-subscribe!
 :api/user-logged-out
 #(do-login false))

(util/inline-subscribe!
 :venue/view-activated
 #(change-view %))
