(ns witan.ui.controllers.rts
  (:require [schema.core :as s]
            [ajax.core :as ajax]
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def dash-query-pending? (atom false))

(def query-fields
  {:header [:kixi.data-acquisition.request-for-data/recipients
            :kixi.data-acquisition.request-for-data/created-at
            :kixi.data-acquisition.request-for-data/request-id
            :kixi.data-acquisition.request-for-data/schema]
   :full [{:kixi.data-acquisition.request-for-data/recipients
           [:kixi.group/id
            :kixi.group/emails
            :kixi.group/type
            :kixi.group/name]}
          {:kixi.data-acquisition.request-for-data/destinations
           [:kixi.group/id
            :kixi.group/type
            :kixi.group/name]}
          :kixi.data-acquisition.request-for-data/created-at
          :kixi.data-acquisition.request-for-data/request-id
          {:kixi.data-acquisition.request-for-data/schema
           [:id :name]}
          :kixi.data-acquisition.request-for-data/message]})

(defn add-request!
  [request]
  (when-let [id (:kixi.data-acquisition.request-to-share/request-id request)]
    (data/swap-app-state! :app/request-to-share update :rts/requests #(assoc % id request))))

(defn select-current!
  [id]
  (when id
    (data/swap-app-state! :app/request-to-share assoc :rts/current id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :data-acquisition/requests-by-requester
  [[_ data]]
  (reset! dash-query-pending? false)
  (log/debug ">>>>> GOT RESULTS" data))

(defmethod on-query-response
  :data-acquisition/request-by-id
  [[_ request]]
  (add-request! request)
  (select-current! (:kixi.data-acquisition.request-to-share/request-id request))
  (data/swap-app-state! :app/request-to-share assoc :rts/pending? false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :create
  [event {:keys [recipients schema destinations message] :as payload}]
  (let [req-id  (str (random-uuid))
        user-id (:user/id (data/get-app-state :app/user))
        schema-id (:schema/id schema)]
    (data/swap-app-state! :app/create-rts assoc :crts/pending? true)
    (data/swap-app-state! :app/create-rts
                          assoc-in [:crts/pending-payload req-id]
                          {:kixi.data-acquisition.request-to-share/recipients recipients
                           :kixi.data-acquisition.request-to-share/destinations destinations
                           :kixi.data-acquisition.request-to-share/schema schema
                           :kixi.data-acquisition.request-to-share/message message
                           :kixi.data-acquisition.request-to-share/request-id req-id
                           :kixi.data-acquisition.request-to-share/created-at (utils/jstime->str)
                           :kixi.data-acquisition.request-to-share/requester-id user-id})
    (data/command!
     :kixi.data-acquisition.request-to-share/create "1.0.0"
     {:kixi.data-acquisition.request-to-share/request-id       req-id
      :kixi.data-acquisition.request-to-share/requester-id     user-id
      :kixi.data-acquisition.request-to-share/schema-id        schema-id
      :kixi.data-acquisition.request-to-share/recipient-ids    (map :kixi.group/id
                                                                    recipients)
      :kixi.data-acquisition.request-to-share/destination-ids  (map :kixi.group/id
                                                                    destinations)
      :kixi.data-acquisition.request-to-share/message (or message "")})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events

(defmulti on-event
  (fn [{:keys [args]}] [(:kixi.comms.event/key args) (:kixi.comms.event/version args)]))

(defmethod on-event
  :default [_])

(defmethod on-event
  [:kixi.data-acquisition.request-to-share/create-failed "1.0.0"]
  [{event :args}]
  (log/warn "FAILED to create the RTS")
  (data/swap-app-state! :app/create-rts assoc :crts/pending? false)
  (data/swap-app-state! :app/create-rts assoc :crts/message :string/create-rts-message-failed))

(defmethod on-event
  [:kixi.data-acquisition.request-to-share/created-successfully "1.0.0"]
  [{event :args}]
  (let [request (:kixi.comms.event/payload event)
        id (:kixi.data-acquisition.request-to-share/request-id request)
        pp (data/get-in-app-state :app/create-rts :crts/pending-payload id)]
    (log/debug "Got RTS created event - PP is" (if pp "matched" "NOT matched"))
    (data/swap-app-state! :app/create-rts assoc :crts/pending? false)
    (when pp
      (data/swap-app-state! :app/create-rts update :crts/pending-payload #(dissoc % id))
      (add-request! pp)
      (select-current! id)
      (route/navigate! :app/rts {:id id} {:new 1}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defn send-dashboard-query!
  [id]
  (when-not @dash-query-pending?
    (reset! dash-query-pending? true)
    (data/query `[{[:data-acquisition/requests-by-requester ~id] ~(:header query-fields)}]
                on-query-response)))

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/request-to-share
  [_]
  (if-let [id (:kixi.user/id (data/get-app-state :app/user))]
    (send-dashboard-query! id)))

(defmethod on-route-change
  :app/rts
  [{:keys [args]}]
  (data/swap-app-state! :app/request-to-share assoc :rts/pending? true)
  (let [rts-id (cljs.core/uuid (get-in args [:route/params :id]))]
    (select-current! (str rts-id))
    (data/query `[{[:data-acquisition/request-by-id ~rts-id] ~(:full query-fields)}]
                on-query-response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-user-logged-in
  [{:keys [args]}]
  (let [{:keys [kixi.user/id]} args
        {:keys [route/path]} (data/get-app-state :app/route)]
    (when (= path :app/request-to-share)
      (send-dashboard-query! id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce subscriptions
  (do (data/subscribe-topic :data/route-changed  on-route-change)
      (data/subscribe-topic :data/user-logged-in on-user-logged-in)
      (data/subscribe-topic :data/event-received on-event)))
