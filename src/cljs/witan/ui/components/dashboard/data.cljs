(ns witan.ui.components.dashboard.data
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.utils :as utils]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data]))

(defn view
  []
  (let [{:keys [about/content]} (data/get-app-state :app/data-dash)]
    [:div.dashboard
     (shared-dash/header {:title :string/data-dash-title
                          :filter-txt :string/data-dash-filter
                          :filter-fn nil})
     [:div.content
      [:ul
       (for [x (range 100)]
         ^{:key x}
         [:li content])]]]))
