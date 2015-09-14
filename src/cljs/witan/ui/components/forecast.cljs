(ns ^:figwheel-always witan.ui.components.forecast
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
    ;;
            [witan.ui.widgets :as widgets]
            [witan.ui.components.model-diagram :as model-diagram]
            [witan.schema.core :refer [Forecast]]
            [witan.ui.async :refer [raise!]]
            [witan.ui.refs :as refs]
            [witan.ui.nav :as nav]))

(def valid-actions
  #{:input
    :output
    :model})

;; There's probably a more elegant way to do this
(defn next-action
  [action]
  (case action
    "input" "model"
    "model" "output"
    "output" "output"))

(defn previous-action
  [action]
  (case action
    "input" "input"
    "model" "input"
    "output" "model"))

(defcomponent
  header
  [forecast owner]
  (render [_]
    (html
      [:div.pure-u-1.witan-pw-header {:key "witan-pw-header"} ;; this key doesn't currently appear
       [:h1 (:name forecast)]])))

(defmulti action-view
          (fn [[action forecast] owner] action))

(defcomponentmethod action-view
  :input
  [[action forecast] owner]
  (render [_]
    (html
      [:div "Input view"])))

(defcomponentmethod action-view
  :output
  [[action forecast] owner]
  (render [_]
    (html
      [:div "Output view"])))

(defcomponentmethod action-view
  :model
  [[action forecast] owner]
  (render [_]
    (html
      [:div "Model view"])))

(defcomponent view
  [cursor owner]
  (render [_]
    (let [{:keys [id action]} (-> cursor :view-state :forecast-wizard)
          kaction (keyword action)
          forecast (some #(if (= (:id %) id) %) (:forecasts cursor))
          ;; this is directly included in the forecast's data for now. More realistically
          ;; it would be derived from input and output information in the forecast.
          model-conf (merge {:action kaction} (select-keys forecast [:n-inputs :n-outputs]))]
      (html
        [:div.pure-g
         (om/build header forecast)
         [:div.pure-u-1-12 {:key "forecast-left"}
          [:div.witan-pw-nav-button
           [:a {:href (nav/forecast-wizard {:id id :action (previous-action action)})}
            [:i.fa.fa-chevron-left.fa-lg]]]]
         [:div.pure-u-5-6.witan-model-diagram {:key "forecast-centre"}
          (om/build model-diagram/diagram model-conf)]
         [:div.pure-u-1-12 {:key "forecast-right"}
          [:div.witan-pw-nav-button
           [:a {:href (nav/forecast-wizard {:id id :action (next-action action)})}
            [:i.fa.fa-chevron-right.fa-lg]]]]
         (if-not (contains? valid-actions kaction)
           [:div.pure-u-1 [:span "Unknown forecast action"]]
           [:div.pure-u-1 {:key "forecast-header"}
            [:div.witan-pw-area-header
             [:div
              {:class action}
              [:h2 (i/capitalize action)]]]
            (om/build action-view [kaction forecast])])]))))
