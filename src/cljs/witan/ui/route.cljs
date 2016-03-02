(ns witan.ui.route
  (:require [cljs.core.async :refer [<! chan put!]]
            [om.next :as om]
            [bidi.bidi :as bidi]
            [witan.ui.data :as data]
            [accountant.core :as accountant])
  (:require-macros [cljs-log.core :as log]))

(def app-route-chan (chan))

(def route-patterns
  ["/app/" {"" nil
            "dashboard/" {"data"      :app/data-dash
                          "workspace" :app/workspace-dash}
            "workspace/" {"create"    :app/create-workspace
                          ["id/" :id] :app/workspace}}])

(defn path-exists?
  [path]
  (boolean (bidi/match-route route-patterns path)))

(defn dispatch-path!
  [path]
  (let [route (if (= "/" path)
                {:handler :app/workspace-dash} ;; default
                (bidi/match-route route-patterns path))]
    (if route
      (let [{:keys [handler route-params]} route]
        (log/debug "Dispatching to route:" path "=>" handler)
        (om/transact! (data/make-reconciler) `[(change/route! {:route ~handler :route-params ~route-params})])
        (put! app-route-chan handler))
      (log/severe "Couldn't match a route to this path:" path))))

(defn navigate!
  ([route]
   (navigate! route {}))
  ([route args]
   (let [path (apply bidi/path-for route-patterns route (mapcat vec args))]
     (if path
       (do
         (log/info "Navigating to" route args "=>" path)
         (accountant/navigate! path))
       (log/severe "No path was found for route" route args)))))
