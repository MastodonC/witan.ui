(ns witan.styles.login
  (:require [garden.def :refer [defstylesheet defstyles]]
            [garden.units :refer [px em percent]]
            [witan.styles.fonts :as f]
            [witan.styles.colours :as colour]
            [witan.styles.util :refer [url]]))

;; Change defstylesheet to defstyles.
(def login
  [;; classes
   [:.trans-bg
    {:background "transparent url ('../img/trans75.png')"
     :background-clip :padding-box}]

   [:.login-bg
    {:position :fixed
     :top (px 0)
     :left (px 0)
     :width (percent 100)
     :height (percent 100)
     :background "url ('../img/login-bg.jpg') no-repeat center center fixed"
     :filter "progid:DXImageTransform.Microsoft.AlphaImageLoader (src='.myBackground.jpg', sizingMethod='scale')"
     :-ms-filter "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='myBackground.jpg', sizingMethod='scale')"
     }
    ^:prefix{:background-size :cover}]

   [:.bg-attribution
    { :color colour/gray
     :font-size (em 0.65)
     :font-family :monospace
     :float :right}
    [:a
     {:text-decoration :none
      :color colour/white}]]

   [:.login-title
    [:h1
     {:font-size (em 5)
      :font-weight 700
      :padding "0px 0px 0px 20px"
      :margin "10px 0px"
      :color colour/white}]
    [:h2
     {:font-size (em 4)
      :font-weight 400
      :padding "0px 20px 10px 20px"
      :margin (px 0)
      :color colour/white}]]

   [:#content-container
    {:position :absolute
     :top (px 0)
     :left (px 0)
     :width (percent 100)}]

   [:#relative-container
    {:position :relative
     :padding (percent 5)
     :display :table-cell}]

   [:#witan-login
    {:margin-top (em 3)
     :margin-bottom (em 1)
     :padding "1px 30px 20px 20px"
     :height (percent 100)
     :width (px 300)
     }
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
      {:float :right}]]
    [:#witan-copy
     {:margin-top (px 20)
      :margin-bottom (px 20)}]
    [:#reset-instructions
     {:color colour/white}]]])
