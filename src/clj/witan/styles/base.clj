(ns witan.styles.base
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.styles.fonts :as f]
            [witan.styles.colours :as colour]
            [witan.styles.util :refer [url]]
            [witan.styles.login :as l]))

;; Change defstylesheet to defstyles.
(defstyles base
  (vec
   (concat
    ;; fonts
    f/font-face-definitions

    ;; login
    l/login

    ;; style
    [;; tags
     [:html
      {:background-color colour/bg}]
     [:body
      {}]
     [:body :h1 :h2 :h3 :h4 :h5
      {:font-family f/base-fonts}]
     [:h1
      {:color colour/title
       :font-size (em 2.5)}]
     [:h2
      {:color colour/subtitle
       :font-size (em 1.75)}]
     [:h3
      {:color colour/para-heading}]
     [:hr
      {:color colour/hr
       :background-color colour/hr}]
     [:href
      {:margin (px 0)
       :padding (px 0)}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     ;; overrides
     [:.pure-menu-heading
      {:color colour/menu-item
       :font-size (px 20)}]
     [:.pure-menu
      {:position :relative}
      [:.pure-menu-list
       {:position :absolute
        :right (em 0.3)
        :margin-top (em 0.3)}]]
     [:.pure-table
      [:thead
       {:background-color :transparent
        :border (px 0)}]]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     [:.button-success
      {:background-color colour/button-success
       :color colour/white}]

     [:.button-warning
      {:background-color colour/button-warning
       :color colour/white}]

     [:.button-error
      {:background-color colour/button-error
       :color colour/white}]

     [:.button-primary
      {:background-color colour/button-primary
       :color colour/white}]

     [:.button-secondary
      {:background-color colour/button-secondary
       :color colour/white}]

     [:.text-center
      {:text-align :center}]

     [:.text-white
      {:color colour/white}]

     [".unselectable::selection"
      {:background-color :transparent}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     [:#container
      {:margin (em 1)}]

     ;; witan

     [:#witan-menu
      {:background-color colour/header}]

     [:.witan-menu-item
      [:a
       {:text-decoration :none
        :color colour/menu-item}]
      [:a:hover
       {:color colour/menu-item-hover}]]

     [:.witan-search-input
      {:position :relative}
      [:i
       {:position :absolute
        :vertical-align :middle
        :margin (em 0.5)}]
      [:#filter-input
       {:padding-left (px 30)}]]

     [:.witan-dash-heading
      {:color colour/primary
       ;;:font-size (px 20)
       :border-bottom "#ccc 2px solid"}
      [:h1
       {:margin-bottom (em 0.2)
        :display :inline-block}]
      [:button
       {:margin-left (em 0.5)}]
      [:.pure-menu-list
       {:bottom (em 0.55)}]
      [:.pure-form
       {:display :inline-flex
        :font-size (px 14)
        :vertical-align :text-bottom
        :margin-left (em 1)}]]

     [:#witan-main-content
      {:padding-left (px 30)
       :padding-right (px 30)}]

     [:#witan-dash-forecast-list
      {:width (percent 100)
       :border (px 0)}
      [:th :td
       {:border (px 0)}]
      [:th:first-child
       {:width (px 1)}]]

     [:.witan-forecast-table-row
      [:.modifier
       {:color colour/gray
        :margin-left (em 0.5)}]
      [:.tree-control
       {:background-color colour/white}]
      [:&:hover
       {:background-color colour/row-highlight
        :cursor :pointer}]]

     [:.witan-forecast-table-row-selected
      :.witan-forecast-table-row-selected:hover
      {:background-color colour/row-selected}]

     [:.witan-forecast-table-row-descendant
      {:color colour/darker-gray
       :font-size (em 0.9)}
      [:.name
       {:margin-left (em 1)}]]

     [:.witan-model-diagram
      {:stroke colour/black
       :stroke-width 3
       :text-align "center"}
      [:.input {:fill colour/forecast-input}]
      [:.output {:fill colour/forecast-output}]
      [:.model {:fill colour/forecast-model}]
      [:.group {:fill colour/forecast-group
                :stroke "none"}]
      [:.highlight {:fill "none"
                    :stroke-width 2
                    :stroke-dasharray "3,3"}]]
     [:.witan-pw-header
      {:border-bottom "#ccc 2px solid"}
      [:h1
       {:margin-bottom (em 0.2)
        :display :inline-block}]]

     [:.witan-pw-nav-button
      ;; I really want this to vertically centre, but can't seem to figure it out
      {:padding-top (em 8)}
      [:a {:color colour/primary}]]

     [:.witan-pw-area-header
      {:text-align :center
       :width (percent 100)
       :margin-top (px 20)
       :margin-bottom (px 15)}
      [:h2
       {:color colour/primary
        :margin "10px"
        :font-weight 400}]
      [:.input
       {:width (percent 100)
        :background-color colour/forecast-input}]
      [:.model
       {:width (percent 100)
        :background-color colour/forecast-model}]
      [:.output
       {:width (percent 100)
        :background-color colour/forecast-output}]]])))
