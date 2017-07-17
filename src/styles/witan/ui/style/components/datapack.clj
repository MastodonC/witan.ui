(ns witan.ui.style.components.datapack
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]
            [witan.ui.style.fonts :as fonts]
            [witan.ui.style.util :refer [transition]]))

(def style
  [[:#create-datapack-view
    {:width (percent 100)
     :height (percent 100)
     :overflow-y :auto
     :overflow-x :hidden}
    [:#create-datapack-files-table
     [:table
      {:table-layout :fixed}]
     [:.shared-table :tbody :tr
      {:cursor :default}
      [:&:hover
       {:background-color 'transparent}]]]
    [:.container
     {:position :relative
      :max-width (px 1024)
      :width (percent 100)}
     [:div.hero-notification
      {:padding (px 10)
       :margin (px 4)}]]
    [:.datapack-edit-title
     [:input
      {:margin-bottom (em 1)}]]
    [:.datapack-edit-sharing :.datapack-edit-title :.datapack-edit-file
     {:width (percent 100)}]
    [:.datapack-edit-title :.datapack-edit-file
     [:input
      {:width (percent 100)}]]
    [:.sharing-summary
     [:i
      {:margin-right (em 0.3)}]]
    [:div.error
     {:margin-left (em 1)}]]])
