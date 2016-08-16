(ns witan.ui.style.components.topology
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#topology
             [:#right-bar
              {:margin (px 16)
               :position :fixed
               :top (px 0)
               :right (px 16)}]
             [:#heading
              {:margin-left (px 96)
               :position :fixed
               :top (px 0)}
              [:h1
               {:margin-top (em 0.5)}]]
             [:#content
              {:margin-top (em 2)}]]

            [:.add-model-widget
             [:.pure-button
              {:background-color colour/button-view}]]])