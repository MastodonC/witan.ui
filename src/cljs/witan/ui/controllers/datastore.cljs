(ns witan.ui.controllers.datastore
  (:require [schema.core :as s]
            [witan.ui.ajax :as ajax]
            [witan.ui.data :as data]
            [witan.ui.activities :as activities]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [cljs-time.core :as t]
            [inflections.core :as i]
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

(defn reset-properties!
  [id]
  (when id
    (data/swap-app-state! :app/datastore update :ds/file-properties dissoc id)))

(defn reset-file-edit-metadata!
  ([]
   (reset-file-edit-metadata! nil))
  ([md]
   (data/swap-app-state! :app/datastore assoc :ds/file-metadata-editing md)))

(defn reset-file-edit-metadata-command!
  []
  (data/swap-app-state! :app/datastore assoc :ds/file-metadata-editing-command nil))

(defn save-file-metadata!
  [{:keys [kixi.datastore.metadatastore/id] :as payload}]
  (when id
    (log/debug "Saving file metadata..." id)
    (data/swap-app-state! :app/datastore assoc-in [:ds/file-metadata id] payload)
    (when (= id (data/get-in-app-state :app/datastore :ds/file-metadata-editing :kixi.datastore.metadatastore/id))
      (reset-file-edit-metadata! payload))))

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
    (data/swap-app-state! :app/data-dash dissoc :items)
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
  ;; TODO this whole thing needs sorting out
  ;; as we don't do it this way anymore.
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

(defn filter-files-by-type
  [file-type-filter]
  (let [p
        (case file-type-filter
          :files (comp (partial = "stored") :kixi.datastore.metadatastore/type)
          :datapacks (comp (partial = "datapack") :kixi.datastore.metadatastore/bundle-type))]
    (fn [items]
      (filter p items))))

(defmethod on-query-response
  :datastore/metadata-with-activities
  [[_ {:keys [items paging]}]]
  (reset! dash-query-pending? false)
  (let [filter-fn
        (if-let [file-type-filter
                 (data/get-in-app-state :app/data-dash :dd/file-type-filter)]
          (filter-files-by-type file-type-filter)
          identity)]
    (data/swap-app-state! :app/data-dash assoc
                          :items (filter-fn items)
                          :paging paging)))


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
  [{:keys [args]}]
  (if-let [type-filter (keyword (get-in args [:route/query :type]))]
    (do
      (data/swap-app-state! :app/data-dash assoc :dd/file-type-filter type-filter))
    (data/swap-app-state! :app/data-dash dissoc :dd/file-type-filter))
  (if-let [id (:kixi.user/id (data/get-app-state :app/user))]
    (send-dashboard-query! id))
  (set-title! (get-string :string/title-data-dashboard)))

(defmethod on-route-change
  :app/data-create
  [_]
  (data/swap-app-state! :app/datastore dissoc :cd/error)
  (set-title! (get-string :string/title-data-create)))

(defmethod on-route-change
  :app/datapack-create
  [_]
  (data/swap-app-state! :app/create-datapack assoc :cdp/pending? false)
  (data/swap-app-state! :app/create-datapack dissoc :cdp/error)
  (set-title! (get-string :string/create-new-datapack)))

(defmethod on-route-change
  :app/data
  [{:keys [args]}]
  (data/swap-app-state! :app/datastore dissoc :ds/error)
  (data/swap-app-state! :app/datastore assoc :ds/pending? true)
  (let [id (get-in args [:route/params :id])]
    (send-single-file-item-query! id)
    (reset-properties! id)
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
                                                              :progress upload-frac}))
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
        (data/swap-app-state! :app/create-data dissoc :cd/pending-data)
        (data/swap-app-state! :app/create-data assoc :cd/pending? false)
        (route/navigate! :app/data {:id id} {:new 1})) 500)))

(defmethod on-event
  [:kixi.datastore.file-metadata/rejected "1.0.0"]
  [{:keys [args]}]
  (log/warn "TODO Change to activity failure")
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
    (.error js/toastr (gstring/format (get-string :stringf/reject-sharing-adjustments)
                                      (:kixi.datastore.metadatastore/name (get-local-file id))))
    (send-single-file-item-query! id)))

