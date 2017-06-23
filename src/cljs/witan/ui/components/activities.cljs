(ns witan.ui.components.activities
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [inflections.core :as i])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn activity
  [{:keys [status message time]}]
  [:div.flex-vcenter.activity
   {:key time}
   [:div.flex-vcenter
    {:key "left"}
    (if (= :completed status)
      (icons/tick-circle :medium :success)
      (icons/error :medium :error))
    [:span.message message]]
   [:span.time
    {:key "right"}
    (time/iso-time-as-moment time)]])

(defn activities
  [activities]
  [:div.activities
   (doall
    (for [[i obj] (map-indexed vector (interpose :hr activities))]
      (if (= :hr obj)
        [:hr {:key i}]
        (activity obj))))])

(defn view
  []
  (fn []
    (let [acts (data/get-in-app-state :app/activities :activities/log)]
      [:div#activity-view
       [:div.container
        (shared/header :string/activity :string/activity-desc)
        [:div.content
         (if (zero? (count acts))
           [:h3 (get-string :string/no-activity)]
           (activities (reverse acts)))]]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcard activity-success-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element (activity @data))]))
  {:message "This activity succeeded!"
   :status :completed
   :time (time/jstime->str)}
  {:inspect-data true
   :frame true
   :history false})

(defcard activity-failure-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element (activity @data))]))
  {:message "This activity failed!"
   :status :failed
   :time (time/jstime->str)}
  {:inspect-data true
   :frame true
   :history false})

(defcard activities-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element (activities @data))]))
  [{:message "1 This activity succeeded!"
    :status :completed
    :time (time/jstime->str)}
   {:message "2 This activity failed!"
    :status :failed
    :time (time/jstime->str)}
   {:message "3 This activity succeeded!"
    :status :completed
    :time (time/jstime->str)}
   {:message "4 This activity failed!"
    :status :failed
    :time (time/jstime->str)}]
  {:inspect-data true
   :frame true
   :history false})
