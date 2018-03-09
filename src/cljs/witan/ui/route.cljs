(ns witan.ui.route
  (:require [cljs.core.async :refer [<! chan put!]]
            [bidi.bidi :as bidi]
            [witan.ui.data :as data]
            [accountant.core :as accountant]
            [cemerick.url :as url])
  (:require-macros [cljs-log.core :as log]))

(defonce app-route-chan (chan))
(def default-view :app/data-dash)

(defn path
  []
  (.. js/document -location -pathname))

(def route-patterns
  ["/" {"app" {""                               default-view
               "/"                              default-view
               "/data/"      {"dashboard"       :app/data-dash
                              "create"          :app/data-create
                              "create-datapack" :app/datapack-create
                              [:id ""]          :app/data}
               "/datapack-add" {""              :app/datapack-bundle-add}
               "/workspace/" {"create"          :app/create-workspace
                              "dashboard"       :app/workspace-dash
                              [:id ""]          :app/workspace}
               "/rts/"       {"create"          :app/rts-create
                              "dashboard"       :app/request-to-share
                              [:id ""]          :app/rts
                              [:id "/submit"]   :app/rts-submit}
               "/activity"  {""                 :app/activity}
               "/debug"     {""                 :app/debug}
               "/about"     {""                 :app/about}}

        "reset" {""  default-view
                 "/" default-view}
        "invite" {""  default-view
                  "/" default-view}}])

(defn path-exists?
  [path]
  (boolean (bidi/match-route route-patterns path)))

(defn query-string->map
  ([]
   (query-string->map (.. js/window -location -href)))
  ([url]
   (let [url' (clojure.string/replace url #"/#" "")]
     (reduce-kv (fn [a k v] (assoc a (keyword k) v)) {}
                (:query (url/url url'))))))

(defn dispatch-path!
  ([alt-path]
   (dispatch-path! alt-path true))
  ([alt-path override]
   (let [patht (if override
                 (or (.getToken accountant/history) alt-path)
                 alt-path)
         path (:path (url/url (clojure.string/replace patht #"/#" "")))
         route (if (or (= "/" path)
                       (clojure.string/blank? path))
                 {:handler :app/data-dash} ;; default
                 (bidi/match-route route-patterns path))]
     (if route
       (let [{:keys [handler route-params]} route
             m {:route/path handler
                :route/params route-params
                :route/address path
                :route/query (query-string->map)}]
         (log/debug "Dispatching to route:" path "=>" handler)
         (data/reset-app-state! :app/route m)
         (data/publish-topic :data/route-changed m)
         (put! app-route-chan handler))
       (log/severe "Couldn't match a route to this path:" path)))))

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
   (let []
     (let [path (find-path route args)
           old-route (data/get-app-state :app/route)]
       (if path
         ;; block against reloading the same route
         (when-not (and (= path (:route/address old-route))
                        (= query (:route/query old-route)))
           (log/debug "Navigating to" route args query "=>" path query)
           (accountant/navigate! path query))
         (log/severe "No path was found for route" route args))))))

(defn swap-query-string!
  [fn]
  (let [{:keys [route/query route/address] :as r} (data/get-app-state :app/route)
        m   (fn query)
        ms  (accountant/map->params m)
        uri (str "/#" address "?" ms)]
    (.replaceState js/history nil nil uri)
    (data/swap-app-state! :app/route assoc-in [:route/query] m)))

(defn assoc-query-param-drop-page!
  [k v]
  (swap-query-string! (fn [q]
                        (-> q
                            (assoc k v)
                            (dissoc :page)))))

(defn dissoc-query-params
  [& ks]
  (swap-query-string! (fn [q]
                        (apply dissoc q ks))))

(defn get-query-map
  []
  (:route/query (data/get-app-state :app/route)))

(defn get-query-param
  [k]
  (k (get-query-map)))