(defmulti on-metadata-updated
  (fn [payload] (:kixi.datastore.communication-specs/file-metadata-update-type payload)))

(defmethod on-metadata-updated
  :kixi.datastore.communication-specs/file-metadata-update
  [{:keys [kixi.datastore.metadatastore/id]}]
  (utils/remove-file-flag! id :metadata-saving)
  (.info js/toastr (get-string :stringf/metadata-saved)))

(defmethod on-metadata-updated
  :kixi.datastore.communication-specs/file-metadata-created
  [args])

(defmethod on-metadata-updated
  :kixi.datastore.communication-specs/file-metadata-sharing-updated
  [args])

(defmethod on-metadata-updated
  :default
  [p]
  (log/warn "Unknown metadata-updated type:" p))

(defmethod on-event
  [:kixi.datastore.file-metadata/updated "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [kixi.datastore.communication-specs/file-metadata-update-type]} payload]
    (on-metadata-updated payload)))

(defn metadata-invalid-field->error-string
  [field val]
  (get {:kixi.datastore.metadatastore.license/license (get-string :string/field-invalid-error (get-string :string/license))
        :kixi.datastore.metadatastore.time/temporal-coverage (get-string :string/field-invalid-error (get-string :string/temporal-coverage))
        :kixi.datastore.metadatastore/name (get-string :string/field-invalid-error (get-string :string/file-name) (str "(" val ")"))}
       field (get-string :string/unknown-error)))

(defn remove-update-from-key
  [k]
  (when (keyword? k)
    (let [up ".update"
          upl (count up)
          ns (or (namespace k) "")
          i (.lastIndexOf ns up)]
      (if (pos? i)
        (keyword (str (subs ns 0 i) (subs ns (+ i upl))) (name k))
        k))))

(defn collect-metadata-update-errors
  [{:keys [reason explanation]}]
  (case reason
    :unauthorised {:unauthorised :string/unauthorised-error}
    :invalid (let [{:keys [clojure.spec/problems]} explanation]
               (into {} (map (fn [{:keys [path val]}]
                               (when-let [fp (remove-update-from-key (second path))]
                                 (hash-map fp (metadata-invalid-field->error-string fp val)))) problems)))))

(defmethod on-event
  [:kixi.datastore.metadatastore/update-rejected "1.0.0"]
  [{:keys [args]}]
  (let [{:keys [kixi.comms.event/payload]} args
        {:keys [original reason explanation]} payload
        id (get-in original [:kixi.datastore.metadatastore/payload :kixi.comms.command/payload :kixi.datastore.metadatastore/id])]
    (log/warn "An adjustment to the metadata of" id "was rejected:" reason original)
    (utils/remove-file-flag! id :metadata-saving)
    (.error js/toastr (gstring/format (get-string :stringf/reject-metadata-adjustments)
                                      (:kixi.datastore.metadatastore/name (get-local-file id))))
    (let [errors (collect-metadata-update-errors payload)]
      (data/swap-app-state! :app/datastore assoc-in [:ds/file-properties id :update-errors] errors))))

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
  (activities/start-activity!
   :upload-file
   (data/command! :kixi.datastore.filestore/create-upload-link "1.0.0" nil)
   {:failed #(gstring/format (get-string :string.activity.upload-file/failed) (:info-name data))
    :completed #(gstring/format (get-string :string.activity.upload-file/completed) (:info-name data))}))

(defmethod handle
  :sharing-change
  [event {:keys [current activity target-state group] :as data}]
  (let [md (data/get-in-app-state :app/datastore :ds/file-metadata current)]
    (data/swap-app-state! :app/datastore update-in [:ds/file-metadata current
                                                    :kixi.datastore.metadatastore/sharing activity]
                          (fn [groups]
                            (vec (let [g-set (set groups)]
                                   (if target-state
                                     (conj g-set group)
                                     (disj g-set group))))))
    (activities/start-activity!
     :update-sharing
     (data/command! :kixi.datastore.metadatastore/sharing-change "1.0.0"
                    {:kixi.datastore.metadatastore/id current
                     :kixi.datastore.metadatastore/activity activity
                     :kixi.group/id (:kixi.group/id group)
                     :kixi.datastore.metadatastore/sharing-update (if target-state
                                                                    :kixi.datastore.metadatastore/sharing-conj
                                                                    :kixi.datastore.metadatastore/sharing-disj)})
     {:failed #(gstring/format (get-string :string.activity.update-sharing/failed)
                               (:kixi.datastore.metadatastore/name md))
      :completed #(gstring/format (get-string :string.activity.update-sharing/completed)
                                  (:kixi.datastore.metadatastore/name md)
                                  (i/capitalize (:kixi.group/type group))
                                  (:kixi.group/name group))})))

