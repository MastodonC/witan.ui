(ns ^:figwheel-always witan.ui.components.dashboard
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
              ;;
            [witan.ui.widgets :as widgets]
            [witan.schema.core :refer [Forecast]]
            [witan.ui.data :refer [get-string]]
            [witan.ui.async :refer [raise!]]
            [witan.ui.refs :as refs]
            [witan.ui.util :refer [goto-window-location!]]
            [witan.ui.nav :as nav]))

(defn get-selected-forecast
  [cursor]
  (some #(if (= (:id %) (-> cursor :forecasts-meta :selected second)) %) (:forecasts cursor)))

(defcomponent
  header
  [[selected top-level] owner]
  (render [_]
          (let [selected-id (:id selected)
                is-top-level? (contains? top-level selected-id)]
            (html
             [:div.pure-menu.pure-menu-horizontal.witan-dash-heading
              [:h1
               (i/capitalize (get-string :forecasts))]
              (om/build widgets/search-input
                        (str (get-string :filter) " " (get-string :forecasts))
                        {:opts {:on-input #(raise! %1 :event/filter-forecasts %2)}})
              [:ul.pure-menu-list
               [:li.witan-menu-item.pure-menu-item
                [:a {:href (nav/new-forecast)}
                 [:button.pure-button.button-success
                  [:i.fa.fa-plus]]]]
               (if (and (not-empty selected) is-top-level?)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (nav/forecast-wizard {:id selected-id :action "input"})}
                   [:button.pure-button.button-warning
                    [:i.fa.fa-pencil]]]])
               (if (not (empty? selected))
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (nav/forecast-wizard {:id selected-id :action "output"})}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-download]]]])
               (if (not (empty? selected))
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (nav/share {:id selected-id})}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-share-alt]]]])]]))))

(defcomponent view
  [cursor owner args]
  (render [_]
          (html
           [:div
            (om/build header [(get-selected-forecast cursor)
                                   (->> :forecasts
                                        (-> cursor)
                                        (filter (comp nil? :descendant-id))
                                        (map :id)
                                        set)])
            [:table.pure-table.pure-table-horizontal#witan-dash-forecast-list
             [:thead
              [:th] ;; empty, for the tree icon
              [:th (get-string :forecast-name)]
              (for [x [:forecast-type
                       :forecast-owner
                       :forecast-version
                       :forecast-lastmodified]]
                [:th.text-center {:key (name x)} (get-string x)])]
             [:tbody
              (om/build-all widgets/forecast-tr
                            (:forecasts cursor)
                            {:key :id
                             :opts {:on-click #(raise! %1 %2 %3)
                                    :on-double-click #(if (nil? (:descendant-id %2))
                                                        (goto-window-location!
                                                         (nav/forecast-wizard {:id (:id %2) :action "input"})))}})]]])))
