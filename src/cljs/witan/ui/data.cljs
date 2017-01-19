(ns witan.ui.data
  (:require [reagent.core :as r]
            [goog.net.cookies :as cookies]
            [goog.crypt.base64 :as b64]
            [schema.core :as s]
            [witan.ui.schema :as ws]
            [chord.client :refer [ws-ch]]
            [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]]
            [cljs.reader :as reader]
            [cognitect.transit :as tr]
            [outpace.schema-transit :as st]
            [cljs-time.coerce :as tc]
            [cljs-time.core :as t])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def config {:gateway/address (or (cljs-env :witan-api-url) "localhost:30015")
             :viz/address     (or (cljs-env :witan-viz-url) "localhost:3448")})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; App State

(defn atomize-map
  [m]
  (reduce-kv (fn [a k v] (assoc a k (r/atom v))) {} m))

(defn deatomize-map
  [m]
  (reduce-kv (fn [a k v] (assoc a k (deref v))) {} m))

;; default app-state
(defonce app-state
  (->>
   {:app/side {:side/upper [[:button :workspaces]
                            [:button :data]
                            [:button :rts]]
               :side/lower [[:button :help]
                            [:button :logout]]}
    :app/login {:login/pending? false
                :login/token nil
                :login/message nil
                :login/auth-expiry -1
                :login/refresh-expiry -1}
    :app/user {:kixi.user/name nil
               :kixi.user/id nil
               :kixi.user/groups [nil]
               :kixi.user/self-group nil}
    :app/route {:route/path nil
                :route/params nil
                :route/query nil}
    ;; component data
    :app/workspace {:workspace/temp-variables {}
                    :workspace/running? false
                    :workspace/pending? true}
    :app/workspace-dash {:wd/workspaces nil}
    :app/data-dash {}
    :app/create-data {:cd/pending? false}
    :app/rts-dash {}
    :app/workspace-results []
    :app/panic-message nil
    :app/create-rts {:crts/pending? false}
    :app/request-to-share {:rts/requests {}
                           :rts/current nil
                           :rts/pending? false}
    :app/datastore {:ds/current nil
                    :ds/pending? false
                    :ds/file-metadata {}}}
   (s/validate ws/AppStateSchema)
   (atomize-map)))

(defn get-app-state
  [k]
  (deref (get app-state k)))

(defn get-in-app-state
  [k & ks]
  (get-in (deref (get app-state k)) ks))

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
  (swap-app-state! :app/workspace dissoc :workspace/current)
  (reset-app-state! :app/panic-message nil)
  (swap-app-state! :app/create-data dissoc :cd/pending-data))

(defn save-data!
  []
  (log/debug "Saving app state to cookie")
  (let [unencoded (deatomize-map app-state)]
    (s/validate ws/AppStateSchema unencoded)
    (.set goog.net.cookies
          cookie-name
          (-> unencoded
              pr-str
              b64/encodeString)
          -1
          "/")))

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
            (s/validate ws/AppStateSchema unencoded)
            (run! (fn [[k v]] (reset-app-state! k v)) unencoded)
            (custom-resets!)
            (log/debug "Restored app state from cookie")
            (publish-topic :data/app-state-restored))
          (catch js/Object e
            (log/warn "Failed to restore app state from cookie:" (str e))
            (delete-data!)))))
    (log/debug "(No existing token was found.)")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Websocket

(defonce ws-conn (atom nil))
(defonce ws-timeout (atom nil))
(defonce events (atom []))
(defonce token-refresh-callbacks (atom []))
(def query-responses (atom {}))
(def message-buffer (atom []))

(def transit-encoding-level :json-verbose) ;; DO NOT CHANGE

(def transit-reader
  (tr/reader
   transit-encoding-level
   {:handlers st/cross-platform-read-handlers}))

(defn transit-decode
  ([s key-fn]
   (reduce-kv (fn [a k v] (assoc a (key-fn k) v)) {}
              (tr/read transit-reader s)))
  ([s]
   (tr/read transit-reader s)))

(def transit-writer
  (tr/writer
   transit-encoding-level
   {:handlers st/cross-platform-read-handlers}))

(defn transit-encode
  [s]
  (tr/write transit-writer s))

(defn reset-everything!
  []
  (delete-data!)
  (.replace js/location "/" true))

(defn- buffer-message
  [m]
  (log/debug "Buffering message:" m)
  (swap! message-buffer conj m))

(declare send-ws!)

(defn send-ping!
  []
  (send-ws! {:kixi.comms.message/type "ping"}))

(defn manage-token-validity
  [completed-cb]
  (let [{:keys [login/auth-expiry
                login/refresh-expiry]} (get-app-state :app/login)
        ae-as-time (tc/from-long auth-expiry)]
    (if (t/after? (t/now) ae-as-time)
      (let [re-as-time (tc/from-long refresh-expiry)]
        (if (t/after? (t/now) re-as-time)
          (do
            (log/debug "Refresh token has expired. Logging out...")
            (reset-everything!))
          (let [refresh-required (empty? @token-refresh-callbacks)]
            (swap! token-refresh-callbacks conj completed-cb)
            (when refresh-required
              (log/debug "Sending tokens for refresh")
              (send-ws! {:kixi.comms.message/type "refresh"} false)))))
      (completed-cb))))

