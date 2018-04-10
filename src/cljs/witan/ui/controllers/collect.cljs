(ns witan.ui.controllers.collect
  (:require [witan.ui.activities :as activities]
            [witan.ui.route :as route]
            [witan.ui.data :as data]
            [witan.ui.ajax :as ajax]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.title :refer [set-title!]]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]))

(def bundle-add-page-data-query-param :d)

(defn reset-messages
  []
  (data/swap-app-state! :app/collect assoc :collect/failure-message nil)
  (data/swap-app-state! :app/collect assoc :collect/success-message nil))

(defn reset-pending
  [b]
  (data/swap-app-state! :app/collect assoc :collect/pending? b))

;; Bundle add to datapack via collect and share: message and pending components.
(defn reset-bundle-add-messages
  []
  (data/swap-app-state! :app/bundle-add assoc :ba/failure-message nil)
  (data/swap-app-state! :app/bundle-add assoc :ba/success-message nil))

(defn reset-bundle-add-pending
  [b]
  (data/swap-app-state! :app/bundle-add assoc :ba/pending? b))

(defn get-self-group
  [{:keys [kixi.user/id]}]
  )

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :reset-messages
  [_ _]
  (reset-messages))

(defmethod handle
  :reset-bundle-add-messages
  [_ _]
  (reset-bundle-add-messages))

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
    {:kixi.collect.request/message message
     :kixi.collect.request/submit-route (str "/#" (route/find-path :app/datapack-bundle-add) "?" (name bundle-add-page-data-query-param) "=")
     :kixi.collect.request/requested-groups (set (map :kixi.group/id groups))
     :kixi.collect.request/receiving-groups #{(:kixi.user/self-group (data/get-user))} ;; TODO allow user to select?
     :kixi.datastore.metadatastore/id (:kixi.datastore.metadatastore/id metadata)})
   {:failed #(gstring/format (get-string :string.activity.send-collect-request/failed)
                             (:kixi.datastore.metadatastore/name metadata))
    :completed #(gstring/format (get-string :string.activity.send-collect-request/completed)
                                (count groups)
                                (:kixi.datastore.metadatastore/name metadata))
    :context {:groups groups
              :metadata metadata}}))

(defmethod handle
  :add-files-to-datapack
  [event {:keys [added-files]}]
  (reset-bundle-add-pending true)
  (reset-bundle-add-messages)
  (let [data (data/get-in-app-state :app/bundle-add :ba/data)
        datapack-id (:kixi.datastore.metadatastore/id data)
        bundled-ids (set (map :kixi.datastore.metadatastore/id added-files))]
    (activities/start-activity!
     :submit-to-collection
     (data/new-command! :kixi.datastore/add-files-to-bundle "1.0.0"
                        {:kixi.datastore.metadatastore/id datapack-id
                         :kixi.datastore.metadatastore/bundled-ids bundled-ids})
     {:failed #(get-string :string.activity.add-files-to-datapack/failed)
      :completed #(get-string :string.activity.add-files-to-datapack/completed)
      :context (assoc data :kixi.datastore.metadatastore/bundled-ids bundled-ids)})))

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

(defmethod on-activity-finished
  [:submit-to-collection :completed]
  [{:keys [args]}]
  (reset-bundle-add-pending false)
  (data/swap-app-state! :app/bundle-add assoc :ba/success-message
                        (get-string :string.activity.add-files-to-datapack/completed))
  (.info js/toastr (:log args)))

(defmethod on-activity-finished
  [:submit-to-collection :failed]
  [{:keys [args]}]
  (let [{:keys [message]} args]
    (reset-bundle-add-pending false)
    (data/swap-app-state! :app/bundle-add assoc :ba/failure-message
                          (get-string :string.activity.add-files-to-datapack/failed " " (:kixi.event.metadata.sharing-change.rejection/reason message)))
    (.error js/toastr (:log args))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti on-activity-progressed
  (fn [{:keys [args]}] (get-in args [:activity :activity])))

(defmethod on-activity-progressed
  :default [_])

(defmethod on-activity-progressed
  :submit-to-collection
  [{args :args}]
  (when (= 2 (get-in args [:activity :state :state-index]))
    (let [{:keys [kixi.collect.request/receiving-groups
                  :kixi.datastore.metadatastore/bundled-ids]} (get-in args [:activity :context])]
      (data/new-command!
       :kixi.datastore/sharing-change "2.0.0"
       {:kixi.datastore.metadatastore/activity :kixi.datastore.metadatastore/meta-read
        ;; TODO only do the FIRST group! This is a hack until this whole process is moved to kixi.collect!
        :kixi.group/id (first receiving-groups)
        :kixi.datastore.metadatastore/sharing-update :kixi.datastore.metadatastore/sharing-conj
        ;; TODO only do the FIRST metadata ID! This is a hack until this whole process is moved to kixi.collect!
        :kixi.datastore.metadatastore/id (first bundled-ids)})
      (data/new-command!
       :kixi.datastore/sharing-change "2.0.0"
       {:kixi.datastore.metadatastore/activity :kixi.datastore.metadatastore/file-read
        ;; TODO only do the FIRST group! This is a hack until this whole process is moved to kixi.collect!
        :kixi.group/id (first receiving-groups)
        :kixi.datastore.metadatastore/sharing-update :kixi.datastore.metadatastore/sharing-conj
        ;; TODO only do the FIRST metadata ID! This is a hack until this whole process is moved to kixi.collect!
        :kixi.datastore.metadatastore/id (first bundled-ids)}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/datapack-bundle-add
  [_]
  (set-title! (get-string :string/share-files-to-datapack))
  (try
    (let [data (-> bundle-add-page-data-query-param
                   (route/get-query-param)
                   (data/decode-string)
                   (data/transit-decode))]
      (log/debug "Collection data:" data)
      (data/swap-app-state! :app/bundle-add assoc :ba/data data))
    (catch js/Object e
      (data/swap-app-state! :app/bundle-add assoc :ba/data nil))))

(defonce subscriptions
  (do
    (data/subscribe-topic :data/route-changed  on-route-change)
    (data/subscribe-topic :activity/activity-finished on-activity-finished)
    (data/subscribe-topic :activity/activity-progressed on-activity-progressed)))
