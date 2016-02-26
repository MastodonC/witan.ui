(ns witan.ui.style.fonts
  (:require [garden.stylesheet :as gs]
            [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]))

(def base-fonts  ["'Lato'" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])
(def title-fonts ["'Merriweather'" "serif"])

(def style
  [(gs/at-font-face
    {:font-family "'Lato', sans-serif"})
   (gs/at-font-face
    {:font-family "'Merriweather', serif"})
   [:body :p :table
    {:font-family base-fonts
     :font-size (px 12.4)}]
   [:h1 :h2 :h3 :h4 :h5
    {:font-family title-fonts
     :color colour/title-fonts-colour}]
   [:.text-center
    {:text-align :center}]])
