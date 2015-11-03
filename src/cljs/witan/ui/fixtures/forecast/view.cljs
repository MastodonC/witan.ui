(ns ^:figwheel-always witan.ui.fixtures.forecast.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              ;;
              [witan.ui.fixtures.forecast.input-view]
              [witan.ui.fixtures.forecast.output-view]
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.components.model-diagram :as model-diagram]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(def valid-actions ;; NOTE keep them in this order or weird stuff will 'appen.
  {:input  [nil    :model  (-> :input get-string i/plural)]
   :model  [:input :output (-> :model get-string)]
   :output [:model nil     (-> :output get-string i/plural)]})

;; There's probably a more elegant way to do this
(defn next-action
  [kaction]
  (-> valid-actions
      (get kaction)
      second))

(defn previous-action
  [kaction]
  (-> valid-actions
      (get kaction)
      first))

(defcomponent
  header
  [{:keys [forecast/name forecast/version forecast/version-id forecast/in-progress? edited?]} owner]
  (render [_]
          (let [new? (zero? version)]
            (html
             [:div.pure-menu.pure-menu-horizontal.witan-pw-header
              [:div.witan-page-heading
               [:h1 name
                [:em {:class (when new? "version-zero") :key "witan-pw-version"}
                 (get-string :forecast-version " " version)]
                [:div.labels
                 {:key "witan-pw-header-labels"}
                 (when in-progress?
                   [:span.label.label-in-progress.label-small (get-string :in-progress)])
                 (when new?
                   [:span.label.label-new.label-small (get-string :new)])
                 (when edited?
                   [:span.label.label-forecast-changed.label-small (get-string :changed)])]]
               [:ul.pure-menu-list
                [:li.witan-menu-item.pure-menu-item
                 [:a {:href (venue/get-route :views/share {:id 123})}
                  [:button.pure-button.button-success
                   [:i.fa.fa-tag]]]]
                [:li.witan-menu-item.pure-menu-item
                 [:a {:href (venue/get-route :views/share {:id 123})}
                  [:button.pure-button.button-primary
                   [:i.fa.fa-share-alt]]]]]]]))))



(defmulti action-view
  (fn [[action cursor] owner] action))

(defcomponentmethod action-view
  :input
  [[action cursor] owner]
  (render [_]
          (om/build witan.ui.fixtures.forecast.input-view/view [action cursor])))

(defcomponentmethod action-view
  :output
  [[action cursor] owner]
  (render [_]
          (om/build witan.ui.fixtures.forecast.output-view/view [action cursor])))

(defcomponentmethod action-view
  :model
  [[action {:keys [forecast model]}] owner]
  (render [_]
          (html
           [:div
            [:p (get-string :model-intro)]
            [:div.pure-g#witan-pw-action-body
             [:div.pure-u-1-2.text-right
              [:div.padding-1

               [:h2 (get-string :model)]
               [:h3.model-value (:model/name model)]
               [:h2 (get-string :forecast-desc)]
               [:h3.model-value (:model/description model)]
               (when (not-empty (:forecast/property-values forecast))
                 [:div [:h2 (get-string :properties)]
                  (for [{:keys [name value]} (:forecast/property-values forecast)]
                    [:div [:h3.model-value name ": " [:small {:style {:font-weight "normal"}} value]]])])]]
             [:div.pure-u-1-2
              [:div.padding-1
               {:style {:border-left "1px solid silver"}}
               [:h2 (get-string :model-publisher)]
               [:h3.model-value (:model/owner model)]
               [:h2 (get-string :created)]
               [:h3.model-value (:model/created model)]
               [:h2 (get-string :forecast-version)]
               [:h3.model-value 2]]]]])))

