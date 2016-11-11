(ns witan.ui.style.components.rts
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]
            [witan.ui.style.fonts :as fonts]))

(def style [[:#rts-functions
             [:.buttons
              {:display :block
               :float :none
               :padding-bottom (em 1)
               :margin (em 1)
               :border-bottom [[(px 1) colour/switcher-bg 'solid]]}
              [:button
               {:font-size (px 16)}
               [:i {:font-size (px 32)
                    :display :block
                    :padding (px 4)}]]
              [:.create-rts-button
               {:background-color colour/rts-button-create
                :color colour/title-fonts-colour}]]]

            [:#create-rts
             {:height (percent 100)}
             [:.container
              {:position :relative
               :height (percent 100)}
              [:.content
               {:padding [[(em 0) (em 1)]]
                :display :flex
                :position :absolute
                :top values/app-peripheral-height
                :right 0
                :left 0
                :bottom 0
                :overflow-y 'auto}
               [:h2
                [:em
                 {:font-size (px 12)
                  :margin-left (px 5)
                  :font-style :normal}]]
               [:.user-list :.schema-list
                {:padding (px 10)}]
               [:.breakout-area
                {:width (percent 100)
                 :transition "height 0.3s"
                 :margin (px 10)
                 :border-radius (px 8)
                 :overflow "hidden"}
                [:.close
                 {:float :right
                  :color 'silver
                  :cursor :pointer}
                 [:&:hover
                  {:color 'gray}]]
                [:.container
                 {:margin-bottom (em 1)
                  :border [[(px 2) "silver" 'dashed]]
                  :overflow-y "auto"
                  :height (percent 89)
                  :width (percent 99)}
                 [:h2
                  {:color 'gray}]]]
               [:h3
                {:font-size (px 14)
                 :margin [[(em 0.4) (em 0)]]
                 :color 'gray}]
               [:button.button-success
                {:background-color colour/button-create
                 :color colour/body-bg}]
               [:#submit-button
                {:margin-bottom (px 20)}]
               [:.pure-form
                [:i
                 {:font-size (px 20)}]
                [:.button-container
                 {:margin-top (px 6)}
                 [:button {:margin (px 0)}]]]]]]

            [:#rts-view
             [:.padded-content
              {:max-width (px 1024)}
              [:#heading
               {:margin [[(em 1) (em 0)]]
                :height (px 24)}]
              [:#info
               {:padding (em 1)
                :border-right [[(px 1) 'silver 'solid]]
                :background-color colour/rts-info-bg}
               [:.info-paragraph
                [:strong
                 {:font-size (em 1.12)}]
                [:span :strong
                 {:font-family fonts/base-fonts}]
                ["span::after" "strong::after"
                 {:content "\" \""}]]]
              [:h1
               {:margin 0}]
              [:div.group-mail-row
               {:margin [[(px 1) (px 0)]]
                :display :inline-block
                :width (percent 100)}
               [:.group-mail-row-button
                {:width (px 120)}]
               [:.shared-inline-group
                {:font-size (em 1.2)}]]]]])
