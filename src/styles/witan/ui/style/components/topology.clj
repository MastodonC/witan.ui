(ns witan.ui.style.components.topology
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#primary-content

             [:#topology
              [:#heading
               {:margin-left (px 96)}
               [:h1 {:margin-top (em 0.5)}]]
              [:#right-bar
               {:position :absolute
                :right (em 0)
                :top (em 0.2)
                :margin (em 0.5)
                :padding (em 1)
                :background-color colour/topology-right-bar-bg
                :box-shadow [[(px 2) (px 2) (px 4) colour/box-shadow]]
                :border-radius (px 3)}]
              [:#add-model-dialog
               {:background-color colour/dialog-bg
                :border-radius (em 1)
                :border 0
                :box-shadow [[(px 2) (px 2) (px 20) colour/box-shadow]]
                :width (percent 70)
                :height (percent 70)
                :position :relative}]]
             [:.modal-container
              {:position :absolute
               :top (px 4)
               :right (px 6)
               :left (px 20)}]

             [:.modal-close-button
              {:cursor :pointer
               :position :absolute
               :right (px 0)
               :top (px 0)}]]])
