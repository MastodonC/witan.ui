(ns witan.ui.controllers.workspace
  (:require [schema.core :as s]
            [ajax.core :as ajax]
            [witan.ui.data :as data]
            [witan.gateway.schema :as wgs]
            [witan.ui.utils :as utils]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [witan.ui.route :as route]
            [cljsjs.filesaverjs])
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
    #_{:workspace/function-list
       [:function/name
        :function/id
        :function/version]}
    (data/query `[{(:workspace/list-by-owner ~id)
                   [:workspace/name
                    :workspace/id
                    :workspace/owner-name
                    :workspace/modified]}]
                on-receive)))

(defn set-result-downloading!
  ([result dling?]
   (set-result-downloading!
    (data/get-app-state :app/workspace)
    result
    dling?))
  ([workspace result dling?]
   (let [{:keys [workspace/current-results]} workspace
         cr' (vec (-> (remove #{result} current-results)
                      (conj (assoc result :result/downloading? dling?))))]
     (data/swap-app-state! :app/workspace assoc :workspace/current-results cr'))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema

(def CreateWorkspace
  {:name s/Str
   (s/optional-key :description) s/Str})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Event

(defmulti on-event
  (fn [{:keys [args]}] [(:event/key args) (:event/version args)]))

(defmethod on-event
  [:workspace/saved "1.0.0"]
  [{event :args}]
  (log/debug "Workspace" (get-in event [:event/params :workspace/id]) "was saved."))

(defmethod on-event
  [:workspace/run-failed "1.0.0"]
  [{event :args}]
  (log/warn "Workspace failed to run" (get-in event [:event/params :workspace/id])))

(defmethod on-event
  [:workspace/started-running "1.0.0"]
  [{event :args}]
  (data/swap-app-state! :app/workspace assoc :workspace/running? true)
  (log/info "Workspace is running" (get-in event [:event/params :workspace/id])))

(defmethod on-event
  [:workspace/finished-with-results "1.0.0"]
  [{event :args}]
  (let [results (get-in event [:event/params :workspace/results])
        make-result-fn (fn [[k v]] {:result/key k
                                    :result/location v
                                    :result/downloading? false})]
    (log/debug "Got new results:" results)
    (data/swap-app-state! :app/workspace assoc
                          :workspace/current-results (mapv make-result-fn results)
                          :workspace/running? false)))

(defmethod on-event
  [:workspace/finished-with-errors "1.0.0"]
  [{event :args}]
  (let [error (get-in event [:event/params :error])]
    (log/severe "Workspace returned an error:" error)
    (data/swap-app-state! :app/workspace assoc :workspace/running? false)))

(defmethod on-event
  [:workspace/result-url-created "1.0.0"]
  [{event :args}]
  (let [{:keys [workspace/result-url
                workspace/original-location]} (:event/params event)
        wsp                                   (data/get-app-state :app/workspace)
        original-result                       (some #(when (= original-location (:result/location %)) %)
                                                    (:workspace/current-results wsp))
        filename                              (str (name (:result/key original-result)) ".csv")]
    (when original-result
      (let [handler (fn [response]
                      (log/debug "Saving CSV as" filename)
                      (js/saveAs
                       (js/Blob. #js [response] #js {:type "text/csv;charset=utf-8"})
                       filename)
                      (set-result-downloading! wsp original-result false))
            error-handler (fn [response]
                            (set-result-downloading! wsp original-result false)
                            (log/severe response))]
        (ajax/GET result-url  {:handler handler
                               :error-handler error-handler})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-receive
  (fn [[k v]] k))

(defmethod on-receive
  :error
  [[_ error-query]]
  (log/debug "Workspace controller acknowledges error:" error-query))

(defmethod on-receive
  :workspace/list-by-owner
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
  :workspace/available-functions
  [[_ functions]]
  (data/swap-app-state! :app/workspace assoc-in [:workspace/functions] functions))

(defmethod on-receive
  :workspace/by-id
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
      (data/command! :workspace/save "1.0.0" {:workspace/to-save (->transport current')}))))

(defmethod on-receive
  :workspace/available-models
  [[_ models]]
  (data/swap-app-state! :app/workspace assoc :workspace/model-list models))

(defmethod on-receive
  :workspace/model-by-name-and-version
  [[_ {:keys [workflow catalog metadata]}]]
  (let [{:keys [witan/name witan/version]} metadata
        ml (:workspace/model-list (data/get-app-state :app/workspace))
        ml' (map (fn [{:keys [metadata] :as model}]
                   (if (and (= (:witan/name metadata) name)
                            (= (:witan/version metadata) version))
                     (assoc model
                            :workflow workflow
                            :catalog catalog)
                     model)) ml)]
    (data/swap-app-state! :app/workspace assoc :workspace/model-list ml')))

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
        workspace-fields (vec
                          (filter keyword? (-> wgs/WorkspaceMessage
                                               (get "1.0.0")
                                               (keys))))]
    (data/query `[{(:workspace/by-id ~workspace-id) ~workspace-fields}] on-receive)))

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
      (data/subscribe-topic :data/user-logged-in on-user-logged-in)
      (data/subscribe-topic :data/event-received on-event)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Handlers

