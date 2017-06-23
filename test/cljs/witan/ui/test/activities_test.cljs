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

(deftest normal-completion-test
  (let [u (random-uuid)
        u2 (random-uuid)
        reporters {:failed #(str "YOU FAILED1!")
                   :completed #(str "YOU COMPLETED1!")}]
    (testing "completion"
      (a/start-activity! :upload-file {:kixi.comms.command/key :kixi.datastore.filestore/create-upload-link
                                       :kixi.comms.command/id  u}
                         reporters)
      (let [s (b/get-data :app/activities :activities/pending u)]
        (is s)
        (is (= 1 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-event {:args
                        {:kixi.comms.event/key :kixi.datastore.filestore/upload-link-created
                         :kixi.comms.command/id u}})
      (let [s (b/get-data :app/activities :activities/pending u)]
        (is s)
        (is (= 2 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-command {:args
                          {:kixi.comms.command/key :kixi.datastore.filestore/create-file-metadata
                           :kixi.comms.command/id u2}})
      (let [old-s (b/get-data :app/activities :activities/pending u)
            s (b/get-data :app/activities :activities/pending u2)]
        (is (nil? old-s))
        (is s)
        (is (= 3 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-event {:args
                        {:kixi.comms.event/key :kixi.datastore.file/created
                         :kixi.comms.command/id u2}})
      (let [s (last (b/get-data :app/activities :activities/log))]
        (is s)
        (is (= :completed (:status s)))
        (is (= ((:completed reporters)) (:message s))))))

  (let [u (random-uuid)
        u2 (random-uuid)
        reporters {:failed #(str "YOU FAILED2!")
                   :completed #(str "YOU COMPLETED2!")}]
    (testing "failure"
      (a/start-activity! :upload-file {:kixi.comms.command/key :kixi.datastore.filestore/create-upload-link
                                       :kixi.comms.command/id  u}
                         reporters)
      (let [s (b/get-data :app/activities :activities/pending u)]
        (is s)
        (is (= 1 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-event {:args
                        {:kixi.comms.event/key :kixi.datastore.filestore/upload-link-created
                         :kixi.comms.command/id u}})
      (let [s (b/get-data :app/activities :activities/pending u)]
        (is s)
        (is (= 2 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-command {:args
                          {:kixi.comms.command/key :kixi.datastore.filestore/create-file-metadata
                           :kixi.comms.command/id u2}})
      (let [old-s (b/get-data :app/activities :activities/pending u)
            s (b/get-data :app/activities :activities/pending u2)]
        (is (nil? old-s))
        (is s)
        (is (= 3 (get-in s [:state :state-index])))
        (is (= :pending (get-in s [:state :value])))
        (is (= :upload-file (:activity s))))
      ;;
      (a/process-event {:args
                        {:kixi.comms.event/key :kixi.datastore.file-metadata/rejected
                         :kixi.comms.command/id u2}})
      (let [s (last (b/get-data :app/activities :activities/log))]
        (is s)
        (is (= :failed (:status s)))
        (is (= ((:failed reporters)) (:message s)))))
    ;;
    (is (= 2 (count (b/get-data :app/activities :activities/log))))))
