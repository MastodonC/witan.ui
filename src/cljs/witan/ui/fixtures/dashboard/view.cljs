(ns ^:figwheel-always witan.ui.fixtures.dashboard.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [clojure.string :as str]
              ;;
              [witan.ui.widgets :as widgets]
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
                selected-forecast-id (:forecast/forecast-id selected)
                selected-version (:forecast/version selected)
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
                         {:opts {:on-input #(venue/raise! %1 :event/filter-forecasts %2)}})
               [:ul.pure-menu-list
                [:li.witan-menu-item.pure-menu-item
                 [:a {:href (venue/get-route :views/new-forecast)}
                  [:button.pure-button.button-success
                   [:i.fa.fa-plus]]]]
                (if (and (not-empty selected) is-top-level?)
                  [:li.witan-menu-item.pure-menu-item
                   [:a {:href (venue/get-route :views/forecast {:id selected-forecast-id :version selected-version :action "input"})}
                    [:button.pure-button.button-error
                     [:i.fa.fa-pencil]]]])
                (if (seq selected)
                  [:li.witan-menu-item.pure-menu-item
                   [:a {:href "#"}
                    [:button.pure-button.button-warning
                     [:i.fa.fa-copy]]]])
                (if (seq selected)
                  [:li.witan-menu-item.pure-menu-item
                   [:a {:href (venue/get-route :views/forecast {:id selected-forecast-id :version selected-version :action "output"})}
                    [:button.pure-button.button-primary
                     [:i.fa.fa-download]]]])
                (if (seq selected)
                  [:li.witan-menu-item.pure-menu-item
                   [:a {:href (venue/get-route :views/share {:id selected-id})}
                    [:button.pure-button.button-primary
                     [:i.fa.fa-share-alt]]]])]]]))))

(defcomponent view
  [cursor owner]
  (render [_]
          (html
           (if (:refreshing? cursor)
             [:i.fa.fa-refresh.fa-spin]
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
                 (for [[x width class] [[:forecast-name "35%"]
                                        [:forecast-owner "25%" "text-center"]
                                        [:forecast-version "20%"]
                                        [:forecast-lastmodified "20%" "text-center"]]]
                   [:th {:key (name x) :class class :style {:width width}} (get-string x)])]
                [:tbody
                 (om/build-all widgets/forecast-tr
                               (map #(as-forecast-tr cursor %) (:forecasts cursor))
                               {:key  :forecast/version-id
                                :opts {:on-click        #(venue/raise! %1 %2 %3)
                                       :on-double-click #(when-not (:forecast/descendant-id %2)
                                                           (goto-window-location!
                                                            (venue/get-route :views/forecast {:id (:forecast/forecast-id %2) :version (:forecast/version %2) :action "input"})))}})]]]]))))
