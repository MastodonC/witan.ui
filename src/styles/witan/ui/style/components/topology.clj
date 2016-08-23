(ns witan.ui.style.components.topology
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#topology
             [:#right-bar
              {:margin (px 16)
               :position :fixed
               :top (px 0)
               :right (px 16)}
              [:button
               {:margin (px 4)}]]
             [:#content
              {:margin-top (em 2)}]]

            [:.add-model-widget
             [:.pure-button
              {:background-color colour/button-view}]]])
