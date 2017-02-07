(ns witan.ui.style.icons
  (:require [garden.color :refer [rgba]]
            [garden.units :refer [px]]
            [witan.ui.style.colour :as c :refer [icon-colour]]))

(def style
  [[:.material-icons
    {:font-size (px 32)}]
   [:.material-icons.md-t
    {:font-size (px 10)}]
   [:.material-icons.md-s
    {:font-size (px 18)}]
   [:.material-icons.md-m
    {:font-size (px 32)}]
   [:.material-icons.md-l
    {:font-size (px 54)}]
   [:.material-icons.md-xl
    {:font-size (px 72)}]
   [:.material-icons.md-error
    {:color c/error}]
   [:.material-icons.md-success
    {:color c/success}]
   [:.material-icons.md-info
    {:color c/info-icon}]
   [:.material-icons.md-warning
    {:color c/warning}]
   [:.material-icons.md-silver
    {:color 'silver}]
   [:.material-icons.md-dark
    {:color icon-colour}]
   [:.material-icons.md-dark.md-inactive
    {:color (rgba 0 0 0 0.26)}]
   [:.material-icons.md-light
    {:color (rgba 255 255 255 1)}]
   [:.material-icons.md-light.md-inactive
    {:color (rgba 255 255 255 0.3)}]

   ;;

   [:.icon-and-text
    [:span
     {:vertical-align :super
      :margin-left (px 6)}]]])
