(ns witan.ui.style.dashboard
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:.dashboard
             {:height (percent 100)
              :min-width (px 800)}
             [:.heading
              {:background-color colour/dash-heading-bg
               :padding-left (px 15)
               :box-shadow "0px 2px 4px #888"
               :position :relative
               :height values/app-peripheral-height
               :z-index 50}
              [:h1
               {:padding (px 0)
                :margin-top (px (/ values/app-peripheral-height-value 6))
                :display :inline-block
                :width (em 6)}]
              [:.shared-search-input
               {:display :inline-flex
                :font-size (px 14)
                :vertical-align :super
                :margin-left (em 1)}
               [:form
                {:width (em 36)}]]]
             [:.content
              {:position :absolute
               :top values/app-peripheral-height
               :bottom 0
               :left 0
               :right 0
               :overflow-y :auto
               :overflow-x :hidden}
              [:#container
               {:width (percent 99)}]]]])
