(ns witan.ui.data
  (:require [reagent.core :as r]
            [goog.net.cookies :as cookies]
            [goog.crypt.base64 :as b64]
            [schema.core :as s]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]]
            [cljs.reader :as reader]   )
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def config {:gateway/address (or (cljs-env :witan-api-url) "localhost:30015")})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PubSub

(def publisher (chan))
(def publication (pub publisher #(:topic %)))

(def topics
  #{:data/app-state-restored
    :data/user-logged-in})

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; App State

(defn atomize-map
  [m]
  (reduce-kv (fn [a k v] (assoc a k (r/atom v))) {} m))

(defn deatomize-map
  [m]
  (reduce-kv (fn [a k v] (assoc a k (deref v))) {} m))

;; app state schema
(def AppStateSchema
  {:app/side {:side/upper [[s/Keyword]]
              :side/lower [[s/Keyword]]}
   :app/login {:login/phase s/Keyword
               :login/success? s/Bool
               :login/token (s/maybe s/Str)
               :login/id (s/maybe s/Str)
               :login/message (s/maybe s/Str)}
   :app/user {:user/name (s/maybe s/Str)
              :user/groups-by-id [s/Int]}
   :app/route {:route/path (s/maybe s/Keyword)
               :route/params (s/maybe s/Any)}
   :app/workspace  {:workspace/primary   {:primary/view-selected s/Int}
                    :workspace/secondary {:secondary/view-selected s/Int}}
   :app/workspace-dash {:wd/selected-id (s/maybe s/Int)
                        :wd/workspaces (s/maybe [{:workspace/name s/Str
                                                  :workspace/id s/Int
                                                  :workspace/owner-id s/Int
                                                  :workspace/owner-name s/Str
                                                  :workspace/modified s/Str}])}
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
    :app/user {:user/name nil
               :user/groups-by-id []}
    :app/route {:route/path nil
                :route/params nil}
    ;; component data
    :app/workspace {:workspace/primary   {:primary/view-selected 0}
                    :workspace/secondary {:secondary/view-selected 0}}
    :app/workspace-dash {:wd/selected-id nil
                         :wd/workspaces nil}
    :app/data-dash {:about/content "This is the about page, the place where one might write things about their own self."}}
   (s/validate AppStateSchema)
   (atomize-map)))

(defn get-app-state
  [k]
  (deref (get app-state k)))

(defn app-state-swap!
  [k & symbs]
  (update app-state k #(apply swap! % symbs)))

(defn app-state-reset!
  [k value]
  (update app-state k #(reset! % value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Cookies

(def cookie-name "_data_")
(defonce wants-to-load? (atom true))

(defn save-data!
  []
  (log/debug "Saving app state to cookie")
  (.set goog.net.cookies
        cookie-name
        (-> app-state
            deatomize-map
            pr-str
            b64/encodeString)
        -1
        "/"))

(defn delete-data!
  []
  (log/debug "Deleting contents of cookie")
  (.remove goog.net.cookies
           cookie-name
           "/"))

(defn load-data!
  []
  (if-let [data (.get goog.net.cookies cookie-name)]
    (when @wants-to-load?
      (reset! wants-to-load? false)
      (let [unencoded (->> data b64/decodeString reader/read-string)]
        (try
          (do
            (run! (fn [[k v]] (app-state-reset! k v)) unencoded)
            (log/debug "Restored app state from cookie")
            (publish-topic :data/app-state-restored))
          (catch js/Object e
            (log/warn "Failed to restore app state from cookie:" (str e))
            (delete-data!)))))
    (log/debug "(No existing token was found.)")))

(load-data!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Websocket

(defonce ws-conn (atom nil))
(defonce message-id (atom 0))
(defonce events (atom []))
(def query-responses (atom {}))

(defn query
  [query cb]
  (if (vector? query)
    (let [id (swap! message-id inc)
          m {:message/type :query
             :query/id id
             :query/edn query}]
      (log/debug "Sending query:" m)
      (swap! query-responses assoc id cb)
      (go (>! @ws-conn m)))
    (throw (js/Error. "Query needs to be a vector"))))

(defn command!
  [command-key version params]
  (let [m {:message/type :command
           :command/key command-key
           :command/version version
           :command/id (swap! message-id inc)
           :command/params params}]
    (log/debug "Sending command:" m)
    (go (>! @ws-conn m))))

;;

(defmulti handle-server-message
  (fn [{:keys [message/type]}] type))

(defmethod handle-server-message
  :default
  [msg]
  (println "Unknown message:" msg))

(defmethod handle-server-message
  :command-receipt
  [msg])

(defmethod handle-server-message
  :query-response
  [{:keys [query/id query/results]}]
  (doseq [{:keys [query/result]} results]
    (log/debug "TODO got query result in data")
    #_(run! (fn [[k r]] (receive-data! k r)) result)))

(defmethod handle-server-message
  :event
  [event]
  (println "Got event" event)
  (swap! events conj event))

;;

(defn connect!
  [{:keys [on-connect]}]
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch (str "ws://"
                                                     (get config :gateway/address)
                                                     "/ws")))]
      (if-not error
        (do
          (reset! ws-conn ws-channel)
          (on-connect)
          (go-loop []
            (let [{:keys [message]} (<! ws-channel)]
              (handle-server-message message))
            (recur)))
        (js/console.log "Error:" (pr-str error))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Mutations - we should remove as many of these are possible

(defmulti mutate
  (fn [f _] f))

(defmethod mutate 'change/route!
  [_ {:keys [route route-params]}]
  (app-state-swap! :app/route assoc-in [:route/path]   route)
  (app-state-swap! :app/route assoc-in [:route/params] route-params))

(defmethod mutate 'change/primary-view!
  [_ {:keys [idx]}]
  (app-state-swap! :app/workspace assoc-in [:workspace/primary :primary/view-selected] idx))

(defmethod mutate 'change/secondary-view!
  [_ {:keys [idx]}]
  (app-state-swap! :app/workspace assoc-in [:workspace/secondary :secondary/view-selected] idx))

;;;;;;;;;;;;;;;;;;;;;;;
;; login state changes

(defmethod mutate 'login/goto-phase!
  [_ {:keys [phase]}]
  (log/debug "Remove me!!")
  (app-state-swap! :app/login assoc-in [:login/phase] phase))

(defmethod mutate 'login/set-message!
  [_ {:keys [message]}]
  (log/debug "Remove me!!")
  (app-state-swap! :app/login assoc-in [:login/message] message))

;;;;;;;;;;;;;;;;;;;;;;;
;; workspace dash changes

(defmethod mutate 'wd/select-row!
  [_ {:keys [id]}]
  (log/debug "Remove me!!")
  (app-state-swap! :app/workspace-dash assoc-in [:wd/selected-id] id))

;;;;

(defn transact!
  [f args]
  (mutate f args))
