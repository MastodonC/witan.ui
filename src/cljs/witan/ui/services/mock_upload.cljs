(ns witan.ui.services.mock-upload
  (:require [cljs.core.async :refer [put!]]
            [venue.core :as venue]
            [witan.ui.services.mock-api :as api])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-service!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod request-handler
  :upload-data
  [owner event _ result-ch]
  (put! result-ch [:success nil]))
