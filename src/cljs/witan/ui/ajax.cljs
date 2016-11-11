(ns witan.ui.ajax
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! chan put! close! timeout]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]
                   [cljs-log.core :as log]))

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
              :format :transit
              :response-format :transit
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
