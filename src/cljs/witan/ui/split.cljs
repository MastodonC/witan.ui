(ns witan.ui.split
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.primary :as primary]
            [witan.ui.secondary :as secondary]
            [witan.ui.utils :as utils]
            [witan.ui.icons :as icons])
  (:require-macros [cljs-log.core :as log]))

(defui Main
  static om/IQuery
  (query [this]
         [:app/route-params])
  Object
  (componentDidMount [this]
                     (js/Split.
                      (clj->js ["#primary" "#secondary"])
                      (clj->js {:direction "vertical"
                                :sizes [50, 50]
                                :gutterSize 8
                                :minSize 200
                                :cursor "row-resize"})))
  (render [this]
          (let [{:keys [app/route-params workspace/primary workspace/secondary]} (om/props this)]
            (sab/html
             [:div#split
              [:div#primary
               (primary/primary-split-view)]
              [:div#secondary
               (secondary/secondary-split-view)]
              #_[:div#loading
                 [:div
                  (icons/cog :x-large :spin :dark)]]]))))