(defn send-ws!
  ([payload manage]
   (let [send-fn
         #(go
            (let [{:keys [login/token]} (get-app-state :app/login)
                  payload' (assoc payload :kixi.comms.auth/token-pair token)]
              (log/debug "Sending message:" payload)
              (>! @ws-conn (transit-encode payload')))
            (when @ws-timeout
              (.clearInterval js/window @ws-timeout))
            (reset! ws-timeout (.setInterval js/window send-ping! 55000)))] ;; 55 secs
     (if-not @ws-conn
       (buffer-message payload)
       (if manage
         (manage-token-validity send-fn)
         (send-fn)))))
  ([payload]
   (send-ws! payload true)))

(defn- drain-buffered-messages
  []
  (when-not (empty? @message-buffer)
    (log/debug "Draining buffered messages...")
    (run! send-ws! @message-buffer)
    (reset! message-buffer [])))

(defn query
  [query cb]
  (if (map? query)
    (let [id (str (random-uuid))
          m {:kixi.comms.message/type "query"
             :kixi.comms.query/id id
             :kixi.comms.query/body query}]
      (swap! query-responses assoc id cb)
      (send-ws! m))
    (throw (js/Error. "Query needs to be a map"))))

(defn command!
  [command-key version params]
  (let [id (str (random-uuid))
        m {:kixi.comms.message/type "command"
           :kixi.comms.command/key command-key
           :kixi.comms.command/version version
           :kixi.comms.command/id id
           :kixi.comms.command/payload params}]
    (send-ws! m)))

;;

(defmulti handle-server-message
  (fn [{:keys [kixi.comms.message/type]}] type))

(defmethod handle-server-message
  :default
  [msg]
  (log/warn "Unknown message:" msg))

(defmethod handle-server-message
  "pong"
  [msg])

(defmethod handle-server-message
  "error"
  [{:keys [kixi.comms.message/payload] :as msg}]
  (let [{:keys [witan.gateway/error
                witan.gateway/error-str]} payload]
    (log/severe "An error was received from the server:" msg)
    (condp = error
      :server-error (panic! (str "Server Error:" error-str))
      :unauthenticated (reset-everything!))))

(defmethod handle-server-message
  "query-response"
  [{:keys [kixi.comms.query/id kixi.comms.query/results kixi.comms.query/error]}]
  (if-let [cb (get @query-responses id)]
    (if error
      (log/warn "Query failed:" error)
      (doseq [result results]
        (if result
          (let [r (first result)]
            (log/debug "Query response:" r)
            (cb r))
          (do
            (panic! (str "Error in query response: " result))
            (cb [:error result])))))
    (log/warn "Received query response id [" id "] but couldn't match callback."))
  (swap! query-responses dissoc id))

(defn deconstruct-token
  [tkn]
  (-> tkn
      (clojure.string/split #"\.")
      (second)
      (b64/decodeString)
      (transit-decode keyword)))

(defn save-token-pair!
  [token-pair]
  (let [auth-info    (deconstruct-token (:auth-token token-pair))
        refresh-info (deconstruct-token (:refresh-token token-pair))]
    (swap-app-state! :app/login assoc :login/token token-pair)
    (swap-app-state! :app/login assoc :login/auth-expiry (:exp auth-info))
    (swap-app-state! :app/login assoc :login/refresh-expiry (:exp refresh-info))
    (swap-app-state! :app/login assoc :login/message nil)))

(defmethod handle-server-message
  "refresh-response"
  [{:keys [kixi.comms.auth/token-pair]}]
  (if token-pair
    (do (save-token-pair! token-pair)
        (save-data!)
        (doseq [cb @token-refresh-callbacks] (cb))
        (reset! token-refresh-callbacks []))
    (do
      (log/debug "Tokens could not be refreshed. Logging out...")
      (reset-everything!))))

(defmethod handle-server-message
  "event"
  [event]
  (publish-topic :data/event-received event))

;;

(add-watch
 ws-conn
 nil
 (fn [k r old new]
   (when @ws-conn
     (log/debug "Connected")
     (drain-buffered-messages))))

(defn connect!
  [{:keys [on-connect]}]
  (log/debug "Connecting to gateway...")
  (go
    (let [{:keys [ws-channel error]} (<! (ws-ch (str "ws://"
                                                     (get config :gateway/address)
                                                     "/ws")
                                                {:format :str}))]
      (if-not error
        (do
          (reset! ws-conn ws-channel)
          (on-connect)
          (go-loop []
            (let [{:keys [message] :as resp} (<! ws-channel)
                  message (transit-decode message)]
              (if message
                (if (contains? message :error)
                  (panic! (str "Received message error: " message))
                  (do
                    (handle-server-message message)
                    (recur)))
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
