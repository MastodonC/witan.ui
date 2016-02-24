(ns witan.ui.style.fonts
  (:require [garden.stylesheet :as gs]))

(def base-fonts  ["'Fira Sans'" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])
(def title-fonts ["'Kadwa'" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])

(def style
  [(gs/at-font-face
    {:font-family "'Fira Sans', sans-serif"})
   (gs/at-font-face
    {:font-family "'Kadwa', serif"})
   [:body :p
    {:font-family base-fonts}]
   [:h1 :h2 :h3 :h4 :h5
    {:font-family title-fonts}]])
