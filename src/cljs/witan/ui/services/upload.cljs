(ns witan.ui.services.upload
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [witan.ui.util :as util]
            [venue.core :as venue]
            [s3-beam.client :as s3])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.macros :as wm]
                   [cljs.core.async.macros :refer [go]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-service!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn local-sign
  [payload _ file ch]
  (put! ch {:f file :signature payload})
  (close! ch))

(defmethod request-handler
  :upload-data
  [owner event {:keys [file s3-beam-payload] :as payload} result-ch]
  (let [uploaded (chan 20)]
    (go
      (with-redefs [s3-beam.client/sign-file (partial local-sign s3-beam-payload)]
        (put! (s3/s3-pipe uploaded) file)
        (put! result-ch [(<! uploaded) nil])))))
