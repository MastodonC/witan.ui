(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [schema.core :as s]
            [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def Login
  {:username (s/constrained s/Str #(> (count %) 5))
   :password (s/constrained s/Str #(> (count %) 7))})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill-login-screen!
  []
  (when-let [login-div (.getElementById js/document "login")]
    (aset login-div "style" "visibility" "hidden")
    (.unmountComponentAtNode js/ReactDOM login-div)))

(defn login-success!
  [{:keys [id token] :as response}]
  (when response
    (data/swap-app-state! :app/user assoc  :user/id (uuid id))
    (data/swap-app-state! :app/login assoc :login/token token)
    (data/swap-app-state! :app/login assoc :login/message nil)
    (data/save-data!))
  (kill-login-screen!)
  (data/connect! {:on-connect #(data/publish-topic :data/user-logged-in (data/get-app-state :app/user))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defmethod api-response
  [:login :success]
  [_ {:keys [token] :as response}]
  (if token
    (login-success! response)
    (data/swap-app-state! :app/login assoc :login/message :string/sign-in-failure)))

(defmethod api-response
  [:login :failure]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/message :string/api-failure))

(defn route-api-response
  [event]
  (fn [status response]
    (api-response {:event event :status status} response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle :login
  [event {:keys [email pass]}]
  (let [args {:username email :password pass}]
    (POST (str "http://" (:gateway/address data/config) "/login")
          {:id event
           :params (s/validate Login args)
           :result-cb (route-api-response event)})))

(defmethod handle :logout
  [event {:keys [email pass]}]
  (data/delete-data!)
  (.replace js/location "/" true))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(data/subscribe-topic :data/app-state-restored #(login-success! nil))
