(ns witan.ui.components.dashboard.shared
  (:require [sablono.core :as sab]
            ;;
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn header
  [{:keys [title subtitle buttons on-button-click]}]
  [:div.shared-heading
   [:h1 (get-string title)]
   (when subtitle
     [:span (get-string subtitle)])
   [:div.dash-buttons
    (for [button buttons]
      (shared/button button on-button-click))]])

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
                 :buttons [{:id :button-a :icon icons/open :txt :string/view   :class "workspace-view"}
                           {:id :button-b :icon icons/plus :txt :string/create :class "workspace-create"}]
                 :on-button-click button-fn})])))
  {:result ""
   :last-button nil}
  {:inspect-data true
   :frame true
   :history false})
