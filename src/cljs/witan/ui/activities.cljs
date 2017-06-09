(ns witan.ui.activities
  (:require [witan.ui.data :as data]
            [automat.core :as a])
  (:require-macros [witan.ui.activities :as am]))

(def available-activities
  #{:upload-file})

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
  (zipmap available-activities
          (map #(a/compile (am/get-activity %) compiler-options) available-activities)))

(defn start-activity!
  [activity command-id]
  (data/swap-app-state! :app/activities assoc-in [:activities/pending command-id :activity] activity))

(defn process-incoming
  [{:keys [args]}]
  (let [activities (data/get-in-app-state :app/activities :activities/pending)]
    (for [[command-id activity] activities]
      (when (= command-id (:kixi.comms.command/id args))
        (let [compiled-activity (get compiled-activities activity)])))))

(defonce subscriptions
  (do (data/subscribe-topic :data/event-received process-incoming)
      (data/subscribe-topic :data/command-sent process-incoming)))
