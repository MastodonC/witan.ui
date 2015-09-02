(ns witan.ring.handler
  (:require [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn not-found [request]
  {:status 404
   :headers {"Content-Type" "text/plain"}
   :body "Not found"})

(defn wrap-dir-index [handler]
  (fn [req]
    (handler
     (update-in req [:uri]
                #(if (= "/" %) "/index.html" %)))))

(def app
  (-> not-found
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-dir-index)))
