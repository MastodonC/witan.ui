(ns witan.ui.data
  (:require [datascript.core :as d]
            [om.next :as om]
            [goog.net.cookies :as cookies]
            [goog.crypt.base64 :as b64]
            [schema.core :as s]
            [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all]])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]))

(def cookie-name "_data_")
(def publisher (chan))
(def publication (pub publisher #(:topic %)))

(def topics
  #{:data/app-state-restored})

(defn publish-topic
  ([topic]
   (publish-topic topic {}))
  ([topic args]
   (if (contains? topics topic)
     (let [payload (merge {:topic topic} (when (not-empty args) {:args args}))]
       (go (>! publisher (merge {:topic topic} (when (not-empty args) {:args args}))))
       (log/debug "Publishing topic:" payload))
     (log/severe "Couldn't publish topic" topic "because it's not on the whitelist."))))

(defn subscribe-topic
  [topic cb]
  (let [subscriber (chan)
        _ (sub publication topic subscriber)]
    (go-loop []
      (cb (<! subscriber))
      (recur))))

;; app state schema
(def AppStateSchema
  {:app/side {:side/upper [[s/Keyword]]
              :side/lower [[s/Keyword]]}
   :app/login {:login/phase s/Keyword
               :login/success? s/Bool
               :login/token (s/maybe s/Str)
               :login/id (s/maybe s/Str)
               :login/message (s/maybe s/Str)}
   :app/user {:user/name (s/maybe s/Str)}
   :app/route (s/maybe s/Keyword)
   :app/route-params (s/maybe s/Any)
   :app/workspace  {:workspace/primary   {:primary/view-selected s/Int}
                    :workspace/secondary {:secondary/view-selected s/Int}}
   :app/workspace-dash {:wd/selected-id (s/maybe s/Int)}
   :app/data-dash (s/maybe s/Any)})

;; default app-state
(defonce app-state
  (->>
   {:app/side {:side/upper [[:button :workspaces]
                            [:button :data]]
               :side/lower [[:button :help]
                            [:button :logout]]}
    :app/login {:login/phase :prompt
                :login/success? false
                :login/token nil
                :login/id nil
                :login/message nil}
    :app/user {:user/name nil}
    :app/route nil
    :app/route-params nil
    :app/workspace {:workspace/primary   {:primary/view-selected 0}
                    :workspace/secondary {:secondary/view-selected 0}}
    :app/workspace-dash {:wd/selected-id nil}
    :app/data-dash {:about/content "This is the about page, the place where one might write things about their own self."}}
   (s/validate AppStateSchema)
   (atom)))

;; database
(def conn (d/create-conn {}))
(d/transact! conn [{:db/id -1
                    :app/count 3}])

;; cookies
(defn save-data!
  []
  (log/debug "Saving app state to cookie")
  (.set goog.net.cookies cookie-name (-> @app-state (dissoc :om.next/queries) pr-str b64/encodeString) -1))

(defn load-data!
  []
  (if-let [data (.get goog.net.cookies cookie-name)]
    (let [unencoded (->> data b64/decodeString cljs.reader/read-string (s/validate AppStateSchema))]
      (reset! app-state unencoded)
      (log/debug "Restored app state from cookie")
      (publish-topic :data/app-state-restored))
    (log/debug "(No existing token was found.)")))

(load-data!)

;; reconciler
(defmulti read om/dispatch)
(defmulti mutate om/dispatch)

(defn make-reconciler []
  (om/reconciler
   {:state app-state
    :parser (om/parser {:read read :mutate mutate})}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; reads

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
;; mutatations

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

(defmethod mutate 'login/set-message!
  [{:keys [state]} _ {:keys [message]}]
  {:value {:keys [:app/login]}
   :action (fn [_]
             (swap! state assoc-in [:app/login :login/message] message))})

(defmethod mutate 'login/complete!
  [{:keys [state]} _ {:keys [token id]}]
  {:value {:keys [:app/login]}
   :action (fn [_]
             (swap! state assoc-in [:app/login :login/id] id)
             (swap! state assoc-in [:app/login :login/token] token)
             (save-data!))})
