(ns witan.ui.activities
  (:require
   [automat.core :as a]))

(defn upload-file-activity
  []
  (->> [{:kixi.comms.command/key :kixi.datastore.filestore/create-upload-link}
        {:kixi.comms.event/key   :kixi.datastore.filestore/upload-link-created}
        {:kixi.comms.command/key :kixi.datastore.filestore/create-file-metadata}
        (a/or
         [{:kixi.comms.event/key  :kixi.datastore.file/created} (a/$ :completed)]
         [{:kixi.comms.event/key  :kixi.datastore.file-metadata/rejected} (a/$ :failed)])]
       (a/precompile)))

(def activities
  {:upload-file upload-file-activity})

(defmacro get-activity
  [activity]
  (when-let [afn (get activities activity)]
    (afn)))
