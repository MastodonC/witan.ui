(ns witan.ui.services.api
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [put! take! chan <! close!]]
            [venue.core :as venue]
            [goog.net.cookies :as cookies])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def ^:private api-token (atom nil))
(def token-name "tkn")

(defn save-token!
  [token]
  (reset! api-token token)
  (.set goog.net.cookies token-name token -1))

(defmulti response-handler
  (fn [result response cursor] result))

(defmulti service-m
  (fn [event args result-ch] event))

(defmulti api-response
  (fn [event-status response] event-status))

(defn local-endpoint
  [method]
  (let [env-url (cljs-env :api-url)
        api-url (or env-url "http://localhost:3000")]
    (str api-url "/api" method)))

(defn- handle-response
  [status event result-ch response]
  (if (and (= status :failure) (not= event :token-test))
    (log/severe "An API error occurred: " event))
  (let [result (api-response [event status] (clojure.walk/keywordize-keys response))]
    (when result-ch
      (put! result-ch [status result]))))

(defn POST
  [event method params result-ch]
  (log/debug "POST" method params)
  (ajax/POST (local-endpoint method)
             {:params params
              :handler (partial handle-response :success event result-ch)
              :error-handler (partial handle-response :failure event result-ch)
              :format :json
              :headers {"Authorization" (str "Token " @api-token)}}))

(defn GET
  [event method params result-ch]
  (log/debug "GET" method params)
  (ajax/GET (local-endpoint method)
            {:params params
             :handler (partial handle-response :success event result-ch)
             :error-handler (partial handle-response :failure event result-ch)
             :format :json
             :headers {"Authorization" (str "Token " @api-token)}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-initialise
  []
  (if-let [token (.get goog.net.cookies token-name)]
    (do
      (reset! api-token token)
      (GET :token-test "/" {} nil))
    (log/debug "No existing token was found.")))

(defn request-handler
  [event args result-ch]
  (if (or (= event :login) @api-token)
    (service-m event args result-ch)
    (do
      (log/warn "An API request was received but there is no token so the outbound call will not be made.")
      (put! result-ch [:failure :no-token]))))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler request args response-ch))
    venue/IHandleResponse
    (handle-response [owner outcome event response cursor]
      (response-handler [event outcome] response cursor))
    venue/IInitialise
    (initialise [owner _]
      (on-initialise))))

(defmethod service-m
  :login
  [event [email pass] result-ch]
  (POST event "/login" {:username email :password pass} result-ch))

(defmethod service-m
  :get-forecasts
  [event _ result-ch]
  (GET event "/forecasts" nil result-ch))

(defmethod service-m
  :get-forecast
  [event id result-ch]
  (GET event (str "/forecasts/" id) nil result-ch))

(defmethod service-m
  :get-user
  [event id result-ch]
  (GET event "/me" nil result-ch))

(defmethod service-m
  :logout
  [event id result-ch]
  (log/info "Logging out...")
  (save-token! nil)
  (venue/publish! :api/user-logged-out)
  (put! result-ch [:success nil]))

(defmethod service-m
  :get-models
  [event id result-ch]
  (GET event "/models" nil result-ch))

(defmethod service-m
  :create-forecast
  [event args result-ch]
  (POST event "/forecasts" args result-ch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- login!
  [response]
  (if-let [token (:token response)]
    (do
      (log/info "Login success.")
      (save-token! token)
      (venue/publish! :api/user-logged-in)
      true)
    (do
      (log/info "Login failed.")
      (log/debug "Response:" response)
      false)))

(defmethod api-response
  [:login :success]
  [_ response]
  (login! response))

(defmethod api-response
  [:token-test :success]
  [_ response]
  (login! {:token @api-token}))

(defmethod api-response
  :default
  [event response] response)
