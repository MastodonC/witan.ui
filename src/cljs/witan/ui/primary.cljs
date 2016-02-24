(ns witan.ui.primary
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]))

(defui Main
  Object
  (render [this]
          (sab/html
           [:h1 "Primary"])))