(defcomponent view
  [{:keys [id action forecast version edited-forecast error? model creating?] :as cursor} owner]
  (render [_]
          (let [kaction (keyword action)
                model-conf  {:action kaction
                             :n-inputs (-> model :model/input-data count)
                             :n-outputs [(-> model :model/output-data count)];; TODO add grps
                             :stage-names (map (fn [[k v]] (last v)) valid-actions)}
                next-action (next-action kaction)
                prev-action (previous-action kaction)
                in-progress? (:forecast/in-progress? forecast)]
            (html
             (if error?
               [:div
                [:h1 "Error"]
                [:h3 error?]]
               (if (or creating? (not (and forecast model)))
                 [:div.view-overlay
                  [:i.fa.fa-cog.fa-spin.fa-4x]
                  (when creating? [:h2 (get-string :creating-forecast)])]
                 [:div
                  [:div
                   {:key "witan-pw-header-container"}
                   (om/build header (assoc forecast :edited? (-> edited-forecast nil? not)))]
                  [:div.pure-g
                   {:key "witan-pw-body"}
                   [:div.pure-u-1#witan-pw-top-spacer
                    {:key "witan-pw-top-spacer"}]
                   [:a.pure-u-1-12.witan-pw-nav-button
                    (merge {:key (str "forecast-left-" prev-action)}
                           (when prev-action
                             {:href (venue/get-route :views/forecast {:id id :version version :action (name prev-action)})}))
                    (when prev-action [:i.fa.fa-chevron-left.fa-3x])]
                   [:div.pure-u-5-6.witan-model-diagram
                    {:key "forecast-centre"}
                    (when forecast (om/build model-diagram/diagram model-conf))]
                   [:a.pure-u-1-12.witan-pw-nav-button
                    (merge {:key (str "forecast-right-" next-action)}
                           (when next-action
                             {:href (venue/get-route :views/forecast {:id id :version version :action (name next-action)})}))
                    (when next-action [:i.fa.fa-chevron-right.fa-3x])]]

                  (if in-progress?
                    [:div.pure-g#witan-pw-in-prog
                     {:key "witan-pw-in-prog"}
                     [:div.pure-u-1#witan-pw-in-prog-text
                      {:key "witan-pw-in-prog-text"}
                      [:span
                       {:key "witan-pw-in-prog-text-span"}
                       (get-string :forecast-in-progress-text)]
                      [:button.pure-button#refresh
                       {:key "witan-pw-in-prog-button-refresh"
                        :on-click #(do
                                     (venue/raise! owner :refresh-forecast)
                                     (.preventDefault %))}
                       [:span
                        [:i.fa.fa-refresh {:key "witan-pw-in-prog-button-refresh-i"}]
                        [:span {:key "witan-pw-in-prog-button-refresh-span"}
                         (str " " (get-string :refresh-now))]]]]]

                    ;; only show edited if not in-progress?
                    (when edited-forecast
                      [:div.pure-g#witan-pw-edits
                       {:key "witan-pw-edits"}
                       [:div.pure-u-1-2#witan-pw-edits-text
                        {:key "witan-pw-edits-text"}
                        [:span (get-string :forecast-changes-text)]]
                       [:div.pure-u-1-2#witan-pw-edits-buttons
                        {:key "witan-pw-edits-buttons"}
                        [:button.pure-button#create
                         {:key "witan-pw-edits-button-create"
                          :on-click #(do (venue/raise! owner :create-forecast-version)
                                         (.preventDefault %))}
                         [:span
                          [:i.fa.fa-thumbs-o-up {:key "witan-pw-edits-button-create-i"}]
                          [:span {:key "witan-pw-edits-button-create-span"}
                           (str " " (get-string :create-new-forecast))]]]
                        [:button.pure-button#revert
                         {:key "witan-pw-edits-button-revert"
                          :on-click #(do
                                       (venue/raise! owner :revert-forecast)
                                       (.preventDefault %))}
                         [:span
                          [:i.fa.fa-undo {:key "witan-pw-edits-button-revert-i"}]
                          [:span {:key "witan-pw-edits-button-revert-span"}
                           (str " " (get-string :revert-forecast))]]]]]))

                  [:div.pure-g
                   {:key "witan-pw-area-container"}
                   (if-not (contains? (set (keys valid-actions)) kaction)
                     [:div.pure-u-1 [:span "Unknown forecast action"]]
                     [:div.pure-u-1.witan-pw-area-header
                      {:key "witan-pw-area-header"}
                      [:div
                       {:class action}
                       [:h2 (-> valid-actions kaction last)
                        (when in-progress?
                          [:i.fa.fa-lock {:key (str action "-locked-key")
                                          :style {:margin-left "0.6em"}}])]]
                      [:div#witan-pw-area
                       {:key "witan-pw-area"}
                       (om/build action-view [kaction cursor])]])]]))))))
