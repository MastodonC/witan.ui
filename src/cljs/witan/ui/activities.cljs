(ns witan.ui.activities
  (:require [witan.ui.data :as data]
            [witan.ui.time :as t]
            [automat.core :as a]
            [cljs.core.async :refer [chan put! close!]])
  (:require-macros [cljs-log.core :as log]))

(def save-timeout (atom nil))

(defn debounce-save!
  []
  (when @save-timeout
    (js/clearTimeout @save-timeout))
  (reset! save-timeout (js/setTimeout #(do
                                         (data/save-data!)
                                         (reset! save-timeout nil)) 1000)))

;; This system is designed to use FSMs to pattern match against 'activities'.
;; Activities are high-level user operations such as uploading a file, changing some metadata etc.
;; Currently, activities must be kicked off manually so that the system knows where to begin looking
;; for the next state to occurr. A more passive approach would be to save the last n messages
;; and constantly pattern match against the flow, but I worry this would be expensive and introduce
;; a degree of false-positives.
;;
;; There are some gotchas;
;; - Activities, right now, must start with a command.
;; - Where ever an activity includes an event followed by a command, the new command will
;;   introduce a *new* command ID. At this point the new command has no data connection
;;   to the previous event so we just cross our fingers and hope for the best. When designing
;;   activities, be aware of commands that appear in existing activities as this could occurr.
;;   The code will simply give the new state to the first FSM it comes across that's expecting
;;   that command, so long as the activity is pending.
;;   Note: Adjust `extract-command-event-signal` to further select messages

