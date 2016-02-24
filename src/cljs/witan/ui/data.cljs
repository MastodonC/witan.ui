(ns witan.ui.data
  (:require [datascript.core :as d]
            [om.next :as om])
  (:require-macros [cljs-log.core :as log]))

(defonce app-state
  (atom
   {:app/side {:side/upper '([:button :workspaces]
                             [:button :data]
                             [:button :settings])
               :side/lower '([:button :help]
                             [:button :logout])}
    :app/route :app/workspace-dash
    :app/route-params nil
    :app/workspace {:workspace/min-size 200}
    :app/workspace-dash {:home/title "Workspace dash"
                         :home/content "This is the homepage. There isn't a lot to see here."}
    :app/data-dash {:about/title "Data dashboard"
                    :about/content "This is the about page, the place where one might write things about their own self."}}))

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
   :action #(let [current-route (:app/route @state)]
              (swap! state assoc :app/route route)
              (swap! state assoc :app/route-params route-params))})
