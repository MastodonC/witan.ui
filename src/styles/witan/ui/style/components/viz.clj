(ns witan.ui.style.components.viz
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#viz
             {:margin [[(px 60) (px 10) (px 0) (px 100)]]
              :height (percent 100)}]
            [:#viz-placeholder
             {
              :margin-top (em 2)}]])
