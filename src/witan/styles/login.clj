(ns witan.styles.login
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.styles.fonts :as f]
            [witan.styles.colours :as colour]
            [witan.styles.util :refer [url]]))

;; Change defstylesheet to defstyles.
(defstyles login
  (-> (concat
        ;; fonts
       f/font-face-definitions

        ;; style
       [;; tags
        [:*
         {:box-sizing :border-box}]
        [:html :body
         {:width (percent 100)
          :height (percent 100)
          :margin (px 0)
          :color colour/white
          :font-family f/base-fonts}
         [:input {:color colour/dark-gray}]]

        [:a
         {:text-decoration :none}]
        [:a:hover
         {:text-decoration :underline}]

         ;; classes
        [:.bg
         {:display :table
          :position :relative
          :width (percent 100)
          :height (percent 100)
          :background "transparent url(\"../img/login-bg.jpg\") no-repeat scroll center center / cover"}]

        [:.bg-attribution
         {:position :absolute
          :background-color colour/login-black-bg
          :color colour/gray
          :font-size (em 0.65)
          :font-family :monospace
          :padding (em 0.2)
          :margin (em 0.2)
          :bottom (px 0)
          :right (px 2)}]

        [:.bg-attribution
         [:a
          {:text-decoration :none
           :color colour/white}]]

        [:#container
         {:position :absolute
          :top (percent 15)
          :left (percent 5)}]

        [:.title
         {:postion :relative}
         [:h1
          {:font-size (em 5)
           :font-weight 700
           :padding "0px 0px 0px 20px"
           :margin "10px 0px"
           :background-color colour/login-black-bg
           :background-clip :padding-box}]
         [:h2
          {:font-size (em 4)
           :font-weight 400
           :padding "0px 20px 10px 20px"
           :margin (px 0)
           :background-color colour/login-black-bg
           :background-clip :padding-box}]]

        [:#witan-login
         {:margin-top (em 3)
          :padding "1px 30px 20px 20px"
          :height (percent 100)
          :width (px 300)
          :background-color colour/login-black-bg}
         [:#loading
          {:color colour/white
           :margin "15px auto 0px auto"
           :display :table}]
         [:input
          {:margin-bottom (em 0.5)
           :width (percent 100)}]
         [:#forgotten-link
          {:font-size (px 10)
           :vertical-align :text-top
           :text-align :right
           :float :right
           :color colour/link
           :cursor :pointer}]
         [:.forgotten-div
          [:#back-button
           {:float :right}]]]])
      vec))
