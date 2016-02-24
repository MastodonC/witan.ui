(ns witan.ui.style.colour
  (:require [garden.color :as color :refer [rgb rgba]]))

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

;; side
(def side-bg color-primary-2)
(def side-text 'white)
(def side-icons-inactive "#ccc")

;; dash
(def dash-heading-bg (color/lighten color-primary-1 20))
