(ns witan.ui.app
  (:require [cljs.core.async :refer [<! chan put! timeout]]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            [bidi.bidi :as bidi]
            [accountant.core :as accountant]
            ;;
            [witan.ui.dashboard.workspaces :as workspace-dash]
            [witan.ui.dashboard.data :as data-dash]
            [witan.ui.split :as split]
            [witan.ui.utils :as utils]
            [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as am :refer [go go-loop alt!]]))

(defn path
  []
  (.. js/document -location -pathname))

(def app-route-chan (chan))

(def route->component
  {:app/workspace-dash workspace-dash/Main
   :app/data-dash      data-dash/Main
   :app/workspace      split/Main})

(def route->factory
  (zipmap (keys route->component)
          (map om/factory (vals route->component))))

(def route-patterns
  ["/app/" {"" nil
            "dashboard/" {"data"      :app/data-dash
                          "workspace" :app/workspace-dash}
            ["workspace/" :id]        :app/workspace}])

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

(defui Main
  static om/IQueryParams
  (params [this]
          {:route/data []})
  static om/IQuery
  (query [this]
         '[:app/route {:route/data ?route/data}])
  Object
  (componentWillMount [this]
                      (go-loop []
                        (let [current-route (<! app-route-chan)
                              initial-query (om/get-query (route->component current-route))]
                          (om/set-query! this {:params {:route/data initial-query}}))
                        (recur)))
  (render [this]
          (let [{:keys [app/route route/data]} (om/props this)
                active-component (get route->factory route)]
            (active-component data))))
