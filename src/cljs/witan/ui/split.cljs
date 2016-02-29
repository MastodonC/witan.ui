(ns witan.ui.split
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.primary :as primary]
            [witan.ui.secondary :as secondary]
            [witan.ui.utils :as utils]
            [witan.ui.icons :as icons])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defui Main
  static om/IQuery
  (query [this]
         [:app/route-params
          {:workspace/primary (om/get-query primary/Main)}
          {:workspace/secondary (om/get-query secondary/Main)}])
  Object
  (componentDidMount [this]
                     (js/Split.
                      (clj->js ["#primary" "#secondary"])
                      (clj->js {:direction "vertical"
                                :sizes [50, 50]
                                :gutterSize 8
                                :minSize 200
                                :cursor "row-resize"})))
  (render [this]
          (let [{:keys [app/route-params workspace/primary workspace/secondary]} (om/props this)]
            (sab/html
             [:div#split
              [:div#primary
               (primary/primary-split-view primary)]
              [:div#secondary
               (secondary/secondary-split-view secondary)]
              #_[:div#loading
                 [:div
                  (icons/cog :x-large :spin :dark)]]]))))

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