(defn add-group-to-file-sharing
  [perm {:keys [id group] :as data}]
  (let [md (get-local-file id)]
    (data/swap-app-state! :app/datastore update-in [:ds/file-metadata id
                                                    :kixi.datastore.metadatastore/sharing
                                                    perm]
                          (fn [groups]
                            (let [g-set (set groups)]
                              (conj g-set group))))
    (activities/start-activity!
     :update-sharing
     (data/command! :kixi.datastore.metadatastore/sharing-change "1.0.0"
                    {:kixi.datastore.metadatastore/id id
                     :kixi.datastore.metadatastore/activity perm
                     :kixi.group/id (:kixi.group/id group)
                     :kixi.datastore.metadatastore/sharing-update :kixi.datastore.metadatastore/sharing-conj})
     {:failed #(gstring/format (get-string :string.activity.update-sharing/failed)
                               (:kixi.datastore.metadatastore/name md))
      :completed #(gstring/format (get-string :string.activity.update-sharing/completed)
                                  (:kixi.datastore.metadatastore/name md))})))

(defmethod handle
  :sharing-add-group
  [event {:keys [current] :as data}]
  (add-group-to-file-sharing :kixi.datastore.metadatastore/meta-read (assoc data :id current)))

(defn md-key->update-command-key
  [k]
  (if (and (keyword? k) (namespace k))
    (keyword (str (namespace k) ".update") (name k))
    k))

(defn command-field->string-key
  [k]
  (get {:kixi.datastore.metadatastore.time.update/temporal-coverage :string/time-and-geog-coverage
        :kixi.datastore.metadatastore.geography.update/geography :string/time-and-geog-coverage
        :kixi.datastore.metadatastore.update/source-created :string/source-dates
        :kixi.datastore.metadatastore.update/source-updated :string/source-dates
        :kixi.datastore.metadatastore.license.update/license :string/license-info
        :kixi.datastore.metadatastore.update/tags :string/tags
        :kixi.datastore.metadatastore.update/author :string/author
        :kixi.datastore.metadatastore.update/name :string/file-name
        :kixi.datastore.metadatastore.update/source :string/file-source
        :kixi.datastore.metadatastore.update/maintainer :string/maintainer
        :kixi.datastore.metadatastore.update/description :string/file-description} k nil))

(defn soft-validate!
  [{:keys [kixi.datastore.metadatastore/id] :as md}]
  (let [result (atom true)]
    (when (> 1 (count (:kixi.datastore.metadatastore/name md)))
      (data/swap-app-state! :app/datastore assoc-in [:ds/file-properties id :update-errors :kixi.datastore.metadatastore/name]
                            (get-string :string/file-name-too-short))
      (reset! result false))
    @result))

(defmethod handle
  :metadata-change
  [event _]
  (let [{:keys [:kixi.datastore.metadatastore/id] :as md} (data/get-in-app-state :app/datastore :ds/file-metadata-editing)]
    (when (soft-validate! md)
      (let [orig-md (data/get-in-app-state :app/datastore :ds/file-metadata id)
            command (data/get-in-app-state :app/datastore :ds/file-metadata-editing-command)
            field-string (->> command
                              (keys)
                              (keep command-field->string-key)
                              (set)
                              (map get-string)
                              (clojure.string/join ", "))]
        (when-not (empty? command)
          ;; REVIEW: is this a good order?
          (set-title! (:kixi.datastore.metadatastore/name md))
          (utils/add-file-flag! id :metadata-saving)
          (save-file-metadata! md)
          (reset-file-edit-metadata! md)
          (reset-file-edit-metadata-command!)
          (data/swap-app-state! :app/datastore update-in [:ds/file-properties id] dissoc :update-errors)
          (activities/start-activity!
           :update-metadata
           (data/command! :kixi.datastore.metadatastore/update "1.0.0" (assoc command :kixi.datastore.metadatastore/id id))
           {:failed #(gstring/format (get-string :string.activity.update-metadata/failed)
                                     (:kixi.datastore.metadatastore/name orig-md)
                                     field-string
                                     (name (get-in % [:kixi.comms.event/payload :reason])))
            :completed #(gstring/format (get-string :string.activity.update-metadata/completed)
                                        (:kixi.datastore.metadatastore/name md)
                                        field-string)}))))))

