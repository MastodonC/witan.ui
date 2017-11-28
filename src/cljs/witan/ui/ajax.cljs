(ns witan.ui.ajax
  (:require [ajax.core :as ajax]
            [ajax.protocols :refer [AjaxImpl]]
            [cljs.core.async :refer [<! chan put! close! timeout]]
            [goog.net.XhrIo]
            [goog.net.EventType]
            [goog.events :as events])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]
                   [cljs-log.core :as log]))

(defn- handle-response
  [status id result-cb response]
  (when (and (= status :failure) (not= id :token-test))
    (log/severe "An API error occurred: " status id response))
  (when result-cb
    (result-cb status response)))

(defn- request
  [method-fn {:keys [id params result-cb auth suppress-error?]}]
  (method-fn {:params          params
              :handler         (partial handle-response :success id result-cb)
              :error-handler   (when-not suppress-error? (partial handle-response :failure id result-cb))
              :format          :transit
              :response-format :transit
              :keywords?       true
              :headers         auth}))

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

(defn get-headers
  [response]
  (as-> (ajax.protocols/-get-all-headers response) h
    (reduce-kv (fn [a k v] (assoc a (clojure.string/lower-case k) v)) {} h)
    (update h "vary" #(when % (clojure.string/lower-case %)))))

(defn s3-upload [uri {:keys [params] :as args}]
  (log/debug "PUT" uri params)
  (ajax/ajax-request
   (merge
    {:uri             uri
     :method          :put
     :format          (ajax/text-request-format)
     :response-format {:read get-headers :description "headers"}}
    args)))
