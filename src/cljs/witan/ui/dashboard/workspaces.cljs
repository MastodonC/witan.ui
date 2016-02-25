(ns witan.ui.dashboard.workspaces
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.shared :as shared]
            [witan.ui.utils :as utils])
  (:require-macros [cljs-log.core :as log]))

(defui Main
  static om/IQuery
  (query [this]
         [:home/title :home/content])
  Object
  (render [this]
          (let [{:keys [home/title home/content]} (om/props this)]
            (sab/html [:div.dashboard
                       [:div.heading
                        [:h1 title]
                        (shared/search-filter "Filter your workspaces" nil)]
                       [:p content]]))))
