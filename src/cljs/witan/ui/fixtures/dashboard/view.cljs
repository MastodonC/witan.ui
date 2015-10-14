(ns ^:figwheel-always witan.ui.fixtures.dashboard.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [clojure.string :as str]
              [schema.core :as s :include-macros true]
              ;;
              [witan.ui.widgets :as widgets]
              [witan.schema.core :refer [Forecast]]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.util :refer [goto-window-location!]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defn get-selected-forecast
  [cursor]
  (some #(if (= (:forecast/version-id %) (-> cursor :selected second)) %) (:forecasts cursor)))

(defn as-forecast-tr
  [cursor forecast]
  (let [selected-forecast     (:selected cursor)
        ancestor-set          (set (map second (:has-ancestors cursor)))
        expanded-set          (set (map second (:expanded cursor)))
        is-selected-forecast? (= (:forecast/version-id forecast) (second selected-forecast))
        has-ancestor?         (contains? ancestor-set (:forecast/version-id forecast))
        is-expanded?          (contains? expanded-set (:forecast/version-id forecast))
        has-descendant?       (not (nil? (:forecast/descendant-id forecast)))]
    (assoc forecast
           :has-ancestor?         has-ancestor?
           :is-selected-forecast? is-selected-forecast?
           :is-expanded?          is-expanded?
           :has-descendant?       has-descendant?)))

(defcomponent
  header
  [[selected top-level] owner]
  (render [_]
          (let [selected-id (:forecast/version-id selected)
                is-top-level? (contains? top-level selected-id)]
            (html
             [:div.pure-menu.pure-menu-horizontal.witan-dash-heading
              [:div.witan-page-heading
               [:h1
                (get-string :forecast)]
               (om/build widgets/search-input
                         (str (get-string :filter) " " (->> :forecast
                                                           get-string
                                                           i/plural
                                                           str/lower-case))
                         {:opts {:on-input #(venue/raise! %1 :event/filter-forecasts %2)}})]
              [:ul.pure-menu-list
               [:li.witan-menu-item.pure-menu-item
                [:a {:href (venue/get-route :views/new-forecast)}
                 [:button.pure-button.button-success
                  [:i.fa.fa-plus]]]]
               (if (and (not-empty selected) is-top-level?)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (venue/get-route :views/forecast {:id selected-id :action "input"})}
                   [:button.pure-button.button-error
                    [:i.fa.fa-pencil]]]])
               (if (seq selected)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href "#"}
                   [:button.pure-button.button-warning
                    [:i.fa.fa-copy]]]])
               (if (seq selected)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (venue/get-route :views/forecast {:id selected-id :action "output"})}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-download]]]])
               (if (seq selected)
                 [:li.witan-menu-item.pure-menu-item
                  [:a {:href (venue/get-route :views/share {:id selected-id})}
                   [:button.pure-button.button-primary
                    [:i.fa.fa-share-alt]]]])]]))))

(defcomponent view
  [cursor owner]
  (render [_]
          (html
           [:div
            [:div#forecasts-view
             (om/build header [(get-selected-forecast cursor)
                               (->> :forecasts
                                    cursor
                                    (remove :forecast/descendant-id)
                                    (map :forecast/version-id)
                                    set)])
             [:table.pure-table.pure-table-horizontal#witan-dash-forecast-list
              [:thead
               [:th {:key "forecast-tree"}] ;; empty, for the tree icon
               [:th {:key "forecast-name"} (get-string :forecast-name)]
               (for [x [:forecast-owner
                        :forecast-version
                        :forecast-lastmodified]]
                 [:th.text-center {:key (name x)} (get-string x)])]
              [:tbody
               (om/build-all widgets/forecast-tr
                             (map #(as-forecast-tr cursor %) (:forecasts cursor))
                             {:key  :forecast/version-id
                              :opts {:on-click        #(venue/raise! %1 %2 %3)
                                     :on-double-click #(when-not (:forecast/descendant-id %2)
                                                         (goto-window-location!
                                                          (venue/get-route :views/forecast {:id (:forecast/version-id %2) :action "input"})))}})]]]
            (when (:refreshing? cursor)
              [:div.view-overlay.trans-bg
               [:div#loading
                [:i.fa.fa-refresh.fa-2x.fa-spin]]])])))
