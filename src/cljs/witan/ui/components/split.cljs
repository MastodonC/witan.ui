(ns witan.ui.components.split
  (:require  [sablono.core :as sab]
             [reagent.core :as r]
             [witan.ui.components.primary :as primary]
             [witan.ui.components.secondary :as secondary]
             [witan.ui.utils :as utils]
             [witan.ui.data :as data]
             [witan.ui.components.icons :as icons])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn view
  []
  (r/create-class
   {:component-did-mount
    (fn [this]
      (js/Split.
       (clj->js ["#primary" "#secondary"])
       (clj->js {:direction "vertical"
                 :sizes [50, 50]
                 :gutterSize 8
                 :minSize 200
                 :cursor "row-resize"})))
    :reagent-render
    (fn []
      (let [{:keys [workspace/primary workspace/secondary]}
            (data/get-app-state :app/workspace)]
        [:div#split
         [:div#primary
          #_(primary/view primary)]
         [:div#secondary
          #_(secondary/view secondary)]
         #_[:div#loading
            [:div
             (icons/cog :x-large :spin :dark)]]]))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcard primary-switcher
  (fn [data _]
    (sab/html
     (primary/switcher {:icon-0 (partial icons/topology :dark :medium)
                        :icon-1 (partial icons/visualisation :dark :medium)
                        :selected-idx (:selected-idx @data)
                        :on-select (partial swap! data assoc :selected-idx)})))
  {:selected-idx 0}
  {:inspect-data true
   :frame true
   :history false})

(defcard secondary-switcher
  (fn [data _]
    (sab/html
     (secondary/switcher {:titles ["Foo" "Bar" "Baz"]
                          :selected-idx (:selected-idx @data)
                          :on-select (partial swap! data assoc :selected-idx)})))
  {:selected-idx 0}
  {:inspect-data true
   :frame true
   :history false})
