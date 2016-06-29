(ns witan.ui.controllers.workspace
  (:require [schema.core :as s]
            [witan.ui.data :as data]
            [witan.gateway.schema :as wgs]
            [witan.ui.utils :as utils]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def dash-query-pending? (atom false))

(defn get-current-workspace
  []
  (:workspace/current (data/get-app-state :app/workspace)))

(defn get-workspaces
  []
  (:wd/workspaces (data/get-app-state :app/workspace-dash)))

(defn ->transport
  [m]
  (-> m
      #_(update :workspace/modified utils/jstime->str)
      (dissoc :workspace/local)))

(defn find-workspace-by-id
  [coll id]
  (some #(when (= id (:workspace/id %)) %) coll))

(defn send-dashboard-query!
  [id on-receive]
  (when-not @dash-query-pending?
    (reset! dash-query-pending? true)
    #_{:workspaces/function-list
       [:function/name
        :function/id
        :function/version]}
    (data/query `[{(:workspaces/list-by-owner ~id)
                   [:workspace/name
                    :workspace/id
                    :workspace/owner-name
                    :workspace/modified]}]
                on-receive)))

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
  ;; TODO this needs to be way more intelligent, and use modified time stamps to
  ;; select most recent version. We don't want to accidentally overwrite local
  ;; changes.
  (let [existing-workspaces (get-workspaces)
        local-only (->> existing-workspaces
                        (remove #(find-workspace-by-id workspaces (:workspace/id %)))
                        (map #(assoc % :workspace/local true)))
        all  (concat (->> workspaces
                          (map #(assoc % :workspace/local false)))
                     local-only)
        all' (sort-by :workspace/name all)]
    (data/swap-app-state! :app/workspace-dash assoc-in [:wd/workspaces] all'))
  (reset! dash-query-pending? false))

(defmethod on-receive
  :workspaces/function-list
  [[_ functions]]
  (data/swap-app-state! :app/workspace assoc-in [:workspace/functions] functions))

(defmethod on-receive
  :workspaces/by-id
  [[_ returned]]
  (let [current (get-current-workspace)
        ;; merge the returned version into a local version
        current' (if (:workspace/id current)
                   (reduce-kv (fn [a k v] (if v (assoc a k v) a)) current returned) ;; TODO this merging is too crude
                   returned)
        ;; if we're still null, find one from local cache
        current' (if (:workspace/id current') current'
                     (find-workspace-by-id
                      (get-workspaces)
                      (uuid (:id (:route/params (data/get-app-state :app/route))))))]
    (log/info (if current' (str "Loading workspace: " current') (str "No workspace found.")))
    (data/swap-app-state! :app/workspace assoc :workspace/current current')
    (data/swap-app-state! :app/workspace assoc :workspace/pending? false)
    (when (and current' (not= current' returned))
      (data/command! :workspace/save "1.0" {:workspace/to-save (->transport current')}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Subscriptions


(defmulti on-route-change
  (fn [{:keys [args]}] (:route/path args)))

(defmethod on-route-change
  :default [_])

(defmethod on-route-change
  :app/workspace
  [{:keys [args]}]
  (let [workspace-id (cljs.core/uuid (get-in args [:route/params :id]))
        workspace-fields (-> wgs/Workspace
                             (get "1.0")
                             (keys)
                             (vec))]
    (data/query `[{(:workspaces/by-id ~workspace-id) ~workspace-fields}] on-receive)))

(defmethod on-route-change
  :app/workspace-dash
  [_]
  ;; reset current
  (data/swap-app-state! :app/workspace assoc :workspace/pending? true)
  (data/swap-app-state! :app/workspace assoc :workspace/current nil)
  (if-let [id (:user/id (data/get-app-state :app/user))]
    (send-dashboard-query! id on-receive)))

(defn on-user-logged-in
  [{:keys [args]}]
  (let [{:keys [user/id]} args
        {:keys [route/path]} (data/get-app-state :app/route)]
    (when (= path :app/workspace-dash)
      (send-dashboard-query! id on-receive))))

(defonce subscriptions
  (do (data/subscribe-topic :data/route-changed on-route-change)
      (data/subscribe-topic :data/user-logged-in on-user-logged-in)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers

(defmulti handle
  (fn [event args] event))

(defmethod handle :create
  [event {:keys [name desc]}]
  (let [{:keys [user/id]} (data/get-app-state :app/user)
        w-id (random-uuid)
        wsp (wgs/validate-workspace
             "1.0"
             {:workspace/name name
              :workspace/id w-id
              :workspace/description desc
              :workspace/owner-id id
              :workspace/owner-name "Me" ;; TODO
              :workspace/modified (utils/jstime->str (t/now))})]
    (data/swap-app-state! :app/workspace-dash update-in [:wd/workspaces] #(conj % wsp))
    (data/swap-app-state! :app/workspace assoc :workspace/current wsp)
    (data/swap-app-state! :app/workspace assoc :workspace/pending? false)
    (route/navigate! :app/workspace {:id w-id})))
