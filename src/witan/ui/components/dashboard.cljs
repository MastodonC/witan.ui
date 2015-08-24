(ns ^:figwheel-always witan.ui.components.dashboard
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
              ;;
            [witan.ui.widgets :as widgets]
            [witan.schema.core :refer [Projection]]
            [witan.ui.data :refer [get-string]]
            [witan.ui.async :refer [raise!]]
            [witan.ui.refs :as refs]
            [witan.ui.util :refer [goto-window-location!]]))

(defn get-selected-projection
  [cursor]
  (some #(if (= (:id %) (-> cursor :projections-meta :selected second)) %) (:projections cursor)))

(defcomponent
  dash-header
  [[selected top-level] owner]
  (render [_]
          (let [selected-id (:id selected)
                is-top-level? (contains? top-level selected-id)]
            (html
             [:div.pure-menu.pure-menu-horizontal.witan-dash-heading
              [:h1
               (i/capitalize (get-string :projections))]
              (om/build widgets/search-input
                        (str (get-string :filter) " " (get-string :projections))
                        {:opts {:on-input #(raise! %1 :event/filter-projections %2)}})
              [:ul.pure-menu-list
               [:li.witan-menu-item.pure-menu-item
                [:a {:href "#/new-projection"}
                 [:button.pure-button.button-success
                  [:i.fa.fa-plus]]]]
               (if (and (not-empty selected) is-top-level?)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (str "#/projection/" selected-id)}
                   [:button.pure-button.button-warning
                    [:i.fa.fa-pencil]]]])
               (if (not (empty? selected))
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (str "#/projection/" selected-id "/download")}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-download]]]])
               (if (not (empty? selected))
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (str "#/share/" selected-id)}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-share-alt]]]])]]))))

(defcomponent view
  [cursor owner args]
  (render [_]
          (html
           [:div
            (om/build dash-header [(get-selected-projection cursor)
                                   (->> :projections
                                        (-> cursor)
                                        (filter (comp nil? :descendant-id))
                                        (map :id)
                                        set)])
            [:table.pure-table.pure-table-horizontal#witan-dash-projection-list
             [:thead
              [:th] ;; empty, for the tree icon
              [:th (get-string :projection-name)]
              (for [x [:projection-type
                       :projection-owner
                       :projection-version
                       :projection-lastmodified]]
                [:th.text-center (get-string x)])]
             [:tbody
              (om/build-all widgets/projection-tr
                            (:projections cursor)
                            {:key :id
                             :opts {:on-click #(raise! %1 %2 %3)
                                    :on-double-click #(goto-window-location! (str "#/projection/" (:id %2)))}})]]])))
