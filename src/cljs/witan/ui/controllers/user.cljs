(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [schema.core :as s]
            [witan.ui.data :as data]
            [goog.crypt.base64 :as b64]
            [goog.string :as gstring]
            [cljs.reader :as reader]
            [witan.ui.data :refer [transit-decode]]
            [witan.ui.schema :as ws])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

;; regex from here http://www.lispcast.com/clojure.spec-vs-schema
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}")
(def password-regex  #"(?=.*\d.*)(?=.*[a-z].*)(?=.*[A-Z].*).{8,}")

(def Login
  {:username email-regex
   :password password-regex})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill-login-screen!
  []
  (when-let [login-div (.getElementById js/document "login")]
    (aset login-div "style" "visibility" "hidden")
    (.unmountComponentAtNode js/ReactDOM login-div)))

(defn login-success!
  [{:keys [token-pair] :as response}]
  (if response
    ;; if response, we assume everything is shiny and new
    (let [auth-info    (data/deconstruct-token (:auth-token token-pair))
          refresh-info (data/deconstruct-token (:refresh-token token-pair))]
      (data/swap-app-state! :app/user assoc  :kixi.user/id (:id auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/name (:name auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/groups (:user-groups auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/self-group (:self-group auth-info))
      (data/save-token-pair! token-pair)
      (data/save-data!))
    ;; if NO response, check we have everything
    ;; NB. don't bother checking that tokens are valid because they'll be checked
    ;; by data as soon as any commands or queries are issued
    (let [user  (data/get-app-state :app/user)
          login (data/get-app-state :app/login)]
      ;; - checking presence of data
      (when-not (and (:kixi.user/id user)
                     (:kixi.user/name user)
                     (:login/token login)
                     (:login/auth-expiry login)
                     (:login/refresh-expiry login))
        (data/delete-data!)
        (data/panic! (str "login-success! was called erroneously - data was deleted" login))
        (throw (js/Error. "login-success! was called erroneously")))))
  (kill-login-screen!)
  (data/connect! {:on-connect #(data/publish-topic :data/user-logged-in (data/get-app-state :app/user))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti api-response
  (fn [{:keys [event status]} response] [event status]))

(defmethod api-response
  [:login :success]
  [_ {:keys [token-pair] :as response}]
  (data/swap-app-state! :app/login assoc :login/pending? false)
  (if token-pair
    (login-success! response)
    (data/swap-app-state! :app/login assoc :login/message :string/sign-in-failure)))

(defmethod api-response
  [:login :failure]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/pending? false)
  (if (= 401 (:status response))
    (data/swap-app-state! :app/login assoc :login/message :string/sign-in-failure)
    (data/swap-app-state! :app/login assoc :login/message :string/api-failure)))

(defn route-api-response
  [event]
  (fn [status response]
    (api-response {:event event :status status} response)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :groups/search
  [[_ data]]
  (data/swap-app-state! :app/user assoc :user/group-search-results (:items data))
  (data/swap-app-state! :app/user assoc :user/group-search-filtered (:items data)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle :login
  [event {:keys [email pass]}]
  (let [args {:username email :password pass}]
    (data/swap-app-state! :app/login assoc :login/pending? true)
    (POST (str "http://" (:gateway/address data/config) "/login")
          {:id event
           :params (s/validate Login args)
           :result-cb (route-api-response event)})))

(defmethod handle :logout
  [event {:keys [email pass]}]
  (data/reset-everything!))

(defmethod handle :refresh-groups
  [event {:keys [email pass]}]
  (data/query {:groups/search [[] (keys ws/GroupSchema)]} on-query-response))

(defmethod handle :search-groups
  [event {:keys [search]}]
  (let [groups (data/get-in-app-state :app/user :user/group-search-results)
        filtered-groups (filter
                         #(gstring/caseInsensitiveContains
                           (:kixi.group/name %)
                           search) groups)]
    (data/swap-app-state! :app/user assoc :user/group-search-filtered filtered-groups)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce subscriptions
  (do
    (data/subscribe-topic :data/app-state-restored #(login-success! nil))))
