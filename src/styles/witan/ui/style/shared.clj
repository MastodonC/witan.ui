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
               :height       (px 34)
               :width        (percent 100)}]]

;;;;;;;;;;;;;;
            [:.shared-checkbox
             [:label
              {:margin-left (px 10)
               :vertical-align "middle"}
              [:input
               {:vertical-align "middle"
                :position "relative"
                :bottom (px 1)}]]]

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
                :height (px 40)}
               [:&:hover
                {:background-color colour/table-row-hover-bg
                 :color colour/table-row-hover-text
                 :cursor :pointer}]
               [:&.selected
                {:background-color colour/table-row-selected-bg
                 :color colour/table-row-selected-text
                 :cursor :pointer}]]]]

;;;;;;;;;;;;;;

            [:.sharing-matrix
             [:.pure-table.pure-table-horizontal
              {:width (percent 100)}]
             [:#loading
              {:margin-top (em 2)}]
             [:th
              {:color colour/table-header-text
               :font-weight :normal
               :cursor :default}]
             [:thead
              [:th
               [:&:first-child {:width "50%"}]]]
             [:tbody
              [:tr
               {:height (em 3)}
               [:td
                [:&:first-child {:width "50%"}]]]]]

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
             [:span
              {:margin [[(px 15) (px 10) (px 10) (px 10)]]}]
             [:.shared-search-input
              {:display :inline-flex
               :font-size (px 14)
               :vertical-align :super
               :margin-left (em 1)}
              [:form
               {:width (em 32)}]]
             [:&.center-string
              [:h1
               {:width (percent 100)
                :text-align :center}]]]

;;;;;;;;;;;;;;

            [:.shared-inline-group :.shared-inline-schema
             {:display :inline}
             [:.group-icon :.schema-icon
              {:display :inline
               :vertical-align :sub
               :margin-right (em 0.2)}]
             [:.you
              {:margin-left (px 4)
               :cursor :default}]]

            [:.shared-inline-file-title
             {:display :flex
              :align-items :center
              :text-overflow :ellipsis
              :width (percent 100)}
             [:h1 :h2 :h3 :h4 :h5
              {:margin [[(em 0.0) (em 0.3)]]
               :line-height (em 1.6)
               :white-space :nowrap
               :overflow :hidden
               :text-overflow :ellipsis}]]

;;;;;;;;;;;;;;

            [:.button-container
             {:align-self :center}
             [:.material-icons
              {:vertical-align :middle}]
             [:button
              {:margin-left (em 0.5)
               :box-shadow [[(px 2) (px 2) (px 4) colour/box-shadow]]}]]

;;;;;;;;;;;;;;

            [:.error
             {:color colour/error}]
            [:.success
             {:color colour/success}]
            [:.btn-success
             {:background-color colour/success
              :color 'white}]
            [:.btn-danger
             {:background-color colour/danger
              :color 'white}]
            [:.btn-error
             {:background-color colour/error
              :color 'white}]
;;;;;;;;;;;;;;

            [:.space-after
             {:margin-bottom (em 1)}]

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
              {:padding-right (em 0.5)}]
             [:.message
              {:overflow :hidden
               :line-height (em 1.4)}]]


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

            [:.shared-schema-search-area :.shared-group-search-area :.shared-search-area
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
                {:color colour/side-bg}]]]]

;;;;;;;;;;;;;;;

            [:.shared-progress-bar
             {:border [[(px 1) 'solid colour/progress-bar-border]]
              :border-radius (em 0.3)
              :margin [[(em 1) 0]]
              :height (px 14)
              :overflow :hidden
              }]
            [:.shared-progress-bar-inner
             {:background-color colour/progress-bar-fill
              :border [[(px 10) 'solid colour/progress-bar-fill]]
              :margin-left (px -5)
              :margin-top (px -5)
              :height (percent 100)}]

            [:.shared-tabs
             {:display :flex
              :justify-content :center
              :background-color "#eee"
              :box-shadow [[(px 0) (px 1) (px 4) "rgba(0,0,0,.14)"]]}
             [:.shared-tab
              {:margin [[(em 0.0) (em 0.75)]]
               :margin-top (em 0.8)
               :color colour/subtle-grey3
               :cursor :pointer}
              [:&:hover
               {:color colour/clickable}]]
             [:.shared-tab-selected
              {:color colour/title-fonts-colour
               :border-bottom [[(px 2) 'solid colour/switcher-button-selected]]}]]

            [:.shared-tag
             {:display :inline
              :margin (em 0.3)
              :padding [[(em 0.4) (em 0.3)]]
              :font-size (em 0.9)
              :background-color colour/tag-bg
              :border [[(px 1) 'solid colour/tag-border]]}
             [:.tag-close
              {:display :inline}]
             [:i
              {:font-size (px 10)
               :font-weight 700
               :padding (px 1)
               :margin-right (px 3)}
              [:&:hover
               {:color 'white}]]]
            [:.shared-tag-clickable
             {:cursor :pointer}
             [:span
              [:&:hover
               {:color colour/body-bg}]]]

            [:.clickable-text
             {:color colour/clickable
              :cursor :pointer}
             [:&:hover
              {:color colour/clickable-hovered}]]

            [:.shared-collapsible-text
             [:div
              {:margin-top (px 3)
               :margin-left (px 2)}
              [:&.rotate270
               {:margin-top (px -3)
                :margin-left (px 3)}]]
             [:span
              {:margin-top (px 4)
               :margin-left (px 1)}
              [:&.ellipsis
               {:margin-top (px 2)
                :margin-left (px -2)}]]
             [:i
              {:cursor :pointer}]]

            [:.editable-field
             {:padding (em 1)
              :margin-bottom (em 1)
              :line-height (em 1.7)
              :border-color colour/subtle-grey
              :border-radius (px 2)
              :box-shadow [[(px 0) (px 1) (px 4) "rgba(0,0,0,.14)"]]
              :position :relative}
             [:&:hover
              {}]
             [:span.clickable-text.edit-label
              {:font-size (px 12)
               :height (em 0.75)
               :line-height (em 0.75)
               :position :absolute
               :right (px 8)
               :bottom (px 8)}]
             [:.heading
              {:margin-top (em 0)}]
             [:.intro
              {:line-height (em 1.5)
               :display :block
               :font-size (px 11)
               :color 'dimgrey
               :margin-bottom (em 1)}]

             [:.editable-field-content
              {:display :flex
               :justify-content :space-between
               :vertical-align :bottom
               :align-items :flex-end}]]

            [:.editable-field-editing
             {}]

            [:.btn-pagination
             {:padding (px 2)}
             [:span {:margin-right (px 5)}]]])
