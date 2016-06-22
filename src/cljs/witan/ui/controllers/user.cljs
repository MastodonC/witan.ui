(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [schema.core :as s]
            [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def Login
  {:username s/Str
   :password (s/constrained s/Str #(> (count %) 7))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill-login-screen!
  []
  (when-let [login-div (.getElementById js/document "login")]
    (aset login-div "style" "visibility" "hidden")))

(defn login-success!
  [{:keys [id token] :as response}]
  (when response
    (data/app-state-swap! :app/login assoc-in [:login/id] id)
    (data/app-state-swap! :app/login assoc-in [:login/token] token)
    (data/save-data!))
  (kill-login-screen!)
  (data/connect! {:on-connect #(data/publish-topic :data/user-logged-in)}))

(defn local-endpoint
  [method]
  (let [api-url (cljs-env :witan-api-url)] ;; 'nil' is a valid api-url (will default to current hostname)
    (str api-url "/api" method)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defmethod api-response
  [:login :success]
  [{:keys [owner]} {:keys [token] :as response}]
  (if token
    (login-success! response)
    (data/transact! 'login/set-message! {:message :string/sign-in-failure})))

(defmethod api-response
  [:login :failure]
  [{:keys [owner]} response]
  (login-success! response)
  #_(data/transact! 'login/set-message! {:message :string/api-failure}))

(defn route-api-response
  [event owner]
  (fn [status response]
    (api-response {:owner owner :event event :status status} response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event owner args] event))

(defmethod handle :login
  [event owner {:keys [email pass]}]
  (let [args {:username email :password pass}]
    (POST (local-endpoint "/login")
          {:id event
           :params (s/validate Login args)
           :result-cb (route-api-response event owner)})))

(defmethod handle :logout
  [event owner {:keys [email pass]}]
  (data/delete-data!)
  (.replace js/location "/" true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(data/subscribe-topic :data/app-state-restored login-success!)
