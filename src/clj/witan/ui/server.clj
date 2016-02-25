(ns witan.ui.server
  (:require [clojure.java.io :as io]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [environ.core :refer [env]]))

(defn index []
  {:status 200
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (io/input-stream (io/resource "public/index.html"))})

(defroutes routes
  (GET "/"      [_] (index))
  (GET "/app/*" [_] (index))
  (resources "/"))

(def handler
  (-> routes
      (wrap-defaults site-defaults)))
