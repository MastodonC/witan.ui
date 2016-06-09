(ns witan.ui.ajax
  (:require [ajax.core :as ajax])
  (:require-macros [cljs-log.core :as log]))

(def endpoint
  ;; TODO add env override
  "http://localhost:30015")

(defn- handle-response
  [status id result-cb response]
  (when (and (= status :failure) (not= id :token-test))
    (log/severe "An API error occurred: " status id response))
  (when result-cb
    (result-cb status response)))

(defn- request
  [method-fn {:keys [id params result-cb auth]}]
  (method-fn {:params params
              :handler (partial handle-response :success id result-cb)
              :error-handler (partial handle-response :failure id result-cb)
              :format :json
              :response-format :json
              :keywords? true
              :headers auth}))

(defn GET
  [method {:keys [params] :as args}]
  (log/debug "GET" method params)
  (request (partial ajax/GET method) args))

(defn POST
  [method {:keys [params] :as args}]
  (log/debug "POST" method params)
  (request (partial ajax/POST method) args))

;; {"Authorization" (str "Token " @api-token)}
