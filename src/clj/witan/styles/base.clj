(ns witan.styles.base
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.styles.fonts :as f]
            [witan.styles.colours :as colour]
            [witan.styles.util :as u]
            [witan.styles.login :as l]))

(def app-peripheral-width  (px 80))
(def app-peripheral-height (px 60))

;; Change defstylesheet to defstyles.
(defstyles base
  ;; stuff wot 'as to be first


  (vec
   (concat
    ;; fonts
    f/font-face-definitions

    ;; animations
    u/keyframe-animations

    ;; login
    l/login

    ;; material-icons
    u/material-icons

    ;; style
    [;; tags

     ;; scaffolding
     [:#login {:position :absolute
               :top 0
               :left 0
               :bottom 0
               :right 0
               :background-color colour/white
               :z-index 50}]
     [:#side {:background-color colour/sidebar-background
              :position :fixed
              :width app-peripheral-width
              :bottom 0
              :top 0
              :left 0
              :margin-top app-peripheral-height
              :overflow :hidden
              :z-index 20
              :box-shadow "2px 0px 4px #888"}]
     [:#corner {:background-color colour/corner-background
                :background-image (u/url "../img/corner3.svg")
                :background-size [[(px 80) (px 45)]]
                :background-repeat :no-repeat
                :background-position [[(px 0) (px 6)]]
                :position :fixed
                :width app-peripheral-width
                :height app-peripheral-height
                :top 0
                :left 0
                :z-index 10
                :box-shadow "2px 0px 4px #888"}]
     [:#app {:background (u/url "../img/bg.png")
             :position :absolute;
             :top 0
             :left app-peripheral-width
             :bottom 0
             :right 0
             :overflow :hidden}]

     [:body
      {:font-family f/base-fonts
       :font-size (px 14)
       :overflow :hidden}]
     [:h1 :h2 :h3 :h4 :h5
      {:font-family f/title-fonts}]
     [:h1
      {:color     colour/title}
      [:em
       {:font-size (em 0.70)
        :color colour/title-light
        :margin-left (em 0.5)}]]
     [:h2
      {:color     colour/subtitle
       :font-size (em 1.75)}]
     [:h3
      {:color colour/para-heading
       :line-height 1}
      [:em
       {:font-size (em 0.7)
        :color colour/para-heading-light}]]
     [:hr
      {:background :transparent
       :border "0"
       :border-bottom "1px solid #ddd"}
      [:&.green
       {:border-bottom [[(px 1) :solid colour/menu-background]]}]
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
     [:.pure-button
      {:box-shadow [[(px 2) (px 2) (px 4) colour/gray]]}
      [:.material-icons
       {:vertical-align :middle}]]
     [:.pure-form
      [:select {:height (em 2.4)}]]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     ;; buttons

     [:.button-success
      {:background-color colour/button-success
       :color            colour/white}]

     [:.button-success-light
      {:background-color colour/button-success-light
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

     [:.text-primary
      {:color colour/button-primary}]

     [:.text-success
      {:color colour/success}]

     [:.text-error
      {:color colour/error}]

     [:.padding-1
      {:padding (em 1)}]

     ;; labels

     [:.label-in-progress
      {:background-color colour/in-progress}]

     [:.label-error
      {:background-color colour/model-error}]

     [:.label-forecast-changed
      {:background-color colour/forecast-changed}]

     [:.label-forecast-superseded
      {:background-color colour/forecast-superseded}]

     [:.label-forecast-public
      {:background-color colour/forecast-public}]

     [:.label-new
      {:background-color colour/new-forecast}]

     [:.label-tag
      {:background-color colour/deep}]

     [:.labels
      [:a
       {:text-decoration :none}]]

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
       :margin-right (em 0.20)}
      [:.material-icons
       {:vertical-align :middle}]]

     [:.label-small
      {:font-size (percent 65)}]

     ;; util
     [:.full-width
      {:width (percent 100)}]

     [:.full-height
      {:height (percent 100)}]

     [:.hidden-file-input
      {:position :fixed
       :top (em -100)}]

     ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

     [:#container
      {:margin (em 1)}]

     ;; witan

     [:#witan-menu
      {:height app-peripheral-height
       :position :relative
       :text-align :center}
      [:.pure-menu
       {:height (percent 90)
        :position :absolute
        :bottom (px 0)}
       [:h3
        {:margin [[(em 0.3) (em 0) (em 0) (em 0)]]}]]]

     [:.witan-menu-item
      [:a
       {:text-decoration :none
        :color           colour/menu-item
        :cursor          :pointer}]
      [:a:hover
       {:color colour/menu-item-hover}]
      [:button
       [:.material-icons
        {:vertical-align :top}]]]

     [:.witan-search-input
      {:position :relative}
      [:i
       {:position       :absolute
        :vertical-align :middle
        :margin         (em 0.2)}]
      [:#filter-input
       {:padding-left (px 30)
        :width          (percent 100)}]]

     [:.witan-page-heading
      {:background-color colour/lighter-gray
       :box-shadow "0px 2px 4px #888888"
       :height (px 58)
       :position :relative
       :z-index 5}
      [:.pure-menu-list
       {:bottom (em 0.9)
        :right  (em 0.7)}]
      [:button
       {:margin-left (em 0.5)}
       [:i
        {:margin [[(px 0) (px 5) (px 0) (px 0)]]}]]
      [:h1
       {:margin [[(em 0) (em 0.2) (em 0) (em 0.7)]]
        :display       :inline-block}]
      [:em
       {:font-size (em 1.3)
        :font-weight :bold
        :color colour/gray}]
      [:.witan-search-input
       {:top (px 5)}]]

     [:.witan-page-content
      {:position :absolute
       :top app-peripheral-height
       :bottom 0
       :left 0
       :right 0
       :overflow-y :auto
       :overflow-x :hidden}
      [:#container
       {:width (percent 99)}]]

     [:.witan-dash-heading

      [:.pure-form
       {:display        :inline-flex
        :font-size      (px 14)
        :vertical-align :text-bottom
        :margin-left    (em 1)}]]

     [:#witan-main-content
      {:padding-left  (px 30)
       :padding-right (px 30)}]

     [:#witan-dash-forecast-list
      [:th :td
       {:border (px 0)
        :padding-left (em 0.5)}]
      [:th:first-child
       {:width (px 1)}]
      [:table
       {:width (percent 99)}]]

     [:.witan-forecast-table-row
      {:border-bottom "1px solid #ddd"
       :height (em 3)}
      [:.modifier
       {:color       colour/gray
        :margin-left (em 0.5)}]
      [:.tree-control
       {:padding 0
        :padding-right (px 4)}]
      [:&:hover
       {:background-color colour/row-highlight
        :cursor           :pointer}]
      [:.version-labels
       {:display :inline
        :margin-left (em 0.5)}]
      [:.tag-labels
       {:display :inline
        :margin-left (em -1)}]
      [:.first-round
       {:border-radius "2px 0px 0px 2px"}]
      [:.last-round
       {:border-radius "0px 2px 2px 0px"}]
      ["span::selection"
       {:background-color :transparent}]
      ["td::selection"
       {:background-color :transparent}]]

     [:.witan-forecast-table-row-descendant
      {:color     colour/darker-gray
       :font-size (em 0.9)
       :border-bottom 0}
      [:.name
       {:margin-left (em 1)}]]

     [:.witan-forecast-table-row-selected
      :.witan-forecast-table-row-selected:hover
      {:background-color colour/row-selected
       :color colour/white}]

     [:.witan-forecast-table-version-descendant
      {:margin-left (em 0.3)}]

     [:.witan-pw-message-box
      {:padding       (em 1)
       :box-shadow "0px 0px 10px #999999"}
      [:button
       {:margin-left (em 1)
        :height (em 2)
        :color colour/white
        :line-height (px 0)
        :padding "0em 1em"}
       [:&#create
        {:background-color colour/button-success}]
       [:&#revert
        {:background-color colour/button-error}]]]

     [:#witan-pw-edits
      {:background-color colour/forecast-changed-light
       :border-color colour/forecast-changed}
      [:#witan-pw-edits-text
       {:text-align :center
        :padding-right (em 1)}]]

     [:#witan-pw-in-prog
      {:background-color colour/in-progress-light
       :border-color colour/in-progress}
      [:button
       {:margin-left (em 1)
        :display :inline
        :background-color colour/in-progress}]
      [:#refresh
       {:color colour/white}]]
     [:#witan-pw-in-prog-text
      {:display :inline
       :text-align :center
       :color colour/white}]

     [:#witan-pw-missing
      {:background-color colour/error-light
       :border-color colour/error}
      [:#witan-pw-missing-text
       {:text-align :center}]]

     [:#witan-pw-action-body
      {:margin-bottom (em 1)}
      [:.model-value
       {:margin (em 1)
        :display :inline-block
        :color colour/gray}
       [:p
        {:display :inline-block}]]
      [:.property
       {:margin-bottom 0
        :margin-top (em 0.5)}]]

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

      [:.version-zero
       {:color colour/new-forecast}]
      [:.labels
       {:display     :inline
        :margin-left (em 0.5)
        :font-size (percent 150)
        :position :absolute
        :top (px 18)}]]

     [:.witan-pw-nav-button
      {:text-align :center
       :color colour/black
       :display :block
       :height :inherit
       :position :relative}
      [:i
       {:top (percent 40)
        :position :absolute
        :left (percent 10)
        :right (percent 10)}]]

     [:#witan-pw-body
      {:position :absolute
       :top app-peripheral-height
       :bottom 0
       :left 0
       :right 0
       :overflow-y :auto
       :overflow-x :hidden}]

     [:#witan-pw-stage-desc
      {:box-shadow "0px 0px 10px #999999"
       :border "1px solid silver"
       :z-index 10
       :position :relative
       :background-color colour/light-gray}
      [:p {:line-height (em 1.4)
           :margin-top (em 1.2)
           :min-width (px 660)}]
      [:h2 {:margin-top (em 0.4)
            :margin-bottom (em 0)
            :padding-bottom (em 0.4)
            :border-bottom "1px solid silver"}]]

     [:#witan-pw-area
      {:line-height (em 1.6)
       :text-align    :center
       :overflow-y :auto
       :margin "0px auto"
       :padding-top (em 1)}

      [:h2
       {:color       colour/black
        :margin      (px 0)
        :padding      (px 5)
        :font-weight 400}]

      [:.witan-pw-input-data-row
       [:small
        {:display :block
         :line-height (px 10)}]
       [:strong
        {:display :block
         :margin 0
         :font-size (em 1.2)}]
       [:.description
        {:display :inline-block
         :margin-bottom (em 1)
         :font-size (em 0.8)
         :line-height (em 1.4)}]
       [:.not-specified
        {:color colour/error}]
       [:.edited
        {:color colour/forecast-changed}]
       [:button
        {:float :left
         :margin-right (em 1)}]
       [:tr
        [:td {:vertical-align :bottom}]]]

      [:.witan-pw-browse-toggle
       {:background-color colour/normal
        :color colour/white}]

      [:.download
       {:margin-left (em 0.5)
        :margin-bottom (em 0.5)
        :width (em 8)}]]

     [:.witan-pw-input-browser-container
      {:padding-top (em 1)
       :transition "height 0.5s"}

      [:.witan-pw-input-browser
       {:width (percent 100)
        :height (percent 100)
        :background-color colour/input-browser
        :overflow :hidden ;; TODO this should be scroll, really, else you can't see the upload form on a small device
        :box-shadow "inset 0px 0px 10px rgba (0,0,0,0.8)"}

       [:.witan-pw-input-browser-content
        {:margin (em 1)
         :height (percent 90)}
        [:h3 {:margin-top (px 0)}]
        [:.spacer
         {:margin-top (em 1.4)}]

        [:.witan-pw-input-browser-content-search
         {:text-align :left
          :height (percent 90)}
         [:.spacer
          {:margin-top (em 0.5)}]
         [:.search-input
          {:width (percent 100)
           :padding-bottom (em 0.5)}
          [:.search-input-inner
           {:width (percent 50)
            :margin-right (em 1)}]
          [:form
           {:width (percent 100)
            :display :inline-block}]
          [:button
           {:margin-right (em 0.4)}]]
         [:.headings
          {:position :relative
           :margin-left  (em 0.5)
           :margin-right (em 2)
           :font-weight :bold}]
         [:.list
          {:position   :relative
           :height     (percent 65)
           :overflow-y :scroll}
          [:.data-item
           {:cursor       :pointer
            :transition   :none
            :line-height  (em 1.8)
            :width        (percent 99)
            :padding-left (em 0.5)
            :border-top   "1px solid #9E9E9E"}
           [:&:hover
            {:background-color colour/row-highlight}]
           ["::selection"
            {:background-color :transparent}]]
          [:.selected
           {:background-color colour/row-selected
            :color colour/white}
           [:&:hover
            {:background-color colour/row-selected}]]]]

        [:.witan-pw-input-browser-content-upload
         {:text-align :left}
         [:.container
          {:padding-left (em 1)
           :margin-left (em 1)
           :border-left [[(px 1) :solid 'silver]]
           :height (px 300)}]
         [:.upload-button
          {:font-size (percent 100)
           :font-weight :bold
           :margin-top (px 5)}]]]]]

     [:#witan-new-forecast-container
      {:padding (em 1)}
      [:#model-information
       {:padding [[(px 0) (px 30)]]}]]

     [:#witan-pw-forecast-nav
      {:margin-bottom (em 1)
       :display :inline-flex}
      [:.witan-pw-forecast-nav-box
       {:display :inline-block
        :width (em 16)
        :height (em 8)
        :margin (em 1)
        :border-radius (px 4)
        :cursor :pointer
        :transition "box-shadow 0.3s"
        :line-height (em 0.6)}
       ["::selection"
        {:background-color :transparent}]
       [:h1 :h2
        {:color colour/white
         :transition "color 0.5s"}]
       [:h3
        {:color colour/white
         :transition "color 0.5s"
         :letter-spacing (px -1)}]]
      [:.input  {:background-color colour/forecast-input-gs}]
      [:.model  {:background-color colour/forecast-model-gs}]
      [:.output {:background-color colour/forecast-output-gs}]
      [:.active {:box-shadow "8px 8px 8px #777777"}
       [:h1 :h2 :h3
        {:color :initial}]
       [:.action
        [:h2
         {:color "#444444"}]]]
      [:.icon
       [:h2 {:margin-top (em 0.4)}]]]

     [:.witan-pw-output-data-row
      [:span
       {:font-size (em 1)}]
      [:small
       {:display :block
        :line-height (em 0.8)
        :font-style :italic
        :font-size (em 0.8)
        :color colour/gray}]]

     [:.view-overlay
      {:width      (percent 100)
       :height     (percent 100)
       :position   :absolute
       :top        (px 100)
       :left       (px 0)
       :text-align :center}
      [:#loading
       {:color      colour/white
        :margin-top (percent 20)}]]

     [:.sidebar-link
      {:margin [[(em 1.2) (em 0)]]
       :display :block
       :color colour/light-gray
       :font-size (px 11)
       :text-decoration :none
       :cursor :pointer}
      [:span
       {:margin-top (em 0.3)
        :display :block}]
      [:&:hover
       {:color colour/white}]]])))
