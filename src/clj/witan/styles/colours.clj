(ns witan.styles.colours
  (:require [garden.color :as color :refer [rgb rgba]]))

;; some presets
(def white (:white color/color-name->hex))
(def black (:black color/color-name->hex))
(def gray (:gray color/color-name->hex))
(def dark-gray (:darkgray color/color-name->hex))
(def darker-gray (color/darken (:darkgray color/color-name->hex) 20))

;;
(def error   (rgb 238 66 102))
(def warning (rgb 242 158 76))
(def success (rgb 14 173 105))
(def normal  (rgb 4 139 168))
(def deep    (rgb 29 53 87))
(def error-light (color/lighten error 20))

;;
(def button-success   success)
(def button-success-light (color/lighten success 10))
(def button-error     error)
(def button-warning   warning)
(def button-primary   normal)
(def button-secondary (color/lighten button-primary 10))

;;
(def forecast-input (rgb 77 157 224))
(def forecast-model (rgb 225 188 41))
(def forecast-output (rgb 59 178 115))
(def forecast-group (rgb 253 26 68))
(def forecast-changed (rgb 230 145 56))
(def forecast-input-light  (color/lighten forecast-input 12))
(def forecast-model-light  (color/lighten forecast-model 12))
(def forecast-output-light (color/lighten forecast-output 12))
(def forecast-changed-light (color/lighten forecast-changed 12))

;; page background
(def bg white)

;; - primary
(def primary black)

;; - secondary
(def secondary (color/lighten primary 30))

;; - tertiary
(def tertiary (rgb 100 100 100))

;; used as a block colour background for menu/nav bars
(def header (rgb 37 37 37))

;; title (h1) of the page
(def title primary)
(def title-light (color/lighten primary 40))

;; page subtitle and also h2
(def subtitle secondary)

;; paragraph heading (h3)
(def para-heading tertiary)
(def para-heading-light (color/lighten tertiary 30))

;; menu bar links (assumes that `header` is background)
(def menu-item white)

;; same as above
(def menu-item-hover secondary)

;; hr tags
(def hr black)

;; login b
(def login-black-bg (rgba 0 0 0 0.75))

;; links
(def link (rgb 76 174 207))

;; row highlight
(def row-selected (rgb 66 133 244))
(def row-highlight (color/lighten gray 46))

;; themes
(def in-progress (rgb 84 13 110))
(def in-progress-light (color/lighten in-progress 20))

(def new-forecast (rgb 234 122 244))
(def input-browser (rgb 204 224 241))
