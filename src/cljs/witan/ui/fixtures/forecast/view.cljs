(ns ^:figwheel-always witan.ui.fixtures.forecast.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [schema.core :as s :include-macros true]
              ;;
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.components.model-diagram :as model-diagram]
              [witan.schema.core :refer [Forecast]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(def valid-actions
  {:input  [nil :model]
   :output [:model nil]
   :model  [:input :output]})


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
          (let [new? (= version 0)]
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
  (fn [[action forecast] owner] action))

(defcomponentmethod action-view
  :input
  [[action forecast] owner]
  (render [_]
          (html
           [:div
            [:p (get-string :input-intro)]])))

(defcomponentmethod action-view
  :output
  [[action forecast] owner]
  (render [_]
          (html
           [:div
            [:p (get-string :output-intro)]])))

(defcomponentmethod action-view
  :model
  [[action forecast] owner]
  (render [_]
          (html
           [:div
            [:p (get-string :model-intro)]])))

(defcomponent view
  [{:keys [id action forecast version edited-forecast]} owner]
  (render [_]
          (let [kaction (keyword action)
                ;; this is directly included in the forecast's data for now. More realistically
                ;; it would be derived from input and output information in the forecast.
                model-conf (merge {:action kaction} (select-keys forecast [:n-inputs :n-outputs]))
                next-action (next-action kaction)
                prev-action (previous-action kaction)
                in-progress? (:forecast/in-progress? forecast)]
            (html
             (if-not forecast
               [:i.fa.fa-refresh.fa-spin]
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
                 [:div.pure-u-5-6.witan-model-diagram {:key "forecast-centre"}
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
                       {:key "witan-pw-edits-button-create"}
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
                     {:class action
                      :key (str action "-key")}
                     [:h2 (let [action-name (i/capitalize action)]
                            (if (= action "model")
                              action-name
                              (i/plural action-name)))
                      (when in-progress?
                        [:i.fa.fa-lock {:key (str action "-locked-key")
                                        :style {:margin-left "0.6em"}}])]]
                    [:div#witan-pw-area
                     {:key "witan-pw-area"}
                     (om/build action-view [kaction forecast])]])]])))))
