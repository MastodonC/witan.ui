(ns ^:figwheel-always witan.ui.fixtures.menu.view-model
    (:require [om.core :as om :include-macros true]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-initialise
  [owner cursor])

(defn on-activate
  [owner args cursor])
