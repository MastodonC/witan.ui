(ns witan.ui.components.dashboard.data
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.utils :as utils]
            [witan.ui.strings :refer [get-string]]))

(defn view
  [this]
  (let [{:keys [about/content]} this]
    [:div.dashboard
     (shared-dash/header {:title :string/data-dash-title
                          :filter-txt :string/data-dash-filter
                          :filter-fn nil})
     [:div.content
      [:ul
       (for [x (range 100)]
         [:li content])]]]))
