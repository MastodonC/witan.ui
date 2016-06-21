(ns witan.ui.data
  (:require [datascript.core :as d]
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
   :app/user {:user/name (s/maybe s/Str)
              :user/groups-by-id [s/Int]}
   :app/route (s/maybe s/Keyword)
   :app/route-params (s/maybe s/Any)
   :app/workspace  {:workspace/primary   {:primary/view-selected s/Int}
                    :workspace/secondary {:secondary/view-selected s/Int}}
   :app/workspace-dash {:wd/selected-id (s/maybe s/Int)
                        :wd/workspaces (s/maybe [{:workspace/name s/Str
                                                  :workspace/id s/Int
                                                  :workspace/owner-id s/Int
                                                  :workspace/owner-name s/Str
                                                  :workspace/modified s/Str}])}
   :app/data-dash (s/maybe s/Any)
   :app/create-workspace {:cw/message (s/maybe s/Str)
                          :cw/pending? s/Bool}})

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
    :app/route nil
    :app/route-params nil
    :app/workspace {:workspace/primary   {:primary/view-selected 0}
                    :workspace/secondary {:secondary/view-selected 0}}
    :app/workspace-dash {:wd/selected-id nil
                         :wd/workspaces nil}
    :app/data-dash {:about/content "This is the about page, the place where one might write things about their own self."}
    :app/create-workspace {:cw/message nil
                           :cw/pending? false}}
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
  (.set goog.net.cookies
        cookie-name
        (-> @app-state pr-str b64/encodeString)
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
    (let [unencoded (->> data b64/decodeString cljs.reader/read-string)]
      (try (reset! app-state (s/validate AppStateSchema unencoded))
           (log/debug "Restored app state from cookie")
           (publish-topic :data/app-state-restored)
           (catch js/Object e
             (log/warn "Failed to restore app state from cookie.")
             (delete-data!))))
    (log/debug "(No existing token was found.)")))

(load-data!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; writes

(defmulti mutate
  (fn [_ f _] f))

(defmethod mutate 'change/route!
  [{:keys [state]} _ {:keys [route route-params]}]
  (swap! state assoc :app/route route)
  (swap! state assoc :app/route-params route-params))

(defmethod mutate 'wd/select-row!
  [{:keys [state]} _ {:keys [id]}]
  (swap! state assoc-in [:app/workspace-dash :wd/selected-id] id))

(defmethod mutate 'change/primary-view!
  [{:keys [state]} _ {:keys [idx]}]
  (swap! state assoc-in [:app/workspace :workspace/primary :primary/view-selected] idx))

(defmethod mutate 'change/secondary-view!
  [{:keys [state]} _ {:keys [idx]}]
  (swap! state assoc-in [:app/workspace :workspace/secondary :secondary/view-selected] idx))

;;;;;;;;;;;;;;;;;;;;;;;
;; login state changes

(defmethod mutate 'login/goto-phase!
  [{:keys [state]} _ {:keys [phase]}]
  (swap! state assoc-in [:app/login :login/phase] phase))

(defmethod mutate 'login/set-message!
  [{:keys [state]} _ {:keys [message]}]
  (swap! state assoc-in [:app/login :login/message] message))

(defmethod mutate 'login/complete!
  [{:keys [state]} _ {:keys [token id]}]
  (swap! state assoc-in [:app/login :login/id] id)
  (swap! state assoc-in [:app/login :login/token] token))

;;;;;;;;;;;;;;;;;;;;;;;
;; workspace dash changes

(defmethod mutate 'wd/select-row!
  [{:keys [state]} _ {:keys [id]}]
  (swap! state assoc-in [:app/workspace-dash :wd/selected-id] id))

;;;;;;;;;;;;;;;;;;;;;;;
;; workspace creation

(defmethod mutate 'cw/set-message!
  [{:keys [state]} _ {:keys [message]}]
  (swap! state assoc-in [:app/create-workspace :cw/message] message))

(defmethod mutate 'cw/set-pending!
  [{:keys [state]} _ {:keys [pending?]}]
  (swap! state assoc-in [:app/create-workspace :cw/pending?] pending?))

(defn transact!
  [owner f args]
  (mutate owner f args))
