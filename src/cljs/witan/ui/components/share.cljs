(ns ^:figwheel-always witan.ui.components.share
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [schema.core :as s :include-macros true]
              ;;
              [witan.ui.widgets :as widgets]
              [witan.schema.core :refer [Forecast]]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.async :refer [raise!]]
              [witan.ui.refs :as refs]
              [witan.ui.util :refer [goto-window-location!]]
              [witan.ui.nav :as nav]))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (let [{:keys [id]} (first opts)]
            (html
             [:h1 (str "Sharing: " id)]))))
