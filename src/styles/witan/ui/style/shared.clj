(ns witan.ui.style.shared
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.fonts  :as fonts]
            [witan.ui.style.values :as values]
            [witan.ui.style.util :refer [transition]]))

(def style [[:.shared-search-input
             {:position :relative}
             [:i
              {:position       :absolute
               :vertical-align :middle
               :margin         (em 0.24)}]
             [:input
              {:padding-left (px 30)
               :width        (percent 100)}]]

            ;;;;;;;;;;;;;;

            [:.shared-table
             ^:prefix {:user-select :none}
             [:.pure-table.pure-table-horizontal
              {:width (percent 100)}]
             [:#loading
              {:margin-top (em 2)}]
             [:th
              {:color colour/table-header-text
               :font-weight :normal
               :cursor :default}]
             [:tbody
              [:tr
               {:transition (transition :background-color "0.15s"
                                        :color "0.15s")
                :height (em 3)}
               [:&:hover
                {:background-color colour/table-row-hover-bg
                 :color colour/table-row-hover-text
                 :cursor :pointer}]
               [:&.selected
                {:background-color colour/table-row-selected-bg
                 :color colour/table-row-selected-text
                 :cursor :pointer}]]]]

            ;;;;;;;;;;;;;;

            [:.shared-heading
             {:background-color colour/dash-heading-bg
              :box-shadow "0px 2px 4px #888"
              :position :relative
              :height values/app-peripheral-height
              :z-index 50
              :display :flex
              :align-items :center
              :justify-content :flex-start
              :min-width (px 325)}
             [:h1 :h2
              {:position :relative
               :float :left
               :padding (px 0)
               :margin-left (px 10)}]
             [:h1
              {:line-height (em 1)
               :font-weight 700}]
             [:h2
              {:font-family fonts/base-fonts

               :font-weight 500
               :font-size (em 1.2)}]
             [:.shared-search-input
              {:display :inline-flex
               :font-size (px 14)
               :vertical-align :super
               :margin-left (em 1)}
              [:form
               {:width (em 32)}]]]

            ;;;;;;;;;;;;;;

            [:.shared-inline-group :.shared-inline-schema
             {:display :inline}
             [:.group-icon :.schema-icon
              {:display :inline
               :vertical-align :sub
               :margin-right (em 0.2)}]]

            ;;;;;;;;;;;;;;

            [:.button-container
             {:align-self :center}
             [:.material-icons
              {:vertical-align :middle}]
             [:button
              {:margin-left (em 0.5)
               :box-shadow [[(px 2) (px 2) (px 4) colour/box-shadow]]}
              [:i
               {:margin [[(px -3) (px 5) (px 0) (px 0)]]}]]]

            ;;;;;;;;;;;;;;

            [:.error
             {:color colour/error}]
            [:.success
             {:color colour/success}]

            ;;;;;;;;;;;;;;

            [:.rotates
             {:transition (transition :transform "0.3s")}]
            [:.rotate0
             {:transform "rotate(0deg)"}]
            [:.rotate270
             {:transform "rotate(-90deg)"}]

            ;;;;;;;;;;;;;;

            [:.shared-index
             [:.alpha-header
              [:.alpha-header-clickable
               {:color 'blue
                :cursor :pointer}]
              [:a :span
               {:margin-right (px 4)
                :font-size (px 18)}]]
             [:.alpha-index
              [:h1
               {:font-size (px 24)
                :font-weight :bold
                :font-family fonts/base-fonts}]]]

            ;;;;;;;;;;;;;;

            [:div.shared-info-panel
             {:display :flex
              :background-color colour/info-bg
              :border [[(px 1) colour/info-border 'solid]]
              :padding (em 0.5)
              :margin [[(em 0.4) (em 0)]]}
             [:div
              {:font-size (px 11)
               :font-style :italic
               :color colour/info-text
               :display :flex
               :justify-content :center
               :align-content :center
               :flex-direction :column
               :vertical-align :middle}]
             [:i
              {:padding-right (em 0.5)}]]


            ;;;;;;;;;;;;;;

            [:.number-circle
             {:border-radius (percent 50)
              :width (px 20)
              :height (px 18)
              :line-height (px 18)
              :padding (px 3)
              :background colour/button-create
              :color colour/body-bg
              :text-align :center
              :font-size (px 12)
              :font-weight :bold
              }]

            ;;;;;;;;;;;;;;;

            [:.shared-schema-search-area :.shared-group-search-area
             [:div.breakout-area
              {:display :flex
               :overflow :hidden
               :transition (transition :height "0.3s")
               :width (percent 100)
               :margin (px 10)}
              [:.shared-table
               {:width (percent 100)
                :border [[(px 1) colour/gutter 'solid]]
                :overflow-y :scroll
                :overflow-x :hidden}
               [:.pure-table.pure-table-horizontal
                {:border 0}]
               [:.shared-table-rows
                [:tbody>tr:last-child>td
                 {:border-bottom [[(px 1) "#cbcbcb" 'solid]]}]]
               [:.pure-button
                ]]
              [:.close
               {:color 'silver
                :cursor :pointer}
               [:&:hover
                {:color colour/side-bg}]]]]])
