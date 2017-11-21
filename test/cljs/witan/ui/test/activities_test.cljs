(ns witan.ui.test.activities-test
  (:require  [cljs.test :refer-macros [deftest is testing async]]
             [reagent.core :as r]
             [witan.ui.test.base :as b]
             [witan.ui.activities :as a]))

(deftest extract-command-event-signal-test
  (is (= {:kixi.comms.command/key :foo} (a/extract-command-event-signal {:kixi.comms.command/key :foo})))
  (is (= {:kixi.comms.event/key :foo} (a/extract-command-event-signal {:kixi.comms.event/key :foo})))
  (is (= {:kixi.comms.event/key :foo
          :kixi.comms.event/payload {:kixi.datastore.communication-specs/file-metadata-update-type :bar}}
         (a/extract-command-event-signal {:kixi.comms.event/key :foo
                                          :kixi.comms.event/payload {:kixi.datastore.communication-specs/file-metadata-update-type :bar}})))
  (is (nil? (a/extract-command-event-signal {:kixi/key :foo}))))

(def reporters {:failed #(str "YOU FAILED1!")
                :completed #(str "YOU COMPLETED1!")})

(defn is-pending-state?
  ([id n]
   (let [s (b/get-data :app/activities :activities/pending id)]
     (is s)
     (is (= :upload-file (:activity s)))
     (is (= n (get-in s [:state :state-index])))
     (is (= :pending (get-in s [:state :value])))))
  ([old-id id n]
   (let [old-s (b/get-data :app/activities :activities/pending old-id)]
     (is (nil? old-s))
     (is-pending-state? id n))))

(defn is-x-state?
  [result-key]
  (let [s (last (b/get-data :app/activities :activities/log))]
    (is s)
    (is (= :upload-file (:activity s)))
    (is (= result-key (:status s)))
    (is (= ((result-key reporters)) (:message s)))))

(def is-completed-state? (partial is-x-state? :completed))
(def is-failed-state? (partial is-x-state? :failed))

(deftest normal-completion-test-upload-file
  (testing "completion"
    (let [[u u2 u3] (repeatedly 3 random-uuid)]
      (a/start-activity! :upload-file {:kixi.command/type :kixi.datastore.filestore/initiate-file-upload
                                       :kixi.command/id  u}
                         reporters)
      (is-pending-state? u 1)
      ;;
      (a/process-event {:args
                        {:kixi.event/type :kixi.datastore.filestore/file-upload-initiated
                         :kixi.command/id u}})
      (is-pending-state? u 2)
      ;;
      (a/process-command {:args
                          {:kixi.command/type :kixi.datastore.filestore/complete-file-upload
                           :kixi.command/id u2}})
      (is-pending-state? u u2 3)
      ;;
      (a/process-event {:args
                        {:kixi.event/type :kixi.datastore.filestore/file-upload-completed
                         :kixi.command/id u2}})
      (is-pending-state? u2 4)
      ;;
      (a/process-command {:args
                          {:kixi.comms.command/key :kixi.datastore.filestore/create-file-metadata
                           :kixi.comms.command/id u3}})
      (is-pending-state? u u3 5)
      ;;
      (a/process-event {:args
                        {:kixi.comms.event/key :kixi.datastore.file/created
                         :kixi.comms.command/id u3}})
      (is-completed-state?)))

  (testing "failure1"
    (let [u (random-uuid)]
      (a/start-activity! :upload-file {:kixi.command/type :kixi.datastore.filestore/initiate-file-upload
                                       :kixi.command/id  u}
                         reporters)
      (is-pending-state? u 1)
      ;;
      (a/process-event {:args
                        {:kixi.event/type :kixi.datastore.filestore/file-upload-failed
                         :kixi.command/id u}})
      (is-failed-state?)))

  (testing "failure2"
    (let [[u u2] (repeatedly 2 random-uuid)]
      (a/start-activity! :upload-file {:kixi.command/type :kixi.datastore.filestore/initiate-file-upload
                                       :kixi.command/id  u}
                         reporters)
      (is-pending-state? u 1)
      ;;
      (a/process-event {:args
                        {:kixi.event/type :kixi.datastore.filestore/file-upload-initiated
                         :kixi.command/id u}})
      (is-pending-state? u 2)
      ;;
      (a/process-command {:args
                          {:kixi.command/type :kixi.datastore.filestore/complete-file-upload
                           :kixi.command/id u2}})
      (is-pending-state? u u2 3)
      ;;
      (a/process-event {:args
                        {:kixi.event/type :kixi.datastore.filestore/file-upload-rejected
                         :kixi.command/id u2}})
      (is-failed-state?))))
