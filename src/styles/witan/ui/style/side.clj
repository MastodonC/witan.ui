(ns witan.ui.style.side
  (:require [garden.units :refer [px em]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#side-container
             {:position :relative
              :text-align :center
              :color colour/side-text
              :width values/app-peripheral-width}
             [:.side-element
              {:margin [[(em 1.2) (em 0)]]}
              [:.side-link
               {:cursor :pointer
                :color colour/side-icons-inactive}
               ^:prefix {:user-select :none}
               [:&:hover
                {:color colour/side-text}]]]
             [:.sub-side-element
              [:.material-icons.md-m
               {:font-size (px 16)}]
              [:.side-element
               {:margin [[(em 0.5) (em 0)]]}]]
             [:#side-upper
              {:width values/app-peripheral-width}]
             [:#side-lower
              {:position :fixed
               :bottom 0
               :width values/app-peripheral-width}]]])
