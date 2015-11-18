(ns ^:figwheel-always witan.ui.fixtures.menu.view
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
              ;;
            [witan.ui.strings :refer [get-string]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]))

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
                [:span.text-white [:strong (get-in cursor [:user :name])]]]
               [:li.pure-menu-item {:style {:width "0.5em"}}]
               [:li.witan-menu-item.pure-menu-item
                [:a.pure-menu-link
                 {:on-click #(do
                               (venue/raise! owner :event/logout {})
                               (.preventDefault %))}
                 [:small (get-string :logout)]]]]]])))