(defmethod handle
  :reset-edit-metadata
  [event _]
  (let [current (data/get-in-app-state :app/datastore :ds/current)
        md (data/get-in-app-state :app/datastore :ds/file-metadata current)]
    (reset-file-edit-metadata! md)
    (reset-file-edit-metadata-command!)))

(defn kw-op->op-fn
  [operation path value]
  (fn [m]
    (case operation
      :dissoc (if (= 1 (count path))
                (dissoc m (first path))
                (update-in m (butlast path) dissoc (last path)))
      :assoc (assoc-in m path value)
      :update-conj (update-in m path (comp set conj) value)
      :update-disj (update-in m path (comp set disj) value))))

(defn kw-op->command-op-fn
  [operation _ value]
  (fn [old-op]
    (case operation
      :dissoc :rm ;; TODO ideally we'd remove any ':rm' values for keys that don't appear in the original metadata
      :assoc {:set value}
      :update-conj
      (let [r (if (contains? (:disj old-op) value)
                (update old-op :disj (comp set disj) value)
                (update old-op :conj (comp set conj) value))]
        (if (empty? (:disj r)) (dissoc r :disj) r))
      :update-disj
      (let [r (if (contains? (:conj old-op) value)
                (update old-op :conj (comp set disj) value)
                (update old-op :disj (comp set conj) value))]
        (if (empty? (:conj r)) (dissoc r :conj) r)))))

(defn conform-op
  [operation path value]
  (cond
    (and (= :assoc operation)
         (string? value)
         (clojure.string/blank? value))
    [:dissoc path nil]
    :else [operation path value]))

(defmethod handle
  :swap-edit-metadata
  [event args]
  (let [[operation path value] (apply conform-op args)
        op-fn (kw-op->op-fn operation path value)
        command-op-fn (kw-op->command-op-fn operation path value)
        edit-md (data/get-in-app-state :app/datastore :ds/file-metadata-editing)]
    (reset-file-edit-metadata! (op-fn edit-md))
    (data/swap-app-state! :app/datastore update-in
                          (cons :ds/file-metadata-editing-command (map md-key->update-command-key path))
                          command-op-fn)
    (data/swap-app-state! :app/datastore update
                          :ds/file-metadata-editing-command
                          utils/remove-nil-or-empty-vals)))

(defmethod handle
  :refresh-files
  [event _]
  (send-dashboard-query! (data/get-in-app-state :app/user :kixi.user/id)))

(defmethod handle
  :search-files
  [event {:keys [search]}]
  (let [mds (data/get-in-app-state :app/data-dash :items)]
    (data/swap-app-state! :app/datastore assoc :ds/files-search-filtered
                          (filter
                           #(and
                             (gstring/caseInsensitiveContains (:kixi.datastore.metadatastore/name %) search)
                             (= "stored" (:kixi.datastore.metadatastore/type %))) mds))))

