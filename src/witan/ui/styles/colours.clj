(ns witan.ui.styles.colours
  (:require [garden.color :as color :refer [rgb]]))

;; page background
(def bg (rgb 255 255 255))

;; - primary
(def primary (rgb 45 62 80))

;; - secondary
(def secondary (color/lighten header 30))

;; - tertiary
(def tertiary (rgb 100 100 100))

;; used as a block colour background for menu/nav bars
(def header primary)

;; title (h1) of the page
(def title header)

;; page subtitle and also h2
(def subtitle secondary)

;; paragraph heading (h3)
(def para-heading tertiary)

;; menu bar links (assumes that `header` is background)
(def menu-item (rgb 255 255 255))

;; same as above
(def menu-item-hover secondary)

;; background for the code snippets in pattern library
(def pattern-example-pre (rgb 243 245 194))

;; hr tags
(def hr (rgb 171 211 255))
