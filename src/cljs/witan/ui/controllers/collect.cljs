(ns witan.ui.controllers.collect
  (:require [witan.ui.activities :as activities]
            [witan.ui.data :as data]
            [witan.ui.ajax :as ajax]
            [witan.ui.strings :refer [get-string]]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]))

(defn reset-messages
  []
  (data/swap-app-state! :app/collect assoc :collect/failure-message nil)
  (data/swap-app-state! :app/collect assoc :collect/success-message nil))

(defn reset-pending
  [b]
  (data/swap-app-state! :app/collect assoc :collect/pending? b))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :reset-messages
  [_ _]
  (reset-messages))

(defmethod handle
  :send-collect-request
  [event {:keys [groups message metadata]}]
  (reset-pending true)
  (reset-messages)
  (activities/start-activity!
   :send-collect-request
   (data/new-command!
    :kixi.collect/request-collection
    "1.0.0"
    {:kixi.collect/message message
     :kixi.collect/groups (map :kixi.group/id groups)
     :kixi.datastore.metadatastore/id (:kixi.datastore.metadatastore/id metadata)})
   {:failed #(gstring/format (get-string :string.activity.send-collect-request/failed)
                             (:kixi.datastore.metadatastore/name metadata))
    :completed #(gstring/format (get-string :string.activity.send-collect-request/completed)
                                (count groups)
                                (:kixi.datastore.metadatastore/name metadata))
    :context {:groups groups
              :metadata metadata}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti on-activity-finished
  (fn [{:keys [args]}] [(:activity args) (:result args)]))

(defmethod on-activity-finished
  :default [_])

(defmethod on-activity-finished
  [:send-collect-request :completed]
  [{{:keys [context]} :args}]
  (let [{:keys [groups metadata]} context]
    (reset-pending false)
    (data/swap-app-state! :app/collect assoc :collect/success-message
                          (gstring/format (get-string :stringf/collection-succeeded) (count groups)))
    (.info js/toastr (gstring/format (get-string :string.activity.send-collect-request/completed)
                                     (count groups)
                                     (:kixi.datastore.metadatastore/name metadata)))))

(defmethod on-activity-finished
  [:send-collect-request :failed]
  [{:keys [args]}]
  (let [{:keys [context message]} args
        {:keys [metadata]} context]
    (reset-pending false)
    (data/swap-app-state! :app/collect assoc :collect/failure-message
                          (get-string :string/collection-failed (:kixi.event.collect.rejection/message message)))
    (.error js/toastr (gstring/format (get-string :string.activity.send-collect-request/failed)
                                      (:kixi.datastore.metadatastore/name metadata)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti on-activity-progressed
  (fn [{:keys [args]}]  (get-in args [:activity :activity])))

(defmethod on-activity-progressed
  :default [_])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce subscriptions
  (do
    (data/subscribe-topic :activity/activity-finished on-activity-finished)
    (data/subscribe-topic :activity/activity-progressed on-activity-progressed)))