(defmethod handle
  :create-datapack
  [event {:keys [datapack]}]
  (data/swap-app-state! :app/create-datapack assoc :cdp/pending? true)
  (data/swap-app-state! :app/create-datapack assoc :cdp/pending-datapack datapack)
  (let [{:keys [title]} datapack]
    (activities/start-activity!
     :create-datapack
     (data/command! :kixi.datastore/create-datapack "1.0.0"
                    {:kixi.datastore.metadatastore/name title
                     :kixi.datastore.metadatastore/id (str (random-uuid))
                     :kixi.datastore.metadatastore/type "bundle"
                     :kixi.datastore.metadatastore/bundle-type "datapack"
                     :kixi.datastore.metadatastore/bundled-ids (set (map :kixi.datastore.metadatastore/id (:selected-files datapack)))
                     :kixi.datastore.metadatastore/sharing (selected-groups->sharing-activities
                                                            (:selected-groups datapack)
                                                            (keys (data/get-in-app-state :app/datastore :dp/activities)))
                     :kixi.datastore.metadatastore/provenance {:kixi.datastore.metadatastore/source "upload"
                                                               :kixi.user/id (data/get-in-app-state :app/user :kixi.user/id)}})
     {:failed #(gstring/format (get-string :string.activity.create-datapack/failed) title)
      :completed #(gstring/format (get-string :string.activity.create-datapack/completed) title)})))

(defmethod handle
  :delete-datapack
  [event {:keys [id]}]
  (let [{:keys [kixi.datastore.metadatastore/name]} (get-local-file id)]
    (data/swap-app-state! :app/datastore update :ds/file-metadata dissoc id)
    (activities/start-activity!
     :delete-datapack
     (data/new-command! :kixi.datastore/delete-bundle "1.0.0"
                        {:kixi.datastore.metadatastore/id id})
     {:failed #(gstring/format (get-string :string.activity.delete-datapack/failed) name)
      :completed #(gstring/format (get-string :string.activity.delete-datapack/completed) name)
      :context {:name name}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti on-activity-finished
  (fn [{:keys [args]}] [(:activity args) (:result args)]))

(defmethod on-activity-finished
  :default
  [{:keys [args]}])

(defmethod on-activity-finished
  [:create-datapack :failed]
  [{:keys [args]}]
  (data/swap-app-state! :app/create-datapack assoc :cdp/pending? false)
  (data/swap-app-state!
   :app/create-datapack assoc :cdp/error
   {:gefeature/neral (case (get-in args [:message :kixi.comms.event/payload :reason])
                       :metadata-invalid (get-string :string/create-datapack-fail-invalid))}))

(defmethod on-activity-finished
  [:create-datapack :completed]
  [{:keys [args]}]
  (let [user (data/get-in-app-state :app/user)
        read-groups
        (get-in args [:message
                      :kixi.comms.event/payload
                      :kixi.datastore.metadatastore/file-metadata
                      :kixi.datastore.metadatastore/sharing
                      :kixi.datastore.metadatastore/meta-read])
        files
        (get-in args [:message
                      :kixi.comms.event/payload
                      :kixi.datastore.metadatastore/file-metadata
                      :kixi.datastore.metadatastore/bundled-ids])]
    (run!
     (fn [file-id]
       (let [md (get-local-file file-id)]
         (when (utils/user-has-edit? user md)
           (run!
            (fn [gid]
              (when-not (= gid (:kixi.user/self-group user))
                (run! #(add-group-to-file-sharing
                        %
                        {:id file-id
                         :group {:kixi.group/id gid}})
                      [:kixi.datastore.metadatastore/meta-read
                       :kixi.datastore.metadatastore/file-read]))) read-groups))))
     files))
  (js/setTimeout
   #(do
      (data/swap-app-state! :app/create-datapack assoc :cdp/pending? false)
      (data/swap-app-state! :app/create-datapack dissoc :cdp/pending-datapack)
      (route/navigate!
       :app/data
       {:id (get-in args [:message
                          :kixi.comms.event/payload
                          :kixi.datastore.metadatastore/file-metadata
                          :kixi.datastore.metadatastore/id])}
       {:new 1})) 500))

(defmethod on-activity-finished
  [:delete-datapack :completed]
  [{:keys [args]}]
  (route/navigate! :app/data-dash)
  (.info js/toastr (gstring/format (get-string :stringf/datapack-deleted) (get-in args [:context :name]))))

(defmethod on-activity-finished
  [:delete-datapack :failed]
  [{:keys [args]}]
  (log/warn "Failed to delete datapack:" args))

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
      (data/subscribe-topic :data/event-received on-event)
      (data/subscribe-topic :activity/activity-finished on-activity-finished)))
