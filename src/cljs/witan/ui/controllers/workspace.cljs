(ns witan.ui.controllers.workspace
  (:require [schema.core :as s]
            [witan.ui.data :as data]
            [witan.gateway.schema :as wgs])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema

(def CreateWorkspace
  {:name s/Str
   (s/optional-key :description) s/Str})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-receive
  (fn [[k v]] k))

(defmethod on-receive
  :workspaces/list-by-owner
  [[_ workspaces]]
  (data/app-state-swap! :app/workspace-dash assoc-in [:wd/workspaces] workspaces))

(defmethod on-receive
  :workspaces/function-list
  [[_ functions]]
  (data/app-state-swap! :app/workspace assoc-in [:workspace/functions] functions))

(defmethod on-receive
  :workspaces/by-id
  [[_ selected]]
  (data/app-state-swap! :app/workspace assoc-in [:workspace/current] selected))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :app/workspace-dash
  [_]
  (data/query '[{:workspaces/function-list
                 [:function/name
                  :function/id
                  :function/version]}
                {(:workspaces/list-by-owner "*")
                 [:workspace/name
                  :workspace/id
                  :workspace/owner-name
                  :workspace/owner-id
                  :workspace/modified]}]
              on-receive))

(defmethod on-route-change
  :app/workspace
  [{:keys [args]}]
  (let [workspace-id (cljs.core/uuid (get-in args [:route/params :id]))
        workspace-fields (-> wgs/Workspace
                             (get "1.0")
                             (keys)
                             (vec))]
    (data/query `[{(:workspaces/by-id ~workspace-id) ~workspace-fields}] on-receive)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subscriptions

(data/subscribe-topic :data/route-changed on-route-change)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers

(defmulti handle
  (fn [event owner args] event))

(defmethod handle :create
  [event owner {:keys [name desc]}])
