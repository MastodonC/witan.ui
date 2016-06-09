(ns witan.ui.controllers.workspace
  (:require [witan.ui.ajax :refer [GET POST endpoint]]
            [schema.core :as s]
            [om.next :as om]
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
  (log/debug "CREATE SUCCESS"))

(defmethod api-response [:create :failure]
  [{:keys [owner]} response]
  (log/debug "Failed to create workspace")
  (om/transact! owner '[(cw/set-message! {:message :string/sign-in-failure})]))

(defn route-api-response
  [event owner]
  (fn [status response]
    (api-response {:owner owner :event event :status status} response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn method
  [method]
  (str endpoint "/workspaces/" method))

(defmulti handle
  (fn [event owner args] event))

(defmethod handle :create
  [event owner {:keys [name desc]}]
  (let [args (merge {:name name} (when desc {:description desc}))]
    (POST (method "create")
          {:id event
           :params (s/validate CreateWorkspace args)
           :result-cb (route-api-response event owner)})))