(def available-activities
  {:update-metadata [{:kixi.comms.command/key  :kixi.datastore.metadatastore/update}
                     (a/or
                      [{:kixi.comms.event/key  :kixi.datastore.file-metadata/updated
                        :kixi.comms.event/payload {:kixi.datastore.communication-specs/file-metadata-update-type :kixi.datastore.communication-specs/file-metadata-update}}
                       (a/$ :completed)]
                      [{:kixi.comms.event/key  :kixi.datastore.metadatastore/update-rejected} (a/$ :failed)])]
   ;;
   :update-sharing [{:kixi.comms.command/key  :kixi.datastore.metadatastore/sharing-change}
                    (a/or
                     [{:kixi.comms.event/key  :kixi.datastore.file-metadata/updated
                       :kixi.comms.event/payload {:kixi.datastore.communication-specs/file-metadata-update-type :kixi.datastore.communication-specs/file-metadata-sharing-updated}}
                      (a/$ :completed)]
                     [{:kixi.comms.event/key  :kixi.datastore.metadatastore/sharing-change-rejected} (a/$ :failed)])]
   ;;
   :create-datapack [{:kixi.comms.command/key  :kixi.datastore/create-datapack}
                     (a/or
                      [{:kixi.comms.event/key  :kixi.datastore.file-metadata/rejected} (a/$ :failed)]
                      [{:kixi.comms.event/key  :kixi.datastore.file-metadata/updated
                        :kixi.comms.event/payload {:kixi.datastore.communication-specs/file-metadata-update-type :kixi.datastore.communication-specs/file-metadata-created}} (a/$ :completed)])]
   ;; NEW style
   ;;
   :delete-datapack [{:kixi.command/type :kixi.datastore/delete-bundle}
                     (a/or
                      [{:kixi.event/type :kixi.datastore/bundle-delete-rejected} (a/$ :failed)]
                      [{:kixi.event/type :kixi.datastore/bundle-deleted} (a/$ :completed)])]
   :remove-file-from-datapack [{:kixi.command/type :kixi.datastore/remove-files-from-bundle}
                               (a/or
                                [{:kixi.event/type :kixi.datastore/files-remove-from-bundle-rejected} (a/$ :failed)]
                                [{:kixi.event/type :kixi.datastore/files-removed-from-bundle} (a/$ :completed)])]
   :add-file-to-datapack [{:kixi.command/type :kixi.datastore/add-files-to-bundle}
                          (a/or
                           [{:kixi.event/type :kixi.datastore/files-add-to-bundle-rejected} (a/$ :failed)]
                           [{:kixi.event/type :kixi.datastore/files-added-to-bundle} (a/$ :completed)])]
   :add-collect-files-to-datapack [{:kixi.command/type :kixi.datastore/add-files-to-bundle}
                                   (a/or
                                    [{:kixi.event/type :kixi.datastore/files-add-to-bundle-rejected} (a/$ :failed)]
                                    [{:kixi.event/type :kixi.datastore/files-added-to-bundle} (a/$ :completed)])]
   :upload-file [{:kixi.command/type :kixi.datastore.filestore/initiate-file-upload}
                 (a/or
                  {:kixi.event/type :kixi.datastore.filestore/file-upload-initiated}
                  [{:kixi.event/type :kixi.datastore.filestore/file-upload-failed} (a/$ :failed)])
                 {:kixi.command/type :kixi.datastore.filestore/complete-file-upload}
                 (a/or
                  {:kixi.event/type :kixi.datastore.filestore/file-upload-completed}
                  [{:kixi.event/type :kixi.datastore.filestore/file-upload-rejected} (a/$ :failed)])
                 {:kixi.comms.command/key  :kixi.datastore.filestore/create-file-metadata}
                 (a/or
                  [{:kixi.comms.event/key  :kixi.datastore.file/created} (a/$ :completed)]
                  [{:kixi.comms.event/key  :kixi.datastore.file-metadata/rejected} (a/$ :failed)])]
   :delete-file [{:kixi.command/type :kixi.datastore/delete-file}
                 (a/or
                  [{:kixi.event/type :kixi.datastore/file-deleted} (a/$ :completed)]
                  [{:kixi.event/type :kixi.datastore/file-delete-rejected} (a/$ :failed)])]
   :send-collect-request [{:kixi.command/type :kixi.collect/request-collection}
                          (a/or
                           [{:kixi.event/type :kixi.collect/collection-requested} (a/$ :completed)]
                           [{:kixi.event/type :kixi.collect/collection-request-rejected} (a/$ :failed)])]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-command-event-signal
  [m]
  (cond
    ;; new commands
    (contains? m :kixi.command/type)
    (select-keys m [:kixi.command/type])
    ;; new events
    (contains? m :kixi.event/type)
    (select-keys m [:kixi.event/type])

    ;; commands
    (contains? m :kixi.comms.command/key)
    (select-keys m [:kixi.comms.command/key])
    ;; events with `file-metadata-update-type` in the payload
    (and (contains? m :kixi.comms.event/key)
         (contains? m :kixi.comms.event/payload)
         (contains? (:kixi.comms.event/payload m) :kixi.datastore.communication-specs/file-metadata-update-type))
    (merge (select-keys m [:kixi.comms.event/key])
           {:kixi.comms.event/payload (select-keys (:kixi.comms.event/payload m) [:kixi.datastore.communication-specs/file-metadata-update-type])})
    ;; events
    (contains? m :kixi.comms.event/key)
    (select-keys m [:kixi.comms.event/key])))

(defn completed-reducer
  [state input] :completed)

(defn failed-reducer
  [state input] :failed)

(def compiler-options
  {:signal extract-command-event-signal
   :reducers {:completed completed-reducer
              :failed failed-reducer}})

(def compiled-activities
  (zipmap (keys available-activities)
          (map #(a/compile % compiler-options) (vals available-activities))))

(defn activity-fsm
  [a]
  (get compiled-activities a))

(defn start-activity!
  [activity message {:keys [context] :as opts}]
  (let [reporters (select-keys opts [:failed :completed])]
    (assert (every? fn? (vals reporters)))
    (if-let [command-id (or (:kixi.comms.command/id message)
                            (:kixi.command/id message))]
      (try
        (let [state (a/advance (activity-fsm activity) :pending message)
              id (str (random-uuid))]
          (data/swap-app-state! :app/activities assoc-in [:activities/pending id] {:activity activity
                                                                                   :id id
                                                                                   :state state
                                                                                   :reporters reporters
                                                                                   :context context
                                                                                   :command-id command-id})
          (data/publish-topic :activity/activity-started {:message message :activity activity})
          (log/debug "Started activity" activity id)
          id)
        (catch js/Error e
          (log/severe "Failed to start activity" activity "- message did not match fsm")))
      (log/severe "Failed to start activity" activity "- message did not contain a command ID"))))

(defn- finish-activity!
  [final-message result id]
  (let [{:keys [activity reporters context]}
        (data/get-in-app-state :app/activities :activities/pending id)
        reporter (get reporters result)
        log-message (when reporter (reporter final-message))]
    (data/swap-app-state! :app/activities update :activities/pending dissoc id)
    (data/swap-app-state! :app/activities update :activities/log conj {:activity activity
                                                                       :status result
                                                                       :message log-message
                                                                       :time (t/jstime->str)})
    (data/publish-topic :activity/activity-finished {:log log-message
                                                     :message final-message
                                                     :activity activity
                                                     :result result
                                                     :context context})
    (log/debug "Finished activity" activity id)
    (debounce-save!)))

(defn abandon-activity!
  [activity-name id]
  (let [{:keys [activity] :as app-state}
        (data/get-in-app-state :app/activities :activities/pending id)]
    (when (and app-state (= activity-name activity))
      (finish-activity! nil :failed id))))

(defn process-event
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (loop [activities' activities]
      (let [[id {:keys [command-id activity state]}] (first activities')]
        (if (= command-id (or (:kixi.comms.command/id args)
                              (:kixi.command/id args)))
          (try
            (let [{:keys [value] :as new-state} (a/advance (activity-fsm activity) state args)]
              (if (= :pending value)
                (do
                  (data/swap-app-state! :app/activities assoc-in [:activities/pending id :state] new-state)
                  (let [updated-activity (data/get-in-app-state :app/activities :activities/pending id)]
                    (data/publish-topic :activity/activity-progressed {:message args
                                                                       :activity updated-activity})))
                (finish-activity! args value id)))
            (catch js/Error e))
          (when (next activities')
            (recur (next activities'))))))))

(defn process-command
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (loop [activities' activities]
      (try
        (let [[id {:keys [command-id activity state]}] (first activities')
              new-command-id (or (:kixi.comms.command/id args)
                                 (:kixi.command/id args))
              {:keys [value] :as new-state} (a/advance (activity-fsm activity) state args)]
          (if (= :pending value)
            (do
              (data/swap-app-state! :app/activities update-in [:activities/pending id]
                                    (fn [x]
                                      (assoc x :state new-state :command-id new-command-id)))
              (let [updated-activity (data/get-in-app-state :app/activities :activities/pending id)]
                (data/publish-topic :activity/activity-progressed {:message args
                                                                   :activity updated-activity})))
            (finish-activity! args value id)))
        (catch js/Error e
          (when (next activities')
            (recur (next activities'))))))))

(defonce subscriptions
  (do (data/subscribe-topic :data/event-received process-event)
      (data/subscribe-topic :data/command-sent process-command)))
