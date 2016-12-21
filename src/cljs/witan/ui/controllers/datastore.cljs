(ns witan.ui.controllers.datastore
  (:require [schema.core :as s]
            [ajax.core :as ajax]
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [witan.ui.route :as route]
            [goog.string :as gstring])
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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn select-current!
  [id]
  (when id
    (data/swap-app-state! :app/datastore assoc :ds/current id)))

(defn save-file-metadata!
  [{:keys [kixi.datastore.metadatastore/id] :as payload}]
  (data/swap-app-state! :app/datastore assoc-in [:ds/file-metadata id] payload))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API responses

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defmethod api-response
  [:upload :success]
  [{:keys [id file-size]} response]
  (log/info "Upload succeeded:" id)
  ;; now upload metadata
  (let [{:keys [pending-file
                info-name
                selected-schema]} (data/get-in-app-state :app/create-data :cd/pending-data)
        user-id (data/get-in-app-state :app/user :kixi.user/id)
        payload {:kixi.datastore.metadatastore/file-name info-name
                 :kixi.datastore.metadatastore/id id
                 :kixi.datastore.metadatastore/type "stored"
                 :kixi.datastore.metadatastore/sharing {:kixi.datastore.metadatastore/file-read [user-id]
                                                        :kixi.datastore.metadatastore/meta-read [user-id]}
                 :kixi.datastore.metadatastore/provenance {:kixi.datastore.metadatastore/source "upload"
                                                           :kixi.user/id user-id}
                 :kixi.datastore.metadatastore/size-bytes (.-size pending-file)
                 :kixi.datastore.metadatastore/header true}
        payload (if selected-schema (assoc payload :kixi.datastore.schemastore/id selected-schema) payload)]
    (data/command! :kixi.datastore.filestore/create-file-metadata "1.0.0" payload)))

(defmethod api-response
  [:upload :failure]
  [_ response]
  (log/severe "Upload failed:" response)
  (data/swap-app-state! :app/create-data assoc :cd/pending? false)
  (data/swap-app-state! :app/create-data assoc :cd/message :api-failure))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Events

(defmulti on-event
  (fn [{:keys [args]}] [(:kixi.comms.event/key args) (:kixi.comms.event/version args)]))

(defmethod on-event
  :default [x])

(defmethod on-event
  [:kixi.datastore.filestore/upload-link-created "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [kixi.datastore.filestore/upload-link
                kixi.datastore.filestore/id]} payload
        {:keys [pending-file]} (data/get-in-app-state :app/create-data :cd/pending-data)]
    (log/debug "Uploading to" upload-link)
    (ajax/PUT upload-link
              {:body pending-file
               :handler (partial api-response {:event :upload :status :success :id id})
               :error-handler (partial api-response {:event :upload :status :failure})})))

(defmethod on-event
  [:kixi.datastore.file/created "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [kixi.datastore.metadatastore/id]} payload]
    (save-file-metadata! payload)
    (data/swap-app-state! :app/create-data assoc :cd/pending? false)
    (route/navigate! :app/data {:id id})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :search-schema
  [event {:keys [search]}]
  (data/swap-app-state! :app/datastore assoc :schema/search-results
                        [{:schema/name "Net New Dwellings"
                          :schema/author {:kixi.group/name "GLA Demography"
                                          :kixi.group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                                          :kixi.group/type :group}
                          :schema/id "a74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"}
                         {:schema/name "Test Schema"
                          :schema/author {:kixi.group/name "GLA Demography"
                                          :kixi.group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                                          :kixi.group/type :group}
                          :schema/id "a74f742d-9cb9-4ede-aeaf-f82aa4b6f310"}]))

(defmethod handle
  :reset-errors
  [_ _]
  (data/swap-app-state! :app/create-data dissoc :cd/message))

(defmethod handle
  :upload
  [event data]
  (data/swap-app-state! :app/create-data assoc :cd/pending? true)
  (data/swap-app-state! :app/create-data assoc :cd/pending-data data)
  (data/command! :kixi.datastore.filestore/create-upload-link "1.0.0" nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :datastore/files-by-author
  [[_ data]]
  (reset! dash-query-pending? false)
  (log/debug ">>>>> GOT RESULTS" data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defn send-dashboard-query!
  [id]
  (when-not @dash-query-pending?
    (reset! dash-query-pending? true)
    (data/query `[{:datastore/files-by-author ~(:header query-fields)}]
                on-query-response)))

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/data-dash
  [_]
  (if-let [id (:kixi.user/id (data/get-app-state :app/user))]
    (send-dashboard-query! id)))

(defmethod on-route-change
  :app/data
  [{:keys [args]}]
  (data/swap-app-state! :app/datastore assoc :ds/pending? true)
  (let [rts-id (get-in args [:route/params :id])]
    (select-current! rts-id)
    #_(data/query `[{[:datastore/file-metadata ~rts-id] ~(:full query-fields)}]
                  on-query-response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-user-logged-in
  [{:keys [args]}]
  (let [{:keys [kixi.user/id]} args
        {:keys [route/path]} (data/get-app-state :app/route)]
    (when (= path :app/data-dash)
      (send-dashboard-query! id))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce subscriptions
  (do (data/subscribe-topic :data/route-changed  on-route-change)
      (data/subscribe-topic :data/user-logged-in on-user-logged-in)
      (data/subscribe-topic :data/event-received on-event)))
