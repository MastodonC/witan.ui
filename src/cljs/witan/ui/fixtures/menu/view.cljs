(ns ^:figwheel-always witan.ui.fixtures.menu.view
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
              ;;
            [witan.schema.core :refer [Forecast]]
            [witan.ui.strings :refer [get-string]]))

(defcomponent
  view
  [cursor owner & opts]
  (render [_]
          (html
            [:div#witan-menu
             [:div.pure-menu.pure-menu-horizontal
              [:a.pure-menu-heading {:href "#"} (get-string :witan-title)]
              [:ul.pure-menu-list
               [:li.witan-menu-item.pure-menu-item
                [:a.pure-menu-link "Ron Burgundy"]]
               [:li.witan-menu-item.pure-menu-item
                [:a.pure-menu-link
                 [:i.fa.fa-user]]]
               [:li.witan-menu-item.pure-menu-item
                [:a.pure-menu-link
                 [:i.fa.fa-users]]]]]])))
