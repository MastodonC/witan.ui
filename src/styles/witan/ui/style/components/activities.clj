(ns witan.ui.style.components.activities
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#activity-view
             {:height (percent 100)
              :width (percent 100)}
             [:.container
              {:position :relative
               :height (percent 100)
               :width (percent 100)}
              [:.content
               {:padding [[(em 0) (em 1)]]
                :display :flex
                :position :absolute
                :top values/app-peripheral-height
                :right 0
                :left 0
                :bottom 0
                :overflow-y 'auto}]]
             [:.activity
              [:.time
               {:color 'grey}]
              [:.message
               {:margin-left (em 0.5)}]]
             [:.activities
              {:width (percent 100)
               :padding-top (em 1)}
              [:hr
               {:height (px 12)
                :border 0
                :border-left [[(px 1) 'solid "#bbb"]]
                :margin [[(px 5) (px 15)]]}]]]])
