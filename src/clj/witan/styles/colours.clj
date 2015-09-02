(ns witan.styles.colours
  (:require [garden.color :as color :refer [rgb rgba]]))

;; some presets
(def white (:white color/color-name->hex))
(def black (:black color/color-name->hex))
(def gray (:gray color/color-name->hex))
(def dark-gray (:darkgray color/color-name->hex))
(def darker-gray (color/darken (:darkgray color/color-name->hex) 20))

;;
(def button-success "#59cd90")
(def button-error "#ee6352")
(def button-warning "#fac05e")
(def button-primary "#3590f3")

;;
(def forecast-input "#9fc5f8")
(def forecast-model "#ffd966")
(def forecast-output "#b6d7a8")
(def forecast-group "#c0c5f7")

;; page background
(def bg white)

;; - primary
(def primary black)

;; - secondary
(def secondary (color/lighten primary 30))

;; - tertiary
(def tertiary (rgb 100 100 100))

;; used as a block colour background for menu/nav bars
(def header primary)

;; title (h1) of the page
(def title primary)

;; page subtitle and also h2
(def subtitle secondary)

;; paragraph heading (h3)
(def para-heading tertiary)

;; menu bar links (assumes that `header` is background)
(def menu-item white)

;; same as above
(def menu-item-hover secondary)

;; hr tags
(def hr (rgb 171 211 255))

;; login b
(def login-black-bg (rgba 0 0 0 0.75))

;; links
(def link (rgb 76 174 207))

;; row highlight
(def row-highlight (rgb 255 252 219))

;; row selected
(def row-selected (color/darken row-highlight 30))
