(ns ^:figwheel-always witan.ui.fixtures.dashboard.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [clojure.string :as str]
              ;;
              [witan.ui.util :as util]
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.util :refer [goto-window-location!]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defn get-selected-forecast
  [cursor]
  (some #(if (= (:forecast/version-id %) (-> cursor :selected second)) %) (:forecasts cursor)))

(defcomponent
  forecast-tr
  "Table row for displaying a forecast"
  [forecast owner & opts]
  (render [_]
          (let [{:keys [on-click on-double-click]} (first opts)
                {:keys [is-selected-forecast?
                        has-ancestor?
                        is-expanded?
                        has-descendant?]} forecast
                        classes [[is-selected-forecast? "witan-forecast-table-row-selected"]
                                 [has-descendant? "witan-forecast-table-row-descendant"]]
                        version (:forecast/version forecast)
                        in-progress? (:forecast/in-progress? forecast)
                        new? (zero? version)
                        tag (:forecast/tag forecast)]
            (html
             [:tr.witan-forecast-table-row {:key (:forecast/version-id forecast)
                                            :class (->> classes
                                                        (filter first)
                                                        (map second)
                                                        (interpose " ")
                                                        (apply str))
                                            :on-click (fn [e]
                                                        (if (fn? on-click)
                                                          (if (and
                                                               has-ancestor?
                                                               (util/contains-str (.. e -target -className) "tree-control"))
                                                            (on-click owner :event/toggle-tree-view forecast e)
                                                            (on-click owner :event/select-forecast forecast e)))
                                                        (.preventDefault e))
                                            :on-double-click (fn [e]
                                                               (if (fn? on-double-click)
                                                                 (on-double-click owner forecast e))
                                                               (.preventDefault e))}

              [:td.tree-control (cond
                                  is-expanded? [:i.fa.fa-minus-square-o.tree-control]
                                  has-ancestor? [:i.fa.fa-plus-square-o.tree-control])]
              [:td
               [:span.name.unselectable (:forecast/name forecast)]
               (when (or new? in-progress?)
                 [:div.version-labels
                  {:key "witan-forecast-table-labels-key"}
                  (when in-progress?
                    [:span.unselectable.label.label-in-progress.label-small
                     {:key "witan-forecast-table-labels-in-prog-key"}
                     (get-string :in-progress)])
                  (when new?
                    [:span.unselectable.label.label-new.label-small
                     {:key "witan-forecast-table-label-new-key"}
                     (get-string :new)])])]
              [:td.text-center
               [:span.unselectable (:forecast/owner-name forecast)]]
              [:td
               {:style {:padding-left "2em"}}
               (if tag
                 [:div.tag-labels
                  {:key "witan-forecast-table-tags-key"}
                  [:span.unselectable.label.label-tag.label-small
                   {:key "witan-forecast-table-tag-key"}
                   tag]]
                 [:span.unselectable
                  {:class (when has-descendant? "witan-forecast-table-version-descendant")
                   :key "witan-forecast-table-version-key"}
                  (:forecast/version forecast)])]
              [:td.text-center
               [:span.unselectable (:forecast/created forecast)]]]))))

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
                 (om/build-all forecast-tr
                               (map #(as-forecast-tr cursor %) (:forecasts cursor))
                               {:key  :forecast/version-id
                                :opts {:on-click        #(venue/raise! %1 %2 %3)
                                       :on-double-click #(when-not (:forecast/descendant-id %2)
                                                           (goto-window-location!
                                                            (venue/get-route :views/forecast {:id (:forecast/forecast-id %2) :version (:forecast/version %2) :action "input"})))}})]]]]))))
