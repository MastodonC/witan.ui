(ns witan.styles.base
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.styles.fonts :as f]
            [witan.styles.colours :as colour]
            [witan.styles.util :refer [url]]))

;; Change defstylesheet to defstyles.
(defstyles base
  (-> (concat
        ;; fonts
        f/font-face-definitions

        ;; style
        [
         ;; tags
         [:html
          {:background-color colour/bg
           :font-family f/base-fonts}]
         [:body
          {:font-size (px 16)
           :line-height 1.5}]
         [:body :h1 :h2 :h3 :h4 :h5
          {:font-family f/base-fonts}]
         [:h1
          {:color colour/title}]
         [:h2
          {:color colour/subtitle}]
         [:h3
          {:color colour/para-heading}]
         [:hr
          {:color colour/hr
           :background-color colour/hr}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

         ;; witan
         [:#witan-layout
          {:padding-left (px 115)
           :left (px 0)
           :position :relative}]

         [:#witan-layout :#witan-menu, :.witan-menu-link
          {:transition "all 0.2s ease-out 0s"}]

         [:#witan-menu
          {:background-color colour/header
           :padding (em 0.5)
           :box-shadow "0px 1px 3px rgba(0, 0, 0, 1)"
           :left (px 150)
           :margin-left (px -150)
           :position :fixed
           :top (px 0)
           :bottom (px 0)
           :text-align :center}]

         [:.witan-menu-item
          {:color colour/menu-item
           :padding-top (em 1)}
          [:a
           {:text-decoration :none
            :color colour/menu-item
            :border "medium none"}]
          [:a:hover
           {:color colour/menu-item-hover}]]

         [:#witan-page-title
          {:margin (px 0)
           :text-align :center
           :border-bottom "1px solid #aaa"}
          [:h1
           {:font-size (em 4)
            :font-weight 400
            :margin (px 0)}]
          [:h2
           {:font-weight 300
            :color colour/subtitle
            :padding (px 0)
            :margin-top (px -20)
            :letter-spacing (px 3)}]]

         [:#witan-main-content
          {:padding-left (px 30)
           :padding-right (px 30)}]

         [:.witan-pattern-example
          {:position :relative
           :min-height (px 100)}
          [:pre
           {:background-color colour/pattern-example-pre
            :font-size (em 0.8)}]]

         [:.witan-pattern-example-code
          {:position :absolute
           :top (percent 25)}]

         ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

         ;; overrides
         [:.pure-menu-heading
          {:color colour/menu-item
           :font-size (px 20)}]])
      vec))
