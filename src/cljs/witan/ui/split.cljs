(ns witan.ui.split
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.utils :as utils])
  (:require-macros [cljs-log.core :as log]))

(defui Main
  static om/IQuery
  (query [this]
         [:workspace/min-size :app/route-params])
  Object
  (componentDidMount [this]
                     (let [{:keys [workspace/min-size]} (om/props this)]
                       (js/Split.
                        (clj->js ["#primary" "#secondary"])
                        (clj->js {:direction "vertical"
                                  :sizes [50, 50]
                                  :gutterSize 8
                                  :minSize min-size
                                  :cursor "row-resize"}))))
  (render [this]
          (let [{:keys [app/route-params workspace/min-size]} (om/props this)]
            (sab/html [:div#split-container
                       [:div#primary
                        [:h1 (str "Top: " min-size)]]
                       [:div#secondary
                        [:h1 (str "Bottom: " route-params)]]]))))
