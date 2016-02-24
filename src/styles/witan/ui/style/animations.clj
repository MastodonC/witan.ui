(ns witan.ui.style.animations
  (:require [garden.def :as g]))

(g/defkeyframes keyframe-animations-spin
  [:from
   {:transform "rotate(0deg)"}]
  [:to
   {:transform "rotate(360deg)"}])

(def style
  [keyframe-animations-spin
   [:.anim-spin
    ^:prefix {:animation-name :keyframe-animations-spin
              :animation-duration "1500ms"
              :animation-iteration-count :infinite
              :animation-timing-function :linear}]])
