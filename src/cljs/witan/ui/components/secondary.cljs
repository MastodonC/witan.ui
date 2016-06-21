(ns witan.ui.components.secondary
  (:require [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data]))

(defn switcher
  [{:keys [titles selected-idx on-select]}]
  [:div.secondary-switcher
   (for [[idx title] (map-indexed vector titles)]
     [:button.pure-button
      {:style {:width (str (/ 100 (count titles)) "%")}
       :class (when (= selected-idx idx) "selected")
       :on-click #(when on-select (on-select idx))}
      title])])

(defn view
  [this]
  (let [{:keys [secondary/view-selected]
         :or {secondary/view-selected 0}} (om/props this)]
    [:div#primary
     [:div#switcher
      (switcher {:titles [(get-string :string/workspace-data-view)
                          (get-string :string/workspace-config-view)
                          (get-string :string/workspace-history-view)]
                 :selected-idx view-selected
                 :on-select #(data/transact! this 'change/secondary-view! {:idx %})})]]))
