(ns witan.ui.data
  (:require [reagent.core :as r]
            [goog.net.cookies :as cookies]
            [goog.crypt.base64 :as b64]
            [schema.core :as s]
            [witan.gateway.schema :as wgs]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]]
            [cljs.reader :as reader]
            [cognitect.transit :as tr]
            [outpace.schema-transit :as st])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def config {:gateway/address (or (cljs-env :witan-api-url) "localhost:30015")})

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
   :app/login {:login/token (s/maybe s/Str)
               :login/message (s/maybe s/Str)}
   :app/user {:user/name (s/maybe s/Str)
              :user/id (s/maybe s/Uuid)}
   :app/route {:route/path (s/maybe s/Keyword)
               :route/params (s/maybe s/Any)
               :route/query (s/maybe {s/Keyword s/Any})}
   :app/workspace  {:workspace/temp-variables {s/Str s/Str}
                    :workspace/running? s/Bool
                    :workspace/pending? s/Bool
                    (s/optional-key :workspace/current) (get wgs/WorkspaceMessage "1.0.0")
                    (s/optional-key :workspace/current-results) [{:result/location s/Str
                                                                  :result/key s/Keyword
                                                                  :result/downloading? s/Bool
                                                                  (s/optional-key :result/content) s/Any}]
                    (s/optional-key :workspace/current-viz) {:result/location s/Str}
                    (s/optional-key :workspace/model-list) [{s/Keyword s/Any}]}
   :app/workspace-dash {:wd/workspaces (s/maybe [(get wgs/WorkspaceMessage "1.0.0")])}
   :app/data-dash (s/maybe s/Any)
   :app/panic-message (s/maybe s/Str)})

;; default app-state
(defonce app-state
  (->>
   {:app/side {:side/upper [[:button :workspaces]
                            [:button :data]]
               :side/lower [[:button :help]
                            [:button :logout]]}
    :app/login {:login/token nil
                :login/message nil}
    :app/user {:user/name nil
               :user/id nil}
    :app/route {:route/path nil
                :route/params nil
                :route/query nil}
    ;; component data
    :app/workspace {:workspace/temp-variables {}
                    :workspace/running? false
                    :workspace/pending? true}
    :app/workspace-dash {:wd/workspaces nil}
    :app/data-dash {:about/content "This is the about page, the place where one might write things about their own self."}
    :app/panic-message nil}
   (s/validate AppStateSchema)
   (atomize-map)))

(defn get-app-state
  [k]
  (deref (get app-state k)))

(defn swap-app-state!
  [k & symbs]
  (update app-state k #(apply swap! % symbs)))

(defn reset-app-state!
  [k value]
  (update app-state k #(reset! % value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Panic

(defn panic!
  [msg]
  (log/severe "App has panicked:" msg)
  (reset-app-state! :app/panic-message msg))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; PubSub

(def publisher (chan))
(def publication (pub publisher #(:topic %)))

(def topics
  #{:data/app-state-restored
    :data/route-changed
    :data/user-logged-in
    :data/event-received})

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
;; Cookies

(def cookie-name "_data_")
(defonce wants-to-load? (atom true))

(defn custom-resets!
  []
  (swap-app-state! :app/workspace assoc :workspace/pending? true)
  (reset-app-state! :app/panic-message nil))

(defn save-data!
  []
  (log/info "Saving app state to cookie")
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
  (log/info "Deleting contents of cookie")
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
            (s/validate AppStateSchema unencoded)
            (run! (fn [[k v]] (reset-app-state! k v)) unencoded)
            (custom-resets!)
            (log/info "Restored app state from cookie")
            (publish-topic :data/app-state-restored))
          (catch js/Object e
            (log/warn "Failed to restore app state from cookie:" (str e))
            (delete-data!)))))
    (log/debug "(No existing token was found.)")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Websocket

(defonce ws-conn (atom nil))
(defonce message-id (atom 0))
(defonce events (atom []))
(def query-responses (atom {}))
(def query-buffer (atom []))

(defn- send-query
  [m]
  (log/debug "Sending query:" m)
  (go (>! @ws-conn m)))

(defn- buffer-query
  [m]
  (log/debug "Buffering query:" m)
  (swap! query-buffer conj m))

(defn- drain-buffered-queries
  []
  (when-not (empty? @query-buffer)
    (log/debug "Draining buffered queries")
    (run! send-query @query-buffer)
    (reset! query-buffer [])))

(defn query
  [query cb]
  (if (vector? query)
    (let [id (swap! message-id inc)
          m {:message/type :query
             :query/id id
             :query/edn query}]
      (swap! query-responses assoc id cb)
      (if @ws-conn
        (send-query m)
        (buffer-query m)))
    (throw (js/Error. "Query needs to be a vector"))))

(defn command!
  [command-key version params]
  (let [id (swap! message-id inc)
        m {:message/type :command
           :command/key command-key
           :command/version version
           :command/id id
           :command/params params}]
    (log/debug "Sending command:" m)
    (go (>! @ws-conn m))))

(def transit-reader
  (tr/reader :json-verbose {:handlers st/cross-platform-read-handlers}))

(defn transit-decode
  [s]
  (tr/read transit-reader s))

;;

(defmulti handle-server-message
  (fn [{:keys [message/type]}] type))

(defmethod handle-server-message
  :default
  [msg]
  (log/warn "Unknown message:" msg))

(defmethod handle-server-message
  :command-receipt
  [msg])

(defmethod handle-server-message
  :query-response
  [{:keys [query/id query/results]}]
  (if-let [cb (get @query-responses id)]
    (doseq [{:keys [query/result query/error] :as qr} results]
      (if result
        (let [decoded-result (first (transit-decode result))]
          (log/debug "Query response:" decoded-result)
          (cb decoded-result))
        (do
          (panic! (str "Error in query response: " qr))
          (cb [:error (first (:query/original qr))]))))
    (log/warn "Received query response id [" id "] but couldn't match callback."))
  (swap! query-responses dissoc id))

(defmethod handle-server-message
  :event
  [event]
  (publish-topic :data/event-received event))

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
          (drain-buffered-queries)
          (go-loop []
            (let [{:keys [message] :as resp} (<! ws-channel)]
              (if message
                (if (contains? message :error)
                  (panic! (str "Received message error: " message))
                  (if-let [err (wgs/check-message "1.0.0" message)]
                    (panic! (str "Received message failed validation: " (str err)))
                    (do
                      (handle-server-message message)
                      (recur))))
                (log/warn "Websocket connection lost" resp)))))
        (panic! (str "WS connection error: " (pr-str error)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Mutations - we should remove as many of these are possible

(defmulti mutate
  (fn [f _] f))

;;;;;;;;;;;;;;;;;;;;;;;
;; login state changes

(defmethod mutate 'login/goto-phase!
  [_ {:keys [phase]}]
  (log/debug "Remove me!!")
  (swap-app-state! :app/login assoc-in [:login/phase] phase))

;;;;

(defn transact!
  [f args]
  (mutate f args))
