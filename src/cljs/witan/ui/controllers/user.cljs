(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [schema.core :as s]
            [witan.ui.data :as data]
            [goog.crypt.base64 :as b64]
            [goog.string :as gstring]
            [cljs.reader :as reader]
            [witan.ui.data :refer [transit-decode]]
            [witan.ui.schema :as ws]
            [witan.ui.utils :as utils])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

;; regex from here http://www.lispcast.com/clojure.spec-vs-schema
(def email-regex #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}")
(def password-regex  #"(?=.*\d.*)(?=.*[a-z].*)(?=.*[A-Z].*).{8,}")

(def Login
  {:username email-regex
   :password password-regex})

(def SignUp
  (merge Login
         {:name s/Str
          :invite-code s/Str}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn kill-login-screen!
  []
  (when-let [login-div (.getElementById js/document "login")]
    (aset login-div "style" "visibility" "hidden")
    (.unmountComponentAtNode js/ReactDOM login-div)))

(defn login-success!
  [{:keys [token-pair] :as response}]
  (data/swap-app-state! :app/login assoc :login/message nil)
  (when response
    ;; if response, we assume everything is shiny and new
    (let [auth-info    (data/deconstruct-token (:auth-token token-pair))
          refresh-info (data/deconstruct-token (:refresh-token token-pair))]
      (data/swap-app-state! :app/user assoc  :kixi.user/id (:id auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/name (:name auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/username (:username auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/groups (:user-groups auth-info))
      (data/swap-app-state! :app/user assoc  :kixi.user/self-group (:self-group auth-info)))
    (data/save-token-pair! token-pair)
    (data/save-data!))
  (kill-login-screen!)
  (let [user-valid? (fn [user]
                      (and (:kixi.user/id user)
                           (:kixi.user/name user)
                           (:kixi.user/username user)
                           (:kixi.user/self-group user)
                           (not-empty (:kixi.user/groups user))))
        login-valid? (fn [login]
                       (and (:login/token login)
                            (:login/auth-expiry login)
                            (:login/refresh-expiry login)))
        user  (data/get-app-state :app/user)
        login (data/get-app-state :app/login)]
    ;; - checking presence of data
    (cond
      (not (user-valid? user))
      (do
        (data/delete-data!)
        (data/panic! (str "Login failed! There was a problem with the user: " user)))

      (and login (not (login-valid? login)))
      (do
        (data/delete-data!)
        (data/panic! (str "Login failed! There was a problem with the login token: " login)))

      :else
      (do
        (data/connect! {:on-connect #(data/publish-topic :data/user-logged-in (data/get-app-state :app/user))})))))

(defn add-groups-to-cache!
  [items]
  (run! (fn [i] (data/swap-app-state! :app/group-cache assoc (:kixi.group/id i) i)) items))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare login)

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

(defmethod api-response
  [:reset :success]
  [_ response])

(defmethod api-response
  [:reset :failure]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/message :string/api-failure))

(defmethod api-response
  [:reset-complete :success]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/pending? false)
  (data/swap-app-state! :app/login assoc :login/reset-complete? true))

(defmethod api-response
  [:reset-complete :failure]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/pending? false)
  (if (= 400 (:status response))
    (data/swap-app-state! :app/login assoc :login/message :string/sign-in-failure)
    (data/swap-app-state! :app/login assoc :login/message :string/api-failure)))

(defmethod api-response
  [:signup :success]
  [{:keys [opts]} _]
  (data/swap-app-state! :app/login assoc :login/message nil)
  (login (:username opts) (:password opts)))

(defmethod api-response
  [:signup :failure]
  [_ response]
  (data/swap-app-state! :app/login assoc :login/pending? false)
  (if (= 400 (:status response))
    (data/swap-app-state! :app/login assoc :login/message :string/sign-up-failure)
    (data/swap-app-state! :app/login assoc :login/message :string/api-failure)))

(defn route-api-response
  ([event opts]
   (fn [status response]
     (api-response {:event event :status status :opts opts} response)))
  ([event]
   (route-api-response event nil)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn login
  [email pass]
  (let [args {:username email :password pass}]
    (data/swap-app-state! :app/login assoc :login/pending? true)
    (POST (str (if (:gateway/secure? @data/config) "https://" "http://")
               (:gateway/address @data/config) "/login")
          {:id :login
           :params (s/validate Login args)
           :result-cb (route-api-response :login)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query Response

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :groups/search
  [[_ data]]
  (data/swap-app-state! :app/user assoc :user/group-search-filtered (:items data))
  (add-groups-to-cache! (:items data)))

(defmethod on-query-response
  :groups/by-ids
  [[_ {:keys [items]}]]
  (add-groups-to-cache! items))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn groups-by-ids
  [group-ids]
  (data/query {:groups/by-ids [[group-ids] (utils/keys* ws/GroupSchema)]} on-query-response))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti handle
  (fn [event args] event))

(defmethod handle :login
  [_ {:keys [email pass]}]
  (login email pass))

(defmethod handle :logout
  [event _]
  (data/reset-everything!))

(defmethod handle :reset-message
  [event _]
  (data/swap-app-state! :app/login assoc :login/message nil))

(defmethod handle :reset-password
  [event username]
  (POST (str (if (:gateway/secure? @data/config) "https://" "http://")
             (:gateway/address @data/config) "/reset")
        {:id :reset
         :params {:username username}
         :result-cb (route-api-response :reset)}))

(defmethod handle :reset-password-complete
  [event {:keys [username reset-code passwords]}]
  (if (not (apply = passwords))
    (data/swap-app-state! :app/login assoc :login/message :string/sign-up-error-passwords-match)
    (do
      (data/swap-app-state! :app/login assoc :login/message nil)
      (data/swap-app-state! :app/login assoc :login/pending? true)
      (data/swap-app-state! :app/login assoc :login/reset-complete? false)
      (POST (str (if (:gateway/secure? @data/config) "https://" "http://")
                 (:gateway/address @data/config) "/complete-reset")
            {:id :reset-complete
             :params {:username username
                      :password (first passwords)
                      :reset-code reset-code}
             :result-cb (route-api-response :reset-complete)}))))

(defmethod handle :refresh-groups
  [event _]
  (data/query {:groups/search [[] (utils/keys* ws/GroupSchema)]} on-query-response))

(defmethod handle :groups-by-ids
  [event {:keys [ids]}]
  (groups-by-ids ids))

(defmethod handle :search-groups
  [event {:keys [search]}]
  (let [groups (vals (data/get-app-state :app/group-cache))
        filtered-groups (filter
                         #(gstring/caseInsensitiveContains
                           (:kixi.group/name %)
                           search) groups)]
    (data/swap-app-state! :app/user assoc :user/group-search-filtered filtered-groups)))

(defmethod handle :signup
  [event {:keys [usernames passwords] :as payload}]
  (if-let [error (cond
                   (not (apply = usernames)) :string/sign-up-error-usernames-match
                   (not (apply = passwords)) :string/sign-up-error-passwords-match
                   :else nil)]
    (data/swap-app-state! :app/login assoc :login/message error)
    (let [p (-> payload
                (dissoc :usernames
                        :passwords)
                (assoc :username (first usernames)
                       :password (first passwords)))]
      (data/swap-app-state! :app/login assoc :login/pending? true)
      (POST (str (if (:gateway/secure? @data/config) "https://" "http://")
                 (:gateway/address @data/config) "/signup")
            {:id event
             :params (s/validate SignUp p)
             :result-cb (route-api-response event p)}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce subscriptions
  (do
    (data/subscribe-topic :data/app-state-restored #(login-success! nil))))
