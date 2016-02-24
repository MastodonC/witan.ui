(ns witan.ui.dashboard.data
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.shared :as shared]
            [witan.ui.utils :as utils]))

(defui Main
  static om/IQuery
  (query [this]
         [:about/title :about/content])
  Object
  (render [this]
          (let [{:keys [about/title about/content]} (om/props this)]
            (sab/html [:div.dashboard
                       [:div.heading
                        [:h1 title]
                        (shared/search-filter "Filter your workspaces" nil)]
                       [:div.content
                        [:ul
                         (for [x (range 100)]
                           [:li content])]]]))))
