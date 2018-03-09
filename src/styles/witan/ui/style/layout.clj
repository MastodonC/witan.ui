(ns witan.ui.style.layout
  (:require [garden.units :refer [px percent]]
            [witan.ui.style.util :as util]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style
  [[:body {:width (percent 100)
           :height (percent 100)
           :overflow :hidden}]

   [:#logo {:background-color colour/html-bg
            :background-image (util/url "../img/witan-logo.png")
            :background-size (px 48);
            :background-repeat 'no-repeat
            :background-position [[(px 7) (px 5)]]
            :width (px 64)
            :height (px 48)
            :position :fixed
            :top 0
            :left 0
            :z-index 100}]

   [:#login {:position :absolute
             :top 0
             :left 0
             :bottom 0
             :right 0
             :background-color 'white
             :z-index 200}]

   [:#side {:background-color colour/side-bg
            :position :fixed
            :width values/app-peripheral-width
            :bottom 0
            :top (px 48)
            :left 0
            :overflow :hidden
            :z-index 20
            :box-shadow "2px 1px 4px #888"}]

   [:#app {;:background (util/url "../img/bg.png")
           :position :absolute;
           :top 0
           :left values/app-peripheral-width
           :bottom 0
           :right 0
           :overflow :hidden}
    [:#split-container
     {:height (percent 100)}]]])
