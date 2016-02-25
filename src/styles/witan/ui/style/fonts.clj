(ns witan.ui.style.fonts
  (:require [garden.stylesheet :as gs]
            [garden.units :refer [px em percent]]))

(def base-fonts  ["'Fira Sans'" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])
(def title-fonts ["'Kadwa'" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])

(def style
  [(gs/at-font-face
    {:font-family "'Fira Sans', sans-serif"})
   (gs/at-font-face
    {:font-family "'Kadwa', serif"})
   [:body :p :table
    {:font-family base-fonts
     :font-size (px 12.4)}]
   [:h1 :h2 :h3 :h4 :h5
    {:font-family title-fonts}]])
