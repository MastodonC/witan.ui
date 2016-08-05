(ns witan.ui.style.panic
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:.panic-screen
             {:width (percent 100)
              :height (percent 100)
              :background-color colour/panic-bg
              :position :fixed
              :text-align :center}
             [:h1 {:color colour/panic-header-text
                   :font-size (em 4)}]
             [:p {:position :fixed
                  :bottom (px 0)}]]])
