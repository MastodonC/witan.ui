(ns witan.ui.style.components.configuration
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:#configuration
             [:#no-data
              {:margin (em 1)}]
             [:table
              {:width (percent 100)}
              [:td
               {:white-space :nowrap}
               [:input
                {:width (percent 80)}]]
              [:th.col-data-key
               {:width (percent 100)}]]]])
