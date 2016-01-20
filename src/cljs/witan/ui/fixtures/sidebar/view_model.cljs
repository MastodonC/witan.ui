(ns ^:figwheel-always witan.ui.fixtures.sidebar.view-model
    (:require [cljs.core.async :refer [<! chan]]
              [om.core :as om :include-macros true]
              [schema.core :as s :include-macros true]
              [witan.ui.util :as util]
              [witan.ui.services.data :as data]
              [venue.core :as venue :include-macros true])
    (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]]
                     [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

(wm/create-standard-view-model! {})

(defmethod event-handler
  :event/logout
  [owner _ args cursor]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :logout}))
