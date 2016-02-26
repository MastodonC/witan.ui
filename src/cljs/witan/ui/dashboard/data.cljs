(ns witan.ui.dashboard.data
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.shared :as shared]
            [witan.ui.utils :as utils]
            [witan.ui.strings :refer [get-string]]))

(defui Main
  static om/IQuery
  (query [this]
         [:about/content])
  Object
  (render [this]
          (let [{:keys [about/content]} (om/props this)]
            (sab/html [:div.dashboard
                       [:div.heading
                        [:h1 (get-string :string/data-dash-title)]
                        (shared/search-filter (get-string :string/data-dash-filter) nil)]
                       [:div.content
                        [:ul
                         (for [x (range 100)]
                           [:li content])]]]))))
