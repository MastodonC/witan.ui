(ns witan.ui.controllers.datastore
  (:require [schema.core :as s]
            [witan.ui.ajax :as ajax]
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [witan.ui.route :as route]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.title :refer [set-title!]]
            [goog.string :as gstring]
            [cljsjs.toastr])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def dash-query-pending? (atom false))

(def query-fields
  {:header [{:activities [:kixi.datastore.metadatastore/meta-read :kixi.datastore.metadatastore/file-read]}]
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

(declare on-query-response)

(defn select-current!
  [id]
  (when id
    (data/swap-app-state! :app/datastore assoc :ds/current id)))

(defn save-file-metadata!
  [{:keys [kixi.datastore.metadatastore/id] :as payload}]
  (data/swap-app-state! :app/datastore assoc-in [:ds/file-metadata id] payload))

(defn add-file-flag!
  [id flag]
  (data/swap-app-state! :app/datastore update-in [:ds/file-flags id] #(conj (set %) flag)))

(defn remove-file-flag!
  [id flag]
  (data/swap-app-state! :app/datastore update-in [:ds/file-flags id] #(disj (set %) flag)))

(defn selected-groups->sharing-activities
  [groups activities]
  (zipmap activities
          (map (fn [activity]
                 (vec (keep (fn [[group group-activities]]
                              (when (get (:values group-activities) activity)
                                (:kixi.group/id group))) groups))) activities)))

(defn send-dashboard-query!
  [id]
  (when-not @dash-query-pending?
    (reset! dash-query-pending? true)
    (data/query {:datastore/metadata-with-activities [[[:kixi.datastore.metadatastore/meta-read]]
                                                      (:full query-fields)]}
                on-query-response)))

(defn send-single-file-item-query!
  [id]
  (data/query {:datastore/metadata-by-id
               [[id] (:full query-fields)]}
              on-query-response))

(defn upload-error->string
  [e]
  (case e
    :metadata-invalid :string/file-upload-metadata-invalid
    :string/file-upload-unknown-error))

(defn get-local-file
  [id]
  (data/get-in-app-state :app/datastore :ds/file-metadata id))

(defn ldiff
  [a b]
  (reduce-kv (fn [agg k v]
               (if (not= v (get b k))
                 (assoc agg k v)
                 agg)) {} a))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; API responses

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defn content?
  [v]
  (and v
       (if (or (coll? v) (string? v))
         (not-empty v)
         v)))

(defn filled-map
  [& keys-vals]
  (reduce
   (fn [acc [k v]]
     (if (content? v)
       (assoc acc k v)
       acc))
   {}
   (partition 2 keys-vals)))

(defmethod api-response
  [:upload :success]
  [{:keys [id file-size]} response]
  (log/info "Upload succeeded:" id)
  (data/swap-app-state! :app/create-data assoc
                        :cd/pending-message
                        {:message :string/upload-finalizing
                         :progress 1})
  ;; now upload metadata
  (let [{:keys [pending-file
                info-name
                info-description
                info-author
                info-maintainer
                info-source
                info-geo-smallest
                info-license-type
                info-license-usage
                info-temporal-cov-from
                info-temporal-cov-to
                info-tags
                selected-schema
                selected-groups]} (data/get-in-app-state :app/create-data :cd/pending-data)
        user-groups [(data/get-in-app-state :app/user :kixi.user/self-group)]
        user-id (data/get-in-app-state :app/user :kixi.user/id)
        ext (last (clojure.string/split (.-name pending-file) #"\."))
        tag-coll (when (not-empty info-tags) (clojure.string/split info-tags #","))
        payload (merge (filled-map :kixi.datastore.metadatastore/name info-name
                                   :kixi.datastore.metadatastore/description info-description
                                   :kixi.datastore.metadatastore/id id
                                   :kixi.datastore.metadatastore/type "stored"
                                   :kixi.datastore.metadatastore/file-type ext
                                   :kixi.datastore.metadatastore/sharing (selected-groups->sharing-activities
                                                                          selected-groups
                                                                          (keys (data/get-in-app-state :app/datastore :ds/activities)))
                                   :kixi.datastore.metadatastore/provenance {:kixi.datastore.metadatastore/source "upload"
                                                                             :kixi.user/id user-id}
                                   :kixi.datastore.metadatastore/size-bytes (.-size pending-file)
                                   :kixi.datastore.metadatastore/header true
                                   :kixi.datastore.metadatastore/author info-author
                                   :kixi.datastore.metadatastore/maintainer info-maintainer
                                   :kixi.datastore.metadatastore/source info-source
                                   :kixi.datastore.metadatastore/tags tag-coll
                                   :kixi.datastore.metadatastore.license/license (filled-map :kixi.datastore.metadatastore.license/type info-license-type
                                                                                             :kixi.datastore.metadatastore.license/usage info-license-usage)
                                   :kixi.datastore.metadatastore.time/temporal-coverage (filled-map :kixi.datastore.metadatastore.time/from info-temporal-cov-from
                                                                                                    :kixi.datastore.metadatastore.time/to info-temporal-cov-to))
                       (when (not-empty info-geo-smallest)
                         {:kixi.datastore.metadatastore.geography/geography (filled-map :kixi.datastore.metadatastore.geography/type "smallest"
                                                                                        :kixi.datastore.metadatastore.geography/level info-geo-smallest)}))
        payload (if selected-schema (assoc payload :kixi.datastore.schemastore/id selected-schema) payload)]
    (data/command! :kixi.datastore.filestore/create-file-metadata "1.0.0" payload)))

(defmethod api-response
  [:upload :failure]
  [_ response]
  (log/severe "Upload failed:" response)
  (data/swap-app-state! :app/create-data assoc :cd/pending? false)
  (data/swap-app-state! :app/create-data assoc :cd/message :api-failure))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :datastore/metadata-with-activities
  [[_ data]]
  (reset! dash-query-pending? false)
  (data/swap-app-state! :app/data-dash assoc
                        :items (get data :items)
                        :paging (get data :paging)))


(defmethod on-query-response
  :datastore/metadata-by-id
  [[_ data]]
  (if (:error data)
    (let [id (first (get-in data [:original :params]))
          tries (data/get-in-app-state :app/datastore :ds/query-tries)]
      (if (< tries 3)
        (js/setTimeout
         #(do
            (data/swap-app-state! :app/datastore update :ds/query-tries inc)
            (send-single-file-item-query! id))
         1000)
        (do
          (log/warn "File" id "is not accessible.")
          (data/swap-app-state! :app/datastore assoc :ds/error :string/file-inaccessible)
          (data/swap-app-state! :app/datastore assoc :ds/query-tries 0))))
    (do
      (data/swap-app-state! :app/datastore assoc :ds/query-tries 0)
      (save-file-metadata! data)
      (set-title! (:kixi.datastore.metadatastore/name data)))))

(defmethod on-query-response
  :error
  [[o data]]
  (reset! dash-query-pending? false)
  (log/severe "Query Error:" o data))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/data-dash
  [_]
  (if-let [id (:kixi.user/id (data/get-app-state :app/user))]
    (send-dashboard-query! id))
  (set-title! (get-string :string/title-data-dashboard)))

(defmethod on-route-change
  :app/data-create
  [_]
  (data/swap-app-state! :app/datastore dissoc :cd/error)
  (set-title! (get-string :string/title-data-create)))

(defmethod on-route-change
  :app/data
  [{:keys [args]}]
  (data/swap-app-state! :app/datastore dissoc :ds/error)
  (data/swap-app-state! :app/datastore assoc :ds/pending? true)
  (let [id (get-in args [:route/params :id])]
    (send-single-file-item-query! id)
    (select-current! id)
    (set-title! :string/title-data-loading)))


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
    (if (clojure.string/starts-with? upload-link "file")
      (do
                                        ;for testing locally, so you can manually copy the metadata-one-valid.csv file
        (log/debug "Sleeping, copy file!")
        (time/sleep 20000)
        (api-response {:event :upload :status :success :id id} 14))
      (ajax/PUT* upload-link
                 {:body pending-file
                  :progress-handler #(let [upload-frac (/ (.-loaded %) (.-total %))]
                                       (data/swap-app-state! :app/create-data assoc
                                                             :cd/pending-message
                                                             {:message :string/uploading
                                                              :progress upload-frac})
                                       (log/debug "progress" upload-frac))
                  :handler (partial api-response {:event :upload :status :success :id id})
                  :error-handler (partial api-response {:event :upload :status :failure})}))))

(defmethod on-event
  [:kixi.datastore.file/created "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [kixi.datastore.metadatastore/id]} payload]
    (save-file-metadata! payload)
    (js/setTimeout
     #(do
        (data/swap-app-state! :app/create-data assoc :cd/pending? false)
        (route/navigate! :app/data {:id id})) 500)))

(defmethod on-event
  [:kixi.datastore.file-metadata/rejected "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [reason]} payload]
    (data/swap-app-state! :app/create-data assoc :cd/error (upload-error->string reason))
    (data/swap-app-state! :app/create-data assoc :cd/pending? false)))

(defmethod on-event
  [:kixi.datastore.metadatastore/sharing-change-rejected "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [kixi.datastore.metadatastore/id]} payload]
    (log/warn "An adjustment to the sharing properties of" id "was rejected. Restoring...")
    (.error js/toastr (str "An adjustment to the sharing properties of '"
                           (:kixi.datastore.metadatastore/name (get-local-file id))
                           "' was rejected."))
    (send-single-file-item-query! id)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :search-schema
  [event {:keys [search]}]
  (data/swap-app-state! :app/datastore assoc :schema/search-results
                        []))

(defmethod handle
  :reset-errors
  [_ _]
  (data/swap-app-state! :app/create-data dissoc :cd/message)
  (data/swap-app-state! :app/create-data dissoc :cd/error)
  (data/swap-app-state! :app/create-data assoc :cd/pending? false))

(defmethod handle
  :upload
  [event data]
  (data/swap-app-state! :app/create-data assoc :cd/pending? true)
  (data/swap-app-state! :app/create-data assoc :cd/pending-data data)
  (data/swap-app-state! :app/create-data assoc :cd/pending-message {:message :string/preparing-upload
                                                                    :progress 0})
  (data/command! :kixi.datastore.filestore/create-upload-link "1.0.0" nil))

(defmethod handle
  :sharing-change
  [event {:keys [current activity target-state group] :as data}]
  (data/swap-app-state! :app/datastore update-in [:ds/file-metadata current
                                                  :kixi.datastore.metadatastore/sharing activity]
                        (fn [groups]
                          (vec (let [g-set (set groups)]
                                 (if target-state
                                   (conj g-set group)
                                   (disj g-set group))))))
  (data/command! :kixi.datastore.metadatastore/sharing-change "1.0.0"
                 {:kixi.datastore.metadatastore/id current
                  :kixi.datastore.metadatastore/activity activity
                  :kixi.group/id (:kixi.group/id group)
                  :kixi.datastore.metadatastore/sharing-update (if target-state
                                                                 :kixi.datastore.metadatastore/sharing-conj
                                                                 :kixi.datastore.metadatastore/sharing-disj)}))

(defmethod handle
  :sharing-add-group
  [event {:keys [current group] :as data}]
  (data/swap-app-state! :app/datastore update-in [:ds/file-metadata current
                                                  :kixi.datastore.metadatastore/sharing
                                                  :kixi.datastore.metadatastore/meta-read]
                        (fn [groups]
                          (let [g-set (set groups)]
                            (conj g-set group))))
  (data/command! :kixi.datastore.metadatastore/sharing-change "1.0.0"
                 {:kixi.datastore.metadatastore/id current
                  :kixi.datastore.metadatastore/activity :kixi.datastore.metadatastore/meta-read
                  :kixi.group/id (:kixi.group/id group)
                  :kixi.datastore.metadatastore/sharing-update :kixi.datastore.metadatastore/sharing-conj}))

(defmethod handle
  :metadata-change
  [event {:keys [kixi.datastore.metadatastore/id] :as md}]
  (let [current-md (data/get-in-app-state :app/datastore :ds/file-metadata id)
        md-diff (ldiff md current-md)]
    (when-not (empty? md-diff)
      (set-title! (:kixi.datastore.metadatastore/name md))
      (add-file-flag! id :metadata-saving)
      (save-file-metadata! md))
    #_(data/command! :kixi.datastore.metadatastore/sharing-change "1.0.0"
                     {:kixi.datastore.metadatastore/id current
                      :kixi.datastore.metadatastore/activity :kixi.datastore.metadatastore/meta-read
                      :kixi.group/id (:kixi.group/id group)
                      :kixi.datastore.metadatastore/sharing-update :kixi.datastore.metadatastore/sharing-conj})))
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
