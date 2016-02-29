(ns witan.ui.controllers.user
  (:require [witan.ui.ajax :refer [GET POST]]
            [cljs.core.async :refer [put! take! chan <! close!]]
            [schema.core :as s])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(def Login
  {:username s/Str
   :password (s/constrained s/Str #(> (count %) 5))})

(defmulti handle
  (fn [event owner args] event))

(defmethod handle :login
  [event owner {:keys [email pass]}]
  (let [result-ch (chan)
        args {:username email :password pass}]
    (POST "/login" {:id event :params (s/validate Login args) :result-ch result-ch})))
