(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [schema.core :as s]
            [witan.ui.data :as data])
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
  [{:keys [id token] :as response}]
  (when response
    (data/swap-app-state! :app/user assoc  :user/id (str id))
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

(defmethod handle :search-groups
  [event {:keys [search]}]
  (data/swap-app-state! :app/user assoc :user/group-search-results
                        [{:kixi.group/name "Bob"
                          :kixi.group/id "a74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :user
                          :kixi.group/emails ["bob@example.com"]}
                         {:kixi.group/name "Bob1"
                          :kixi.group/id "b74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :user
                          :kixi.group/emails ["bob1@example.com"]}
                         {:kixi.group/name "Bob2"
                          :kixi.group/id "c74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :user
                          :kixi.group/emails ["bob2@example.com"]}
                         {:kixi.group/name "Bob3"
                          :kixi.group/id "d74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :user
                          :kixi.group/emails ["bob3@example.com"]}
                         {:kixi.group/name "Bob4"
                          :kixi.group/id "e74f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :user
                          :kixi.group/emails ["bob4@example.com"]}
                         {:kixi.group/name "Alice1"
                          :kixi.group/id "f74f742d-9cb9-4ede-aeaf-f82aa4b6f3a8"
                          :kixi.group/type :user
                          :kixi.group/emails ["alice1@example.com"]}
                         {:kixi.group/name "Alice2"
                          :kixi.group/id "f74f742d-9cb9-4ede-aeaf-f82aa4b6f3a7"
                          :kixi.group/type :user
                          :kixi.group/emails ["alice2@example.com"]}
                         {:kixi.group/name "Alice3"
                          :kixi.group/id "f74f742d-9cb9-4ede-aeaf-f82aa4b6f3a6"
                          :kixi.group/type :user
                          :kixi.group/emails ["alice3@example.com"]}
                         {:kixi.group/name "Alice4"
                          :kixi.group/id "f74f742d-9cb9-4ede-aeaf-f82aa4b6f3a5"
                          :kixi.group/type :user
                          :kixi.group/emails ["alice4@example.com"]}
                         {:kixi.group/name "Alice5"
                          :kixi.group/id "f74f742d-9cb9-4ede-aeaf-f82aa4b6f3a4"
                          :kixi.group/type :user
                          :kixi.group/emails ["alice5@example.com"]}
                         {:kixi.group/name "GLA Demography"
                          :kixi.group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6f3a9"
                          :kixi.group/type :group
                          :kixi.group/emails ["alice1@example.com"
                                              "bob1@example.com"]}
                         {:kixi.group/name "Camden Demography"
                          :kixi.group/id "074f742d-9cb9-4ede-aeaf-f82aa4b6329a"
                          :kixi.group/type :group
                          :kixi.group/emails ["alice2@example.com"
                                              "bob2@example.com"]}]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(data/subscribe-topic :data/app-state-restored #(login-success! nil))
