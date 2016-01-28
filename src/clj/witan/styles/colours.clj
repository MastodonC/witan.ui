(ns witan.styles.colours
  (:require [garden.color :as color :refer [rgb rgba]]))

;; some presets
(def white (:white color/color-name->hex))
(def black (:black color/color-name->hex))
(def gray (:gray color/color-name->hex))
(def dark-gray (:darkgray color/color-name->hex))
(def darker-gray (color/darken (:darkgray color/color-name->hex) 20))
(def light-gray (:lightgray color/color-name->hex))
(def lighter-gray (color/lighten (:lightgray color/color-name->hex) 8))
(def lightest-gray (color/lighten (:lightgray color/color-name->hex) 12))

;;#####  Palette URL: http://paletton.com/#uid=72Z0B0klMikavurgZnhrfcv-T6C

;; green
(def color-primary-0 "#216840")
(def color-primary-1 "#74AD8D")
(def color-primary-2 "#3E845D")
(def color-primary-3 "#0B4725")
(def color-primary-4 "#002610")

;; blue
(def color-secondary-1-0  "#23425E")
(def color-secondary-1-1  "#6D869D")
(def color-secondary-1-2  "#3D5C78")
(def color-secondary-1-3  "#0D2840")
(def color-secondary-1-4  "#021322")

;; orange
(def color-secondary-2-0  "#92692F")
(def color-secondary-2-1  "#F2D2A3")
(def color-secondary-2-2  "#B99157")
(def color-secondary-2-3  "#64400F")
(def color-secondary-2-4  "#351F00")

;; red
(def color-complement-0  "#92432F")
(def color-complement-1  "#F2B4A3")
(def color-complement-2  "#B96C57")
(def color-complement-3  "#64200F")
(def color-complement-4  "#350B00")

;;
(def error   color-complement-0)
(def warning color-secondary-2-0)
(def success color-primary-0)
(def normal  color-secondary-1-0)
(def deep    color-secondary-1-3)
(def error-light color-complement-0)

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
(def forecast-superseded (rgb 10 10 10))
(def forecast-public normal)
(def forecast-input-light  (color/lighten forecast-input 12))
(def forecast-model-light  (color/lighten forecast-model 12))
(def forecast-output-light (color/lighten forecast-output 12))
(def forecast-changed-light (color/lighten forecast-changed 12))

(def forecast-input-gs (rgb 136 136 136))
(def forecast-model-gs (rgb 170 170 170))
(def forecast-output-gs (rgb 204 204 204))

;; page background
(def bg white)

;; used as a block colour background for menu/nav bars
(def header (rgb 37 37 37))

;; title (h1) of the page
(def title black)
(def title-light gray)

;; page subtitle and also h2
(def subtitle title-light)

;; paragraph heading (h3)
(def para-heading gray)
(def para-heading-light (color/lighten para-heading 30))

;; menu bar links (assumes that `header` is background)
(def menu-item white)
(def menu-background color-primary-2)
(def sidebar-background (color/lighten color-primary-4 10))
(def corner-background (color/lighten color-primary-4 5))

;; same as above
(def menu-item-hover gray)

;; hr tags
(def hr black)

;; login b
(def login-black-bg (rgba 0 0 0 0.75))

;; links
(def link color-secondary-1-3)

;; row highlight
(def row-selected color-secondary-1-2)
(def row-highlight (color/lighten gray 42))

;; themes
(def in-progress color-primary-0)
(def in-progress-light (color/lighten in-progress 20))
(def model-error color-complement-0)

(def new-forecast (color/lighten color-complement-2 10))
(def input-browser (color/lighten color-secondary-1-1 15))
