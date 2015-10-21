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
      {:color     colour/title
       :font-size (em 2.5)}
      [:em
       {:font-size (em 0.70)
        :color colour/title-light
        :margin-left (em 0.5)}]]
     [:h2
      {:color     colour/subtitle
       :font-size (em 1.75)}]
     [:h3
      {:color colour/para-heading}
      [:em
       {:font-size (em 0.7)
        :color colour/para-heading-light}]]
     [:hr
      {:background :transparent
       :border "0"
       :border-bottom "1px solid #ddd"}
      [:&.small
       {:margin-top (px 0)
        :margin-botom (px 0)}]
      [:&.medium
       {:margin-top (px 30)
        :margin-bottom (px 30)}]
      [:&.large
       {:margin-top (px 60)
        :margin-bottom (px 60)}]]
     [:href
      {:margin  (px 0)
       :padding (px 0)}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     ;; overrides
     [:.pure-menu-heading
      {:color     colour/menu-item
       :font-size (px 20)}]
     [:.pure-menu
      {:position :relative}
      [:.pure-menu-list
       {:position   :absolute
        :right      (em 0.3)
        :margin-top (em 0.3)}]]
     [:.pure-table
      {:border (px 0)}
      [:thead :th :td
       {:background-color :transparent
        :border           (px 0)}]]
     [:.pure-g
      {:font-family f/base-fonts}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     [:#app
      {:padding "0 1em"}]

     [".unselectable::selection"
      {:background-color :transparent}]

     ;; buttons

     [:.button-success
      {:background-color colour/button-success
       :color            colour/white}]

     [:.button-warning
      {:background-color colour/button-warning
       :color            colour/white}]

     [:.button-error
      {:background-color colour/button-error
       :color            colour/white}]

     [:.button-primary
      {:background-color colour/button-primary
       :color            colour/white}]

     [:.button-secondary
      {:background-color colour/button-secondary
       :color            colour/white}]

     ;; text

     [:.text-center
      {:text-align :center}]

     [:.text-right
      {:text-align :right}]

     [:.text-left
      {:text-align :left}]

     [:.text-white
      {:color colour/white}]

     [:.text-gray
      {:color colour/gray}]

     [:.padding-1
      {:padding (em 1)}]

     ;; labels

     [:.label-in-progress
      {:background-color colour/in-progress}]

     [:.label-forecast-changed
      {:background-color colour/forecast-changed}]

     [:.label-new
      {:background-color colour/new-forecast}]

     [:.label
      {:display :inline
       :padding ".2em .6em .3em"
       :font-size (percent 75)
       :font-weight 700
       :line-height 1
       :color colour/white
       :text-align :center
       :white-space :nowrap
       :vertical-align :middle
       :border-radius (em 0.25)
       :margin-right (em 0.20)}]

     [:.label-small
      {:font-size (percent 65)}]

     ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     [:#container
      {:margin (em 1)}]

     ;; witan

     [:#witan-menu
      {:background-color colour/header
       :box-shadow "0px 3px 4px #888888"}]

     [:.witan-menu-item
      [:a
       {:text-decoration :none
        :color           colour/menu-item
        :cursor          :pointer}]
      [:a:hover
       {:color colour/menu-item-hover}]]

     [:.witan-search-input
      {:position :relative}
      [:i
       {:position       :absolute
        :vertical-align :middle
        :margin         (em 0.5)}]
      [:#filter-input
       {:padding-left (px 30)}]]

     [:.witan-page-heading
      [:.pure-menu-list
       {:bottom (em 0.55)}]
      [:button
       {:margin-left (em 0.5)}]]

     [:.witan-dash-heading
      {:color         colour/primary
       :border-bottom "#ccc 2px solid"}
      [:h1
       {:margin-bottom (em 0.2)
        :display       :inline-block}]

      [:.pure-form
       {:display        :inline-flex
        :font-size      (px 14)
        :vertical-align :text-bottom
        :margin-left    (em 1)}]]

     [:#witan-main-content
      {:padding-left  (px 30)
       :padding-right (px 30)}]

     [:#witan-dash-forecast-list
      {:width  (percent 100)
       :border (px 0)}
      [:th :td
       {:border (px 0)}]
      [:th:first-child
       {:width (px 1)}]]

     [:.witan-forecast-table-row
      [:.modifier
       {:color       colour/gray
        :margin-left (em 0.5)}]
      [:.tree-control
       {:background-color colour/white}]
      [:&:hover
       {:background-color colour/row-highlight
        :cursor           :pointer}]
      [:.version-labels
       {:display :inline
        :margin-left (em 0.5)}]]

     [:.witan-forecast-table-row-selected
      :.witan-forecast-table-row-selected:hover
      {:background-color colour/row-selected}]

     [:.witan-forecast-table-row-descendant
      {:color     colour/darker-gray
       :font-size (em 0.9)}
      [:.name
       {:margin-left (em 1)}]]

     [:.witan-forecast-table-version-descendant
      {:margin-left (em 0.3)}]

     [:#witan-pw-top-spacer
      {:height (em 2)}]

     [:#witan-pw-edits
      {:background-color colour/forecast-changed-light
       :border "solid 2px"
       :border-color colour/forecast-changed
       :border-radius (em 0.3)
       :padding (em 1)
       :line-height (em 1.6)
       :font-size (em 1.1)}
      [:#witan-pw-edits-buttons
       {:text-align :right}]
      [:button
       {:margin-left (em 1)}
       [:&#create
        {:background-color colour/forecast-changed}]
       [:&#revert
        {:background-color colour/button-error}]]]

     [:#witan-pw-in-prog
      {:background-color colour/in-progress-light
       :border "solid 2px"
       :border-color colour/in-progress
       :border-radius (em 0.3)
       :padding (em 1)
       :line-height (em 1.6)
       :font-size (em 1.1)}
      [:button
       {:margin-left (em 1)
        :display :inline
        :background-color colour/in-progress}]]
     [:#witan-pw-in-prog-text
      {:display :inline}]

     [:#witan-pw-area
      {:line-height (em 1.6)}]

     [:#witan-pw-action-body
      {:text-align :left}
      [:.model-value
       {:margin-left (em 1)
        :margin-right (em 1)}]]

     [:.witan-model-diagram
      {:stroke       colour/black
       :stroke-width 3
       :text-align   "center"}
      [:.input  {:fill colour/forecast-input}]
      [:.output {:fill colour/forecast-output}]
      [:.model  {:fill colour/forecast-model}]
      [:.group  {:fill         colour/forecast-group
                 :stroke       "grey"
                 :stroke-width (px 2)}]

      [:.highlight-input  {:fill colour/forecast-input-light
                           :transition "stroke 0.5s"
                           :stroke     "white"}]
      [:.highlight-output {:fill colour/forecast-output-light
                           :transition "stroke 0.5s"
                           :stroke     "white"}]
      [:.highlight-model  {:fill colour/forecast-model-light
                           :transition "stroke 0.5s"
                           :stroke     "white"}]

      [:.highlighted
       {:stroke "none"}]

      [:.forecast-label-circle {:stroke-weight (px 2)
                                :fill          :none
                                :transition    "stroke 0.5s"}]
      [:.forecast-label-text {:font-family f/base-fonts
                              :font-weight :bold
                              :stroke      :none
                              :transition  "fill 0.5s"}]]

     [:.witan-pw-header
      {:border-bottom "#ccc 2px solid"}
      [:h1
       {:margin-bottom (em 0.2)
        :display       :inline-block}]
      [:.version-zero
       {:color colour/new-forecast}]
      [:.labels
       {:display     :inline
        :margin-left (em 0.5)
        :font-size (percent 60)}]]

     [:.witan-pw-nav-button
      {:text-align :center
       :color colour/primary
       :display :block
       :height :inherit
       :position :relative}
      [:i
       {:top (percent 40)
        :position :absolute
        :left (percent 10)
        :right (percent 10)}]]

     [:.witan-pw-area-header
      {:text-align    :center
       :width         (percent 100)
       ;;:margin-top    (px 20)
       :margin-bottom (px 15)}
      [:h2
       {:color       colour/primary
        :margin      "10px"
        :font-weight 400}]

      [:.witan-pw-input-data-row
       [:small
        {:display :block
         :line-height (px 6)}]]

      [:.input
       {:width            (percent 100)
        :background-color colour/forecast-input
        :transition       "background-color 0.5s"}]
      [:.model
       {:width            (percent 100)
        :background-color colour/forecast-model
        :transition       "background-color 0.5s"}]
      [:.output
       {:width            (percent 100)
        :background-color colour/forecast-output
        :transition       "background-color 0.5s"}]]

     [:.view-overlay
      {:width      (percent 100)
       :height     (percent 100)
       :position   :absolute
       :top        (px 0)
       :left       (px 0)
       :text-align :center}
      [:#loading
       {:color      colour/white
        :margin-top (percent 20)}]]])))
