(ns witan.ui.style.shared
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
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

            [:.shared-table
             [:.pure-table.pure-table-horizontal
              {:width (percent 100)}]
             [:th
              {:color colour/table-header-text
               :font-weight :normal}]
             [:.box-shadow
              {:box-shadow "0px 2px 4px #888"}]
             [:tbody
              [:tr
               [:&:hover
                {:background-color colour/table-row-hover-bg
                 :color colour/table-row-hover-text
                 :cursor :pointer}]
               [:&.selected
                {:background-color colour/table-row-selected-bg
                 :color colour/table-row-selected-text
                 :cursor :pointer}]]]]])
