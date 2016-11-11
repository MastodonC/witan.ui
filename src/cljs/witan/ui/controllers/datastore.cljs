(ns witan.ui.controllers.datastore
  (:require [schema.core :as s]
            [ajax.core :as ajax]
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :search-schema
  [event {:keys [search]}]
  (data/swap-app-state! :app/datastore assoc :schema/search-results
                        [{:schema/name "Net New Dwellings"
                          :schema/author {:group/name "GLA Demography"
                                          :group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                                          :group/type :group}
                          :schema/id "a74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"}
                         {:schema/name "Test Schema"
                          :schema/author {:group/name "GLA Demography"
                                          :group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                                          :group/type :group}
                          :schema/id "a74f742d-9cb9-4ede-aeaf-f82aa4b6f310"}]))
