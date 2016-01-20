(ns witan.styles.util
  (:require [garden.def :as g]
            [garden.color :refer [rgba]]
            [garden.units :refer [px]]))

(g/defcssfn url)

(g/defkeyframes keyframe-animations-spin
  [:from
   {:transform "rotate(0deg)"}]
  [:to
   {:transform "rotate(360deg)"}])

(def keyframe-animations
  [keyframe-animations-spin
   [:.anim-spin
    ^:prefix {:animation-name :keyframe-animations-spin
              :animation-duration "1500ms"
              :animation-iteration-count :infinite
              :animation-timing-function :linear}]])

(def material-icons
  [[:.material-icons.md-s
    {:font-size (px 18)}]
   [:.material-icons.md-l
    {:font-size (px 30)}]
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
