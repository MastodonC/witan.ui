(ns witan.ui.activities
  (:require [witan.ui.data :as data]
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
;;
;; TODO - add tests for activities

(def available-activities
  {:upload-file [{:kixi.comms.command/key :kixi.datastore.filestore/create-upload-link}
                 {:kixi.comms.event/key   :kixi.datastore.filestore/upload-link-created}
                 {:kixi.comms.command/key :kixi.datastore.filestore/create-file-metadata}
                 (a/or
                  [{:kixi.comms.event/key  :kixi.datastore.file/created} (a/$ :completed)]
                  [{:kixi.comms.event/key  :kixi.datastore.file-metadata/rejected} (a/$ :failed)])]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn extract-command-event-signal
  [m]
  (cond
    (contains? m :kixi.comms.command/key)
    (select-keys m [:kixi.comms.command/key])
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
  [activity message]
  (if-let [command-id (:kixi.comms.command/id message)]
    (try
      (let [state (a/advance (activity-fsm activity) :pending message)]
        (data/swap-app-state! :app/activities assoc-in [:activities/pending command-id] {:activity activity
                                                                                         :state state}))
      (data/publish-topic :activity/activity-started {:message message :activity activity})
      (catch js/Error e
        (log/severe "Failed to start activity" activity "- message did not match fsm")))
    (log/severe "Failed to start activity" activity "- message did not contain a command ID")))

(defn process-event
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (loop [[command-id {:keys [activity state]}] (first activities)]
      (if (= command-id (:kixi.comms.command/id args))
        (try
          (let [{:keys [value] :as new-state} (a/advance (activity-fsm activity) state args)]
            (if (= :pending value)
              (do
                (data/swap-app-state! :app/activities assoc-in [:activities/pending command-id :state] new-state)
                (data/publish-topic :activity/activity-progressed {:message args :activity activity}))
              (do
                (data/swap-app-state! :app/activities update :activities/pending dissoc command-id)
                (data/publish-topic :activity/activity-finished {:message args :activity activity :result value}))))
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
            (do
              (data/swap-app-state! :app/activities update :activities/pending dissoc command-id)
              (data/publish-topic :activity/activity-finished {:message args :activity activity :result value}))))
        (catch js/Error e
          (when (next activities)
            (recur (next activities))))))))

(defonce subscriptions
  (do (data/subscribe-topic :data/event-received process-event)
      (data/subscribe-topic :data/command-sent process-command)))
