(ns witan.ui.style.icons
  (:require [garden.color :refer [rgba]]
            [garden.units :refer [px]]))

(def style
  [[:.material-icons
    {:font-size (px 32)}]
   [:.material-icons.md-s
    {:font-size (px 18)}]
   [:.material-icons.md-m
    {:font-size (px 32)}]
   [:.material-icons.md-l
    {:font-size (px 54)}]
   [:.material-icons.md-xl
    {:font-size (px 72)}]
   [:.material-icons.md-dark
    {:color (rgba 0 0 0 0.54)}]
   [:.material-icons.md-dark.md-inactive
    {:color (rgba 0 0 0 0.26)}]
   [:.material-icons.md-light
    {:color (rgba 255 255 255 1)}]
   [:.material-icons.md-light.md-inactive
    {:color (rgba 255 255 255 0.3)}]])
