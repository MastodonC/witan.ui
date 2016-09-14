(ns witan.ui.style.components.data-select
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#data-select
             [:#no-data
              {:margin (em 1)}]
             [:table
              {:width (percent 100)}
              [:tbody
               [:tr
                {:height (px 40)}]]
              [:td
               {:white-space :nowrap}
               [:button
                {:margin-right (px 10)}]
               [:.input-container
                {:width (percent 99)
                 :overflow :hidden
                 :font-size (em 1)
                 :font-family "monospace"}
                [:input
                 {:width (percent 100)}]]
               [:.fake-input
                {:position :absolute
                 :left (px 1)
                 :top (px 0.5)
                 :pointer-events :none
                 :padding [[(em 0.5) (em 0.6)]]
                 :color colour/temp-var-empty}]
               [:.has-input
                {:color colour/temp-var-replacement}]]
              [:th.col-data-key :th.col-data-actions
               {:width (percent 100)}]]]])
