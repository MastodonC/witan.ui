(ns witan.ui.style.components.viz
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#viz
             {:position :absolute
              :top (px 60)
              :left (px 60)
              :right (px 10)
              :bottom (px 2)}
             [:iframe {:height (percent 100)}]]
            [:#viz-container
             {:width (percent 100)
              :height (percent 100)}
             [:.buttons
              {:margin (px 16)
               :position :fixed
               :top (px 0)
               :right (px 16)
               :z-index 1}
              [:button :span
               {:margin (px 4)}]
              [:span
               {:background-color colour/body-bg}]]]
            [:#viz-placeholder
             {:margin-top (em 2)}]])
