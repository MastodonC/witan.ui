(ns witan.ui.secondary
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]))

(defui Main
  Object
  (render [this]
          (sab/html
           [:h1 "Secondary"])))

(def secondary-split-view (om/factory Main))
