(ns witan.ui.secondary
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            [witan.ui.strings :refer [get-string]]))

(defn switcher
  [{:keys [titles selected-idx on-select]}]
  [:div.secondary-switcher
   (for [[idx title] (map-indexed vector titles)]
     [:button.pure-button
      {:style {:width (str (/ 100 (count titles)) "%")}
       :class (when (= selected-idx idx) "selected")
       :on-click #(when on-select (on-select idx))}
      title])])

(defui Main
  static om/IQuery
  (query [this]
         [:secondary/view-selected])
  Object
  (render [this]
          (let [{:keys [secondary/view-selected]
                 :or {secondary/view-selected 0}} (om/props this)]
            (sab/html
             [:div#primary
              [:div#switcher
               (switcher {:titles [(get-string :string/workspace-data-view)
                                   (get-string :string/workspace-config-view)
                                   (get-string :string/workspace-history-view)]
                          :selected-idx view-selected
                          :on-select #(om/transact! this `[(change/secondary-view! {:idx ~%})])})]]))))

(def secondary-split-view (om/factory Main))
