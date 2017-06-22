(ns witan.ui.activities
  (:require [witan.ui.data :as data]
            [witan.ui.time :as t]
            [automat.core :as a]
            [cljs.core.async :refer [chan put! close!]])
  (:require-macros [cljs-log.core :as log]))

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
  {:upload-file [{:kixi.comms.command/key  :kixi.datastore.filestore/create-upload-link}
                 {:kixi.comms.event/key    :kixi.datastore.filestore/upload-link-created}
                 {:kixi.comms.command/key  :kixi.datastore.filestore/create-file-metadata}
                 (a/or
                  [{:kixi.comms.event/key  :kixi.datastore.file/created} (a/$ :completed)]
                  [{:kixi.comms.event/key  :kixi.datastore.file-metadata/rejected} (a/$ :failed)])]
   ;;
   :update-metadata [{:kixi.comms.command/key  :kixi.datastore.metadatastore/update}
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
                     [{:kixi.comms.event/key  :kixi.datastore.metadatastore/sharing-change-rejected} (a/$ :failed)])]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-command-event-signal
  [m]
  (cond
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
  [activity message reporters]
  (assert (every? fn? (vals reporters)))
  (if-let [command-id (:kixi.comms.command/id message)]
    (try
      (let [state (a/advance (activity-fsm activity) :pending message)
            id (random-uuid)]
        (data/swap-app-state! :app/activities assoc-in [:activities/pending command-id] {:activity activity
                                                                                         :state state
                                                                                         :reporters reporters
                                                                                         :id id})
        (data/publish-topic :activity/activity-started {:message message :activity activity})
        (log/debug "Started activity" activity id))
      (catch js/Error e
        (log/severe "Failed to start activity" activity "- message did not match fsm")))
    (log/severe "Failed to start activity" activity "- message did not contain a command ID")))

(defn- finish-activity!
  [final-message result command-id]
  (let [{:keys [activity reporters id]} (data/get-in-app-state :app/activities :activities/pending command-id)
        reporter (get reporters result)]
    (data/swap-app-state! :app/activities update :activities/pending dissoc command-id)
    (data/swap-app-state! :app/activities update :activities/log conj {:status result
                                                                       :message (when reporter (reporter final-message))
                                                                       :time (t/jstime->str)})
    (data/publish-topic :activity/activity-finished {:message final-message :activity activity :result result})
    (log/debug "Finished activity" activity id)
    (js/setTimeout #(data/save-data!) 1000)))

(defn process-event
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (loop [[command-id {:keys [activity state]}] (first activities)]
      (if (= command-id (:kixi.comms.command/id args))
        (try
          (log/debug "TRYING" args)
          (let [{:keys [value] :as new-state} (a/advance (activity-fsm activity) state args)]
            (if (= :pending value)
              (do
                (data/swap-app-state! :app/activities assoc-in [:activities/pending command-id :state] new-state)
                (data/publish-topic :activity/activity-progressed {:message args :activity activity}))
              (finish-activity! args value command-id)))
          (catch js/Error e))
        (when (next activities)
          (recur (next activities)))))))

(defn process-command
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (loop [[command-id {:keys [activity state]}] (first activities)]
      (try
        (let [new-command-id (:kixi.comms.command/id args)
              {:keys [value] :as new-state} (a/advance (activity-fsm activity) state args)]
          (if (= :pending value)
            (let [existing (data/get-in-app-state :app/activities :activities/pending command-id)]
              (data/swap-app-state! :app/activities assoc-in [:activities/pending new-command-id]
                                    (assoc existing :state new-state))
              (data/swap-app-state! :app/activities update :activities/pending dissoc command-id)
              (data/publish-topic :activity/activity-progressed {:message args :activity activity}))
            (finish-activity! args value command-id)))
        (catch js/Error e
          (when (next activities)
            (recur (next activities))))))))

(defonce subscriptions
  (do (data/subscribe-topic :data/event-received process-event)
      (data/subscribe-topic :data/command-sent process-command)))
