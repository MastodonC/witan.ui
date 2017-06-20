(ns witan.ui.ajax
  (:require [ajax.core :as ajax]
            [ajax.protocols :refer [AjaxImpl]]
            [cljs.core.async :refer [<! chan put! close! timeout]]
            [goog.net.XhrIo]
            [goog.net.EventType]
            [goog.events :as events])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]
                   [cljs-log.core :as log]))

;; Progres handler borrowed from
;; https://github.com/JulianBirch/cljs-ajax/issues/175
(extend-type goog.net.XhrIo
  AjaxImpl
  (-js-ajax-request
    [this
     {:keys [uri method body headers timeout with-credentials
             response-format progress-handler]
      :or {with-credentials false
           timeout 0}}
     handler]
    (when-let [response-type (:type response-format)]
      (.setResponseType this (name response-type)))
    ;; Check for the existence of a :progress-handler arg and register if it's there
    (when progress-handler
      (doto this (.setProgressEventsEnabled true)
            (.listen goog.net.EventType.UPLOAD_PROGRESS progress-handler)))
    (doto this
      (events/listen goog.net.EventType/COMPLETE
                     #(handler (.-target %)))
      (.setTimeoutInterval timeout)
      (.setWithCredentials with-credentials)
      (.send uri method body (clj->js headers)))))

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

(defn PUT [method {:keys [params] :as args}]
  (log/debug "PUT" method params)
  (request (partial ajax/PUT method) args))

(defn PUT* [method {:keys [params] :as args}]
  (log/debug "PUT" method params)
  (ajax/PUT method args))
