(ns ^:figwheel-always witan.ui.fixtures.new-forecast.view
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
              ;;
            [witan.ui.widgets :as widgets]
            [witan.schema.core :refer [Forecast]]
            ))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (html
           [:h1 "New Forecast"])))
