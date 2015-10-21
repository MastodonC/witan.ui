(ns ^:figwheel-always witan.ui.fixtures.share.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              ;;
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              ))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (let [id (-> cursor :view-state :share :id)]
            (html
             [:h1 (str "Sharing: " id)]))))
