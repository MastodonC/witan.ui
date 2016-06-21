(ns witan.ui.ajax
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! chan put! close! timeout]])
  (:require-macros [cljs.core.async.macros :as am :refer [go-loop]]
                   [cljs-log.core :as log]))

(def endpoint
  ;; TODO add env override
  "http://localhost:30015/api")

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CQRS

(defn command!
  [command version {:keys [params] :as args}]
  (log/debug "Command (POST)" command version params)
  (request (partial ajax/POST (str endpoint "/command"))
           (assoc args :params {:command command
                                :version version
                                :params params})))

(defn query
  [query {:keys [params] :as args}]
  (log/debug "Query (GET)" query params)
  (request (partial ajax/GET (str endpoint "/query")) args))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Receipt handling

(def receipt-timeout 500)

(defn validate-receipt!
  [{:keys [receipt]} cb]
  (let [c (chan)]
    (go-loop []
      (ajax/GET (str endpoint "/status/" receipt)
                {:format :json
                 :response-format :json
                 :keywords? true
                 :handler (fn [{:keys [status]}] (put! c status))
                 :error-handler #(close! c)})
      (let [result (<! c)]
        (log/debug "result!" result)
        (if (= result "pending")
          (do
            (<! (timeout receipt-timeout))
            (recur)))
        (do
          (cb result receipt)
          (when c
            (close! c)))))))

;; {"Authorization" (str "Token " @api-token)}
