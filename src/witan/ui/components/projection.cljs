(ns ^:figwheel-always witan.ui.components.projection
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
              ;;
            [witan.ui.widgets :as widgets]
            [witan.schema.core :refer [Projection]]
            [witan.ui.async :refer [raise!]]
            [witan.ui.refs :as refs]))

(defcomponent view
  [cursor owner {:keys [id] :as args}]
  (render [_]
          (html
           [:h1 (str "Projection: " id)])))
