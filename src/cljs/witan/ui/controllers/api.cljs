(ns witan.ui.controllers.api
  (:require [ajax.core :as ajax]))

(defn local-endpoint
  [method]
  (str "http://localhost:3000" method))

(defmulti api-request
  (fn [event args cursor] event))

(defmulti api-response
  (fn [event status cursor response] [event status]))

(defn POST
  [event cursor method params]
  (ajax/POST (local-endpoint method)
             {:params params
              :handler (partial api-response event :success cursor)
              :error-handler (partial api-response event :failure cursor) }))



(defn handler
  [[event args] cursor]
  (api-request event args cursor))

(defmethod api-request
  :api/login
  [event args cursor]
  (POST event cursor "/login" args))

(defmethod api-response
  [:api/login :success]
  [event status cursor response]
  (println "GOT A SODDIN RESPONSE"))

(defmethod api-response
  [:api/login :failure]
  [event status cursor response]
  (println event "failed:" response))
