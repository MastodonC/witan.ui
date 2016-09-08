(ns witan.ui.style.components.results
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]
            [witan.ui.style.fonts :as fonts]))

(def style [[:#results-header
             [:h1
              {:font-family fonts/base-fonts
               :font-size (em 1.3)
               :height (px 26)
               :padding [[(px 2) (em 0) (em 0) (em 0)]]
               :margin [[(px 0) (em 0)]]
               :background-color colour/result-header
               :box-shadow [[(px 0) (px 2) (px 4) colour/box-shadow]]}]]
            [:#results
             [:#no-results
              {:margin (em 1)}]
             [:table
              {:width (percent 100)}]
             [:.result-group-name
              {:cursor :pointer}
              [:span
               {:font-size (em 1.2)
                :vertical-align :super}]]
             [:td.col-results-actions :td.result-group-actions
              {:text-align :right}
              [:button
               {:margin-right (px 10)}]]]])
