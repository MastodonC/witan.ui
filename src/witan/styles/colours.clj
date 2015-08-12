(ns witan.styles.colours
  (:require [garden.color :as color :refer [rgb rgba]]))

;; some presets
(def white (rgb 255 255 255))
(def black (rgb 0 0 0))
(def gray (rgb 205 205 205))
(def dark-gray (rgb 100 100 100))


;; page background
(def bg white)

;; - primary
(def primary (rgb 45 62 80))

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

;; background for the code snippets in pattern library
(def pattern-example-pre (rgb 243 245 194))

;; hr tags
(def hr (rgb 171 211 255))

;; login b
(def login-black-bg (rgba 0 0 0 0.75))

;; links
(def link (rgb 76 174 207))
