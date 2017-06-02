(ns witan.ui.style.app
  (:require [garden.units :refer [px percent em]]
            [witan.ui.style.colour :as colour]))

(def pc100 (percent 100))
(def switcher-padding 3)
(def switcher-icon-dx 32)
(def switcher-height 28)

(def style [[:#app
             {:background-color colour/body-bg}

             ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
             ;; General

             [:button.pure-button
              {:align-self :center
               :box-shadow [[(px 2) (px 2) (px 4) colour/box-shadow]]}
              [:.material-icons
               {:vertical-align :middle}]
              [:i
               {:margin [[(px -3) (px 0) (px 0) (px 0)]]}]
              [:span
               {:margin-left (px 5)}]]

             [:hr
              {:height 0
               :margin [[(px 15) (px 0)]]
               :overflow :hidden
               :background :transparent
               :border 0
               :border-bottom [[(px 1) 'solid "#ddd"]]}]

             [:.padded-content
              {:padding [[(em 1) (em 1)]]
               :margin (em 1)}]

             [:.flex
              {:display :flex
               :align-items :flex-start
               :justify-content :space-between}]

             [:.flex-center
              {:display :flex
               :align-items :flex-start
               :justify-content :center}]

             ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
             ;; Create Workspace

             [:#create-workspace
              [:#content
               {:padding [[(em 0) (em 1)]]
                :max-width (px 700)}
               [:h2
                [:em
                 {:font-size (px 12)
                  :margin-left (px 5)
                  :font-style :normal
                  :color 'gray}]]
               [:button
                {:background-color colour/button-create
                 :color colour/body-bg}]]]

             ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
             ;; Create RTS

             [:.user-list :.schema-list
              {:display :block}
              [:& :table
               {:border "none"}
               [:thead :th
                {:background-color colour/switcher-bg}]
               [:td :th
                {:border "none"}]]
              [:.user-list-item :.schema-list-item
               {:margin-bottom (em 0.4)}
               [:.button-container
                {:display :inline
                 :margin-right (em 0.6)}
                [:button
                 {:background-color colour/button-view
                  :color colour/button-create}]]]]

             [:.loading
              {:width pc100
               :height pc100
               :background-color colour/body-bg
               :position :absolute
               :top (em 2)
               :display :table
               :text-align :center}
              [:div
               {:display :table-cell
                :vertical-align :middle}]]

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
                :top (em 2)
                :display :table
                :text-align :center}
               [:div
                {:display :table-cell
                 :vertical-align :middle}]]
              [:#loading-modal
               {:width pc100
                :height pc100
                :position :absolute
                :top (em 4)
                :display :table
                :text-align :center}]]

             [:#primary
              [:#heading
               {:margin-left (px 96)
                :position :fixed
                :top (px 0)}
               [:h1
                {:margin-top (em 0.5)
                 :background-color colour/body-bg}]]
              [:#overlay
               {:margin (px 8)
                :position :absolute
                :top (px 0)}]
              [:div#container
               {:width pc100
                :height pc100
                :overflow :hidden}]
              [:#primary-content
               {:width pc100
                :height pc100
                :position :relative
                :display :flex}]]

             [:#secondary
              [:div.secondary-outer-container
               {:height (percent 100)
                :position :relative}]
              [:div.secondary-container
               {:top (px (+ 2 switcher-height))
                :overflow :auto
                :position :absolute
                :bottom (px 0)
                :right (px 0)
                :left (px 0)}]]
             [:#secondary-left :#secondary-right
              {:height (percent 100)
               :float :left}]]

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
               :height (px switcher-height)
               :background-color colour/switcher-bg
               :margin 0
               :border-left [[(px 1) 'solid colour/side-bg]]
               :box-shadow [[(px 0) (px 2) (px 4) colour/box-shadow]]
               :font-weight :bold}]
             [:.selected
              {:background-color colour/switcher-button-selected
               :pointer-events :none}]]

            [:div.hero-notification
             {:position :relative
              :margin [[(px 0) (px 0)]]
              :border [[(px 1) 'silver 'solid]]
              :border-right 0
              :background-color colour/hero-bg
              :padding [[(px 0) (px 10) (px 10) (px 10)]]}
             [:div.hero-close
              {:position :absolute
               :right (px 1)
               :cursor :pointer
               :color 'silver}
              [:&:hover
               {:color 'gray}]]]])
