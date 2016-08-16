(ns witan.ui.style.panic
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:.panic-screen
             {:left (px 0)
              :right (px 0)
              :height (percent 100)
              :background-color colour/panic-bg
              :position :absolute
              :text-align :center}
             [:h1 {:color colour/panic-header-text
                   :font-size (em 4)}]
             [:strong
              {:font-size (em 1.5)
               :line-height (em 1.4)}]
             [:p {:position :fixed
                  :bottom (px 0)}]]])
