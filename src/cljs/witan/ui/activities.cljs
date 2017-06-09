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

(def compiler-options
  {:signal extract-command-event-signal
   :reducers })

(def compiled-activities
  (zipmap available-activities
          (map #(a/compile (am/get-activity %) compiler-options) available-activities)))

(defn start-activity!
  [activity command-id])

(defn on-event
  [{:keys [args]}]
  ())

(defonce subscriptions
  (data/subscribe-topic :data/event-received on-event))
