(ns witan.ui.controllers.workspace
  (:require [schema.core :as s]
            [witan.ui.data :as data]
            [witan.gateway.schema :as wgs]
            [witan.ui.utils :as utils]
            [cljs-time.core :as t]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defn get-current-workspace
  []
  (:workspace/current (data/get-app-state :app/workspace)))


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
  (data/swap-app-state! :app/workspace-dash assoc-in [:wd/workspaces] workspaces))

(defmethod on-receive
  :workspaces/function-list
  [[_ functions]]
  (data/swap-app-state! :app/workspace assoc-in [:workspace/functions] functions))

(defmethod on-receive
  :workspaces/by-id
  [[_ returned]]
  (let [current (get-current-workspace)
        current' (if (:workspace/id current)
                   (reduce-kv (fn [a k v] (if v (assoc a k v) a)) current returned)
                   returned)
        current' (if (:workspace/id current') current' nil)]
    (data/swap-app-state! :app/workspace assoc :workspace/current current')
    (data/swap-app-state! :app/workspace assoc :workspace/pending? false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; On Route Change

(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/workspace-dash
  [_]
  ;; reset current
  (data/swap-app-state! :app/workspace assoc :workspace/pending? true)
  (data/swap-app-state! :app/workspace assoc :workspace/current nil)

  (data/query '[{:workspaces/function-list
                 [:function/name
                  :function/id
                  :function/version]}
                {(:workspaces/list-by-owner "*")
                 [:workspace/name
                  :workspace/id
                  :workspace/owner-name
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
  (fn [event args] event))

(defmethod handle :create
  [event {:keys [name desc]}]
  (let [{:keys [login/id]} (data/get-app-state :app/login)
        w-id (random-uuid)
        wsp {:workspace/name name
             :workspace/id w-id
             :workspace/description desc
             :workspace/owner-id id
             :workspace/owner-name "Me" ;; TODO
             :workspace/modified (t/now)}]
    (data/swap-app-state! :app/workspace-dash update-in [:wd/workspaces] #(conj % wsp))
    (data/swap-app-state! :app/workspace assoc :workspace/current wsp)
    (data/swap-app-state! :app/workspace assoc :workspace/pending? false)
    (route/navigate! :app/workspace {:id w-id})))
