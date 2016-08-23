(ns witan.ui.style.components.viz
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#viz
             {:margin [[(px 60) (px 10) (px 0) (px 100)]]
              :height (percent 100)}]
            [:#viz-container
             [:.buttons
              {:margin (px 16)
               :position :fixed
               :top (px 0)
               :right (px 16)}
              [:button :span
               {:margin (px 4)}]]]
            [:#viz-placeholder
             {:margin-top (em 2)}]])
