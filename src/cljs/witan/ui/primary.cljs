(ns witan.ui.primary
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.icons :as icons])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn switcher
  [{:keys [icon-0 icon-1 selected-idx on-select]}]
  [:div.primary-switcher
   [:div#indicator-container
    [:div#indicator
     {:class (when (= selected-idx 1) "indicator-offset-1")}]]
   [:div.icons
    [:div.icon#icon-0
     {:class (when (= selected-idx 0) "selected")
      :on-click #(when on-select (on-select 0))}
     (icon-0)]
    [:div.icon#icon-1
     {:class (when (= selected-idx 1) "selected")
      :on-click #(when on-select (on-select 1))}
     (icon-1)]]])

(defui Main
  static om/IQuery
  (query [this]
         [{:app/workspace {:workspace/primary [:primary/view-selected]}}])
  Object
  (render [this]
          (log/debug "primary props" (om/props this))
          (let [{:keys [primary/view-selected]
                 :or {primary/view-selected 0}} (get (om/props this) :app/workspace)]
            (sab/html
             [:div#primary
              [:div#overlay
               (switcher {:icon-0 (partial icons/topology :dark :medium)
                          :icon-1 (partial icons/visualisation :dark :medium)
                          :selected-idx view-selected
                          :on-select #(om/transact! this `[(change/primary-view! {:idx ~%})])})]]))))

(def primary-split-view (om/factory Main))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcard switcher
  (fn [data _]
    (sab/html
     (switcher {:icon-0 (partial icons/topology :dark :medium)
                :icon-1 (partial icons/visualisation :dark :medium)
                :selected-idx (:selected-idx @data)
                :on-select (partial swap! data assoc :selected-idx)})))
  {:selected-idx 0}
  {:inspect-data true
   :frame true
   :history false})
