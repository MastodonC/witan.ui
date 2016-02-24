(ns witan.ui.secondary-data-search
  (:require [cljs.test :refer-macros [is async]]
            [om.dom :as dom :include-macros true]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab :include-macros true])
  (:require-macros
   [devcards.core :as dc :refer [defcard deftest]]))

(defcard second-card
  (sab/html [:div
             [:h1 "This is your second devcard! foo"]]))

(defcard second-card-2
  (sab/html [:div
             [:h1 "This is your second devcard! foo bar"]]))
