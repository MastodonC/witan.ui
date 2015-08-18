(ns ^:figwheel-always witan.ui.components.dashboard
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
            ;;
            [witan.ui.library :as l]
            [witan.schema.core :refer [Projection]]
            [witan.ui.util :refer [get-string]]
            [witan.ui.async :refer [raise!]]))

(defn selected-projection
  [cursor]
  (om/ref-cursor (:selected-projection cursor)))

(defcomponent
  dash-header
  [cursor owner]
  (render [_]
          (html
           [:div.pure-menu.pure-menu-horizontal.witan-dash-heading
            [:h1
             (i/capitalize (get-string cursor :projections))]
            (om/build l/search-input (str (get-string cursor :filter) " " (get-string cursor :projections)))
            [:ul.pure-menu-list
             [:li.witan-menu-item.pure-menu-item
              ;; single button
              [:a {:href "#/new-projection"}
               [:button.pure-button.button-success
                [:i.fa.fa-plus]]]
              (if (not (empty? (om/observe owner (selected-projection cursor))))
                  ;; additional buttons
                  [:span "more buttons"])]]])))

(defcomponent
  view
  [cursor owner]
  (render [_]
          (html
           [:div
            (om/build dash-header cursor)
            [:table.pure-table.pure-table-horizontal#witan-dash-projection-list
             [:thead
              [:th] ;; empty, for the tree icon
              [:th (get-string cursor :projection-name)]
              (for [x [:projection-type
                       :projection-owner
                       :projection-version
                       :projection-lastmodified]]
                [:th.text-center (get-string cursor x)])]
             [:tbody
              (om/build-all l/projection-tr (:projections cursor) {:key :id
                                                                   :opts {:on-click #(raise! owner :event/test-event 123)}})]]])))
