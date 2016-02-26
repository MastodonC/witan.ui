(ns witan.ui.app
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.dashboard.workspaces :as workspace-dash]
            [witan.ui.dashboard.data :as data-dash]
            [witan.ui.split :as split]
            [witan.ui.utils :as utils]
            [witan.ui.data :as data]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as am :refer [go-loop]]))

(defn path
  []
  (.. js/document -location -pathname))

(def route->component
  {:app/workspace-dash workspace-dash/Main
   :app/data-dash      data-dash/Main
   :app/workspace      split/Main})

(def route->factory
  (zipmap (keys route->component)
          (map om/factory (vals route->component))))

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
                        (let [current-route (<! route/app-route-chan)
                              initial-query (om/get-query (route->component current-route))]
                          (om/set-query! this {:params {:route/data initial-query}}))
                        (recur)))
  (render [this]
          (let [{:keys [app/route route/data]} (om/props this)
                active-component (get route->factory route)]
            (active-component data))))
