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
    :app/login {:login/success? false
                :login/token nil
                :login/phase :prompt}
    :app/user {:user/name nil}
    :app/route nil
    :app/route-params nil
    :app/workspace {:workspace/primary   {:primary/view-selected 0}
                    :workspace/secondary {:secondary/view-selected 0}}
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

(defmethod read :app/login
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (select-keys (get st k) query)}))

(defmethod read :app/route-params
  [{:keys [state query]} k _]
  (log/debug ":app/route-params state" state )
  (let [st @state]
    {:value (get st k)}))

(defmethod read :workspace/primary
  [{:keys [state query] :as foo} k params]
  (let [st @state]
    {:value (select-keys (get-in st [:app/workspace k]) query)}))

(defmethod read :workspace/secondary
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (select-keys (get-in st [:app/workspace k]) query)}))

(defmethod read :route/data
  [{:keys [state query parser] :as env} key params]
  (let [st @state]
    (let [result (get st (get st :app/route))
          with-route-params (assoc result :app/route-params (get st :app/route-params))
          embedded-queries (filter map? query)
          recursive-results (map #(parser {:state state} (vector %)) embedded-queries)]
      {:value (reduce merge (select-keys with-route-params query) recursive-results)})))

(defmethod read :app/workspace-dash
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :app/data-dash
  [{:keys [state query]} k _]
  (let [st @state]
    {:value (get st k)}))

(defmethod read :app/side
  [{:keys [state query]} _ _]
  {:value (select-keys (:app/side @state) query)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
  {:value {:keys [:route/data]}
   :action (fn [_]
             (swap! state assoc-in [:app/workspace :workspace/primary :primary/view-selected] idx))})

(defmethod mutate 'change/secondary-view!
  [{:keys [state]} _ {:keys [idx]}]
  {:value {:keys [:route/data]}
   :action (fn [_]
             (swap! state assoc-in [:app/workspace :workspace/secondary :secondary/view-selected] idx))})

(defmethod mutate 'login/goto-phase!
  [{:keys [state]} _ {:keys [phase]}]
  {:value {:keys [:app/login]}
   :action (fn [_]
             (swap! state assoc-in [:app/login :login/phase] phase))})
