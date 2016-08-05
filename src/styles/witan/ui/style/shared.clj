(ns witan.ui.style.shared
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.fonts  :as fonts]
            [witan.ui.style.values :as values]))

(def style [[:.shared-search-input
             {:position :relative}
             [:i
              {:position       :absolute
               :vertical-align :middle
               :margin         (em 0.24)}]
             [:#filter-input
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
               {:transition "background-color 0.15s, color 0.15s"
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
              :justify-content :flex-start}
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
               :font-size (em 1.2)}]]

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
             {:color colour/error}]])
