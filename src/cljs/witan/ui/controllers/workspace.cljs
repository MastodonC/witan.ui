(ns witan.ui.controllers.workspace
  (:require [witan.ui.ajax :refer [command! query validate-receipt!]]
            [schema.core :as s]
            [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CreateWorkspace
  {:name s/Str
   (s/optional-key :description) s/Str})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defmethod api-response [:create :success]
  [{:keys [owner]} response]
  (validate-receipt! response (fn [status receipt]
                                (log/debug "OMG!!!" status receipt))))

(defmethod api-response [:create :failure]
  [{:keys [owner]} response]
  (log/debug "Failed to create workspace")
  (om/transact! owner '[(cw/set-pending! {:pending? false})
                        (cw/set-message! {:message :string/create-workspace-error})]))

(defn route-api-response
  [event owner]
  (fn [status response]
    (api-response {:owner owner :event event :status status} response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event owner args] event))

(defmethod handle :create
  [event owner {:keys [name desc]}]
  (om/transact! owner '[(cw/set-pending! {:pending? true})])
  (let [args (merge {:name name} (when desc {:description desc}))]
    (command! "workspace/create" "1.0"
              {:id event
               :params (s/validate CreateWorkspace args)
               :result-cb (route-api-response event owner)})))
