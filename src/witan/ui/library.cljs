(ns ^:figwheel-always witan.ui.library
    (:require[om.core :as om :include-macros true]
             [om-tools.dom :as dom :include-macros true]
             [om-tools.core :refer-macros [defcomponent]]
             [sablono.core :as html :refer-macros [html]]
             [inflections.core :as i]))

;; search input
(defcomponent
  search-input
  [placeholder owner]
  (render [_]
          (html
           [:form {:class "pure-form"}
            [:div {:class "witan-search-input"}
             [:i {:class "fa fa-search"}]
             [:input {:id "filter-input"
                      :type "text"
                      :placeholder placeholder}]]])))
