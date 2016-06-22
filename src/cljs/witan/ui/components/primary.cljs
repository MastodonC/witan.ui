(ns witan.ui.components.primary
  (:require [witan.ui.components.icons :as icons]
            [witan.ui.data :as data])
  (:require-macros
   [cljs-log.core :as log]))

(defn switcher
  [{:keys [icon-0 icon-1 selected-idx on-select]}]
  [:div.primary-switcher
   [:div#indicator-container
    [:div#indicator
     {:class (when (= selected-idx 1) "indicator-offset-1")}]]
   [:div.icons
    [:div.icon#icon-0
     {:class (when (= selected-idx 0) "selected")
      :on-click #(when on-select (on-select 0))}
     (icon-0)]
    [:div.icon#icon-1
     {:class (when (= selected-idx 1) "selected")
      :on-click #(when on-select (on-select 1))}
     (icon-1)]]])

(defn view
  [this]
  (let [{:keys [primary/view-selected]
         :or {primary/view-selected 0}} this]
    [:div#primary
     [:div#overlay
      (switcher {:icon-0 (partial icons/topology :dark :medium)
                 :icon-1 (partial icons/visualisation :dark :medium)
                 :selected-idx view-selected
                 :on-select #(data/transact! 'change/primary-view! {:idx ~%})})]]))
