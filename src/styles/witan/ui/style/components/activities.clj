(ns witan.ui.style.components.activities
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:.activity
             [:.time
              {:color 'grey}]
             [:.message
              {:margin-left (em 0.5)}]]
            [:.activities
             [:hr
              {:height (px 12)
               :border 0
               :border-left [[(px 1) 'solid "#bbb"]]
               :margin [[(px 5) (px 15)]]}]]])
