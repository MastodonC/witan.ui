(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]))

(defn view
  []
  (let [id (data/get-in-app-state :app/datastore :ds/current)
        md (data/get-in-app-state :app/datastore :ds/file-metadata id)]
    [:div
     [:span name]
     [:span (pr-str md)]]))
