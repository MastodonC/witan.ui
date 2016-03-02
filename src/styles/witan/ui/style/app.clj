(ns witan.ui.style.app
  (:require [garden.units :refer [px percent]]
            [witan.ui.style.colour :as colour]))

(def pc100 (percent 100))
(def switcher-padding 3)
(def switcher-icon-dx 32)

(def style [[:#app
             {:background-color colour/body-bg}

             [:#split
              {:height pc100
               :width  pc100
               :top (px 0)
               :position :absolute}
              [:#loading
               {:width pc100
                :height pc100
                :background-color colour/body-bg
                :position :absolute
                :top (px 0)
                :display :table
                :text-align :center}
               [:div
                {:display :table-cell
                 :vertical-align :middle}]]]

             [:#primary
              [:#overlay
               {:margin (px 8)}]]]

            [:.primary-switcher
             {:height (px (+ (* switcher-padding 2) switcher-icon-dx))
              :width (px (+ (* switcher-padding 4) (* switcher-icon-dx 2)))
              :background-color colour/switcher-bg
              :box-shadow [[(px 2) (px 2) (px 4) colour/box-shadow]]
              :border-radius (px 3)}
             [:.icons
              {:position :absolute}
              ^:prefix {:user-select :none}
              [:.icon
               {:padding (px switcher-padding)
                :cursor :pointer
                :display :table-cell
                :opacity 0.5}]
              [:.selected
               {:opacity 1
                :cursor :default}]]
             [:#indicator-container
              {:margin-left (px switcher-padding)
               :position :relative}
              [:#indicator
               {:height (px switcher-icon-dx)
                :width (px switcher-icon-dx)
                :margin-top (px switcher-padding)
                :position :absolute
                :background-color colour/body-bg
                :border-radius (px 3)
                :top (px 0)
                :transition "margin-left 0.1s"}]]
             [:.indicator-offset-1
              {:margin-left (px (+ (* switcher-padding 2) switcher-icon-dx))}]]

            [:.secondary-switcher
             [:button
              {:border-radius 0
               :background-color colour/switcher-bg
               :margin 0
               :border-left [[(px 1) 'solid colour/side-bg]]
               :box-shadow [[ (px 0) (px 2) (px 4) colour/box-shadow]]
               :font-weight :bold}]
             [:.selected
              {:background-color colour/switcher-button-selected
               :pointer-events :none}]]])
