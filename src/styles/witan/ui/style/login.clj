(ns witan.ui.style.login
  (:require [garden.units :refer [px percent em]]
            [witan.ui.style.colour :as colour]))

(def style
  [;; classes
   [:.trans-bg
    {:background      "transparent url ('../img/trans75.png')"
     :background-clip :padding-box}]

   [:#login-bg
    {:position   :fixed
     :top        (px 0)
     :left       (px 0)
     :width      (percent 100)
     :height     (percent 100)
     :z-index    55
     :background "url ('../img/login-bg.jpg') no-repeat center center fixed"
     :filter     "progid:DXImageTransform.Microsoft.AlphaImageLoader (src='.myBackground.jpg', sizingMethod='scale')"
     :-ms-filter "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='myBackground.jpg', sizingMethod='scale')"}
    ^:prefix {:background-size :cover}]

   [:#bg-attribution
    {:color       'gray
     :font-size   (em 0.65)
     :font-family :monospace
     :position    :absolute
     :bottom      (px 0)
     :right       (px 0)
     :margin      (px 5)
     :padding     (px 3)}
    [:a
     {:text-decoration :none
      :color 'white}]]

   [:.login-title
    [:h1
     {:font-size   (em 5)
      :font-weight 700
      :padding     "20px 0px 0px 20px"
      :margin      "0px 0px 20px 0px"
      :color       'white
      :line-height (em 1)}]
    [:h2
     {:font-size   (em 4)
      :font-weight 400
      :padding     "0px 20px 10px 20px"
      :margin      (px 0)
      :color 'white
      :line-height (em 1)}]]

   [:#content-container
    {:position :absolute
     :top      (px 0)
     :left     (px 0)
     :width    (percent 100)
     :z-index    56}]

   [:#relative-container
    {:position :relative
     :padding  (percent 5)
     :display  :inline-block}]

   [:#witan-login
    {:margin-top    (em 3)
     :margin-bottom (em 1)
     :padding       "1px 30px 20px 20px"
     :height        (percent 100)
     :width         (px 300)}
    [:h3
     {:color colour/login-subtitles}]
    [:#loading
     {:color 'white
      :margin  "15px auto 0px auto"
      :display :table}]
    [:input
     {:margin-bottom (em 0.5)
      :width         (percent 100)}]
    [:#error-message
     {:color         colour/error
      :display       :block
      :margin-bottom (em 1)}]
    [:#forgotten-link
     {:font-size      (px 10)
      :vertical-align :text-top
      :text-align     :right
      :float          :right
      :color          "#0078e7"
      :cursor         :pointer}]
    [:.sub-page-div
     [:#back-button
      {:float :right}]]
    [:#witan-copy
     {:margin-top    (px 20)
      :margin-bottom (px 20)}]
    [:p :#reset-instructions
     {:color 'white}]
    [:.pure-button
     {:box-shadow "0px 0px 0px #fff"}]]])