(defmulti handle
  (fn [event args] event))

(defmethod handle :create
  [event {:keys [name desc]}]
  (let [{:keys [user/id]} (data/get-app-state :app/user)
        w-id (random-uuid)
        wsp (wgs/validate-workspace
             "1.0.0"
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

(defmethod handle :fetch-models
  [_ _]
  (log/debug "Fetching models....")
  (data/query [{:workspace/available-models [:metadata]}] on-receive))

(defmethod handle :select-model
  [_ {:keys [name version]}]
  (log/debug "Fetching model" name version)
  (data/query `[{(:workspace/model-by-name-and-version ~name ~version)
                 [:workflow :catalog :metadata]}]
              #(let [[_ {:keys [workflow catalog]}] %]
                 (on-receive %)
                 (data/swap-app-state!
                  :app/workspace assoc-in [:workspace/current :workspace/workflow]
                  workflow)
                 (data/swap-app-state!
                  :app/workspace assoc-in [:workspace/current :workspace/catalog]
                  catalog))))

(defmethod handle :run-current
  [_ _]
  (let [{:keys [workspace/current workspace/running? workspace/temp-variables]}
        (data/get-app-state :app/workspace)
        apply-temp-vars (fn [entry]
                          (if (= :input (:witan/type entry))
                            (update-in entry [:witan/params :src] #(utils/render-mustache % temp-variables))
                            entry))]
    (if running?
      (log/warn "Model is already running. Request ignored.")
      (do
        (log/info "Running model" (:workspace/id current))
        (data/swap-app-state! :app/workspace assoc :workspace/running? true)
        (let [current' (update current :workspace/catalog (partial mapv apply-temp-vars))]
          (data/command! :workspace/run "1.0.0" {:workspace/to-run current'}))))))

(defmethod handle :adjust-current-data
  [_ {:keys [key value]}]
  (let [current (:workspace/current (data/get-app-state :app/workspace))
        {:keys [workspace/catalog]} current
        input (some #(when (= key (:witan/name %)) %) catalog)]
    (when input
      (let [catalog' (vec (-> (remove #{input} catalog)
                              (conj (assoc-in input [:witan/params :src] value))))]
        (data/swap-app-state! :app/workspace assoc-in [:workspace/current :workspace/catalog] catalog')))))

(defmethod handle :adjust-current-configuration
  [_ {:keys [key value]}]
  (let [current (:workspace/current (data/get-app-state :app/workspace))
        {:keys [workspace/catalog]} current
        entries (not-empty (filter #(when (contains? (:witan/params %) key) %) catalog))]
    (when entries
      (let [entries' (map #(assoc-in % [:witan/params key] value) entries)
            catalog' (vec (reduce conj (remove (set entries) catalog) entries'))]
        (data/swap-app-state! :app/workspace assoc-in [:workspace/current :workspace/catalog] catalog')))))

(defmethod handle :adjust-temp-variable
  [_ {:keys [key value]}]
  (if (clojure.string/blank? value)
    (data/swap-app-state! :app/workspace update :workspace/temp-variables #(dissoc % key))
    (data/swap-app-state! :app/workspace assoc-in [:workspace/temp-variables key] value)))

(defmethod handle :download-result
  [_ {:keys [result]}]
  (let [wsp (data/get-app-state :app/workspace)]
    (set-result-downloading! wsp result true)
    (data/command! :workspace/create-result-url "1.0.0" {:workspace/result-location (:result/location result)})))
