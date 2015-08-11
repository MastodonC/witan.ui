(ns witan.ui.styles.login
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.ui.styles.fonts :as f]
            [witan.ui.styles.colours :as colour]
            [witan.ui.styles.util :refer [url]]))

;; Change defstylesheet to defstyles.
(defstyles login
  (-> (concat
        ;; fonts
        f/font-face-definitions

        ;; style
        [
         ;; tags
         [:*
          {:box-sizing :border-box}]
         [:html :body
          {:width (percent 100)
           :height (percent 100)
           :margin (px 0)
           :color colour/white
           :font-family f/base-fonts}]

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

         [:.title
          {:position :absolute
           :top (percent 15)
           :left (percent 5)}]

         [:.title
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
         ])
      vec))
