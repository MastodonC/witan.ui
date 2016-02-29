(ns witan.ui.dashboard.shared
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.shared :as shared]
            [witan.ui.icons :as icons]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn header
  [{:keys [title buttons filter-txt filter-fn on-button-click]}]
  [:div.heading
   [:h1 (get-string title)]
   (shared/search-filter (get-string filter-txt) filter-fn)
   [:div.buttons
    (for [{:keys [icon txt class id]} buttons]
      [:div.button-container
       [:button.pure-button
        {:class class
         :on-click #(when on-button-click (on-button-click id))}
        (icon :small)
        (get-string txt)]])]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcard dashboard-header
  (fn [data _]
    (let [filter-fn (partial swap! data assoc :result)
          button-fn (partial swap! data assoc :last-button)]
      (sab/html
       [:div.dashboard
        {:style {:width "100%"}}
        (header {:title :string/workspace-dash-title
                 :filter-txt :string/workspace-dash-filter
                 :filter-fn filter-fn
                 :buttons [{:id :button-a :icon icons/open :txt :string/view   :class "workspace-view"}
                           {:id :button-b :icon icons/plus :txt :string/create :class "workspace-create"}]
                 :on-button-click button-fn})])))
  {:result ""
   :last-button nil}
  {:inspect-data true
   :frame true
   :history false})
