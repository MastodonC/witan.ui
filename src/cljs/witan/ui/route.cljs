(ns witan.ui.route
  (:require [cljs.core.async :refer [<! chan put!]]
            [bidi.bidi :as bidi]
            [witan.ui.data :as data]
            [accountant.core :as accountant]
            [cemerick.url :as url])
  (:require-macros [cljs-log.core :as log]))

(defonce app-route-chan (chan))

(defn path
  []
  (.. js/document -location -pathname))

(def route-patterns
  ["/app/" {"" nil
            "data/"      {"dashboard"     :app/data-dash
                          "create"        :app/data-create
                          [:id ""]        :app/data}
            "workspace/" {"create"        :app/create-workspace
                          "dashboard"     :app/workspace-dash
                          [:id ""]        :app/workspace}
            "rts/"       {"create"        :app/rts-create
                          "dashboard"     :app/request-to-share
                          [:id ""]        :app/rts
                          [:id "/submit"] :app/rts-submit}}])

(defn path-exists?
  [path]
  (boolean (bidi/match-route route-patterns path)))

(defn query-string->map
  []
  (reduce-kv (fn [a k v] (assoc a (keyword k) v)) {}
             (:query (url/url (-> js/window .-location .-href)))))

(defn dispatch-path!
  [_]
  (let [path (.getToken accountant/history)
        route (if (= "/" path)
                {:handler :app/data-dash} ;; default
                (bidi/match-route route-patterns path))]
    (if route
      (let [{:keys [handler route-params]} route
            m {:route/path handler
               :route/params route-params
               :route/query (query-string->map)}]
        (log/debug "Dispatching to route:" path "=>" handler)
        (data/reset-app-state! :app/route m)
        (data/publish-topic :data/route-changed m)
        (put! app-route-chan handler))
      (log/severe "Couldn't match a route to this path:" path))))

(defn find-path
  ([route]
   (find-path route {}))
  ([route args]
   (apply bidi/path-for route-patterns route (mapcat vec args))))

(defn navigate!
  ([route]
   (navigate! route {}))
  ([route args]
   (navigate! route args {}))
  ([route args query]
   (let [path (find-path route args)]
     (if path
       (do
         (log/debug "Navigating to" route args "=>" path)
         (accountant/navigate! path query))
       (log/severe "No path was found for route" route args)))))

(defn swap-query-string!
  [fn]
  (let [{:keys [route/query]} (data/get-app-state :app/route)
        m   (fn query)
        ms  (accountant/map->params m)
        uri (str (path) "?" ms)]
    (.replaceState js/history nil nil uri)
    (data/swap-app-state! :app/route assoc-in [:route/query] m)))
