(ns witan.ui.data
  (:require [datascript.core :as d]
            [om.next :as om])
  (:require-macros [cljs-log.core :as log]))

(defonce app-state
  (atom
   {:app/side {:side/upper '([:button :workspaces]
                             [:button :data])
               :side/lower '([:button :help]
                             [:button :logout])}
    :app/route nil
    :app/route-params nil
    :app/workspace {:workspace/primary
                    {:primary/view-selected 0}
                    :workspace/secondary
                    {:secondary/view-selected 0}}

    :app/workspace-dash {:wd/selected-id nil}
    :app/data-dash {:about/content "This is the about page, the place where one might write things about their own self."}}))

(def conn (d/create-conn {}))

(d/transact! conn [{:db/id -1
                    :app/count 3}])

(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defn make-reconciler []
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})}))

(defmethod read :app/route
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :route/data
  [{:keys [state query]} k _]
  (let [st @state]
    (let [result (get st (get st :app/route))
          with-route-params (assoc result :app/route-params (get st :app/route-params))]
      {:value (select-keys with-route-params query)})))

(defmethod read :app/side
  [{:keys [state query]} _ _]
  {:value (select-keys (:app/side @state) query)})

(defmethod read :app/workspace
  [{:keys [state query]} _ _]
  (log/debug "Q :app/workspace" query)
  {:value (select-keys (:app/workspace @state) query)})

(defmethod read :app/counter
  [{:keys [state query]} _ _]
  #_(println "query" query)
  {:value (d/q '[:find [(pull ?e ?selector) ...]
                 :in $ ?selector
                 :where [?e :app/count]]
               (d/db state) query)})

;;;;;;

(defmethod mutate 'app/increment
  [{:keys [state]} _ entity]
  {:value {:keys [:app/counter]}
   :action (fn []
             (d/transact!
              state
              [(update-in entity [:app/count] inc)]))})

(defmethod mutate 'change/route!
  [{:keys [state]} _ {:keys [route route-params]}]
  {:value {:keys [:app/route]}
   :action (fn [_]
             (swap! state assoc :app/route route)
             (swap! state assoc :app/route-params route-params))})

(defmethod mutate 'wd/select-row!
  [{:keys [state]} _ {:keys [id]}]
  {:value {:keys [:route/data]}
   :action (fn [_]
             (swap! state assoc-in [:app/workspace-dash :wd/selected-id] id))})

(defmethod mutate 'change/primary-view!
  [{:keys [state]} _ {:keys [idx]}]
  {:value {:keys [:app/workspace]}
   :action (fn [_]
             (swap! state assoc-in [:app/workspace :workspace/primary :primary/view-selected] idx))})
