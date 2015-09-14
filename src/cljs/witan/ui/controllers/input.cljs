(ns ^:figwheel-always witan.ui.controllers.input
  (:require [om.core :as om :include-macros true]
            [schema.core :as s :include-macros true]
            [witan.schema.core :refer [Forecast]]
            [witan.ui.data :as d]
            [witan.ui.async :as a]
            [witan.ui.app :as app]
            [cljs.core.async :as async :refer [<!]])
  (:require-macros
   [cljs.core.async.macros :as am :refer [go]]))

(defn fetch-visible-forecasts
  [state]
  (let [all-expanded (-> state :view-state :forecasts :expanded)
        filter (-> state :view-state :forecasts :filter)
        toggled-on (mapv #(vector :db/id (first %)) all-expanded)]
    (d/fetch-forecasts {:expand toggled-on
                        :filter filter})))

(defmulti handler
  (fn [[event args] cursor] event))

(defmethod handler
  :event/attempt-login
  [[event args] cursor]
  (om/update! cursor [:login-state :phase] :waiting)
  (a/put! (:api @app/comms) :api/login args))

(defmethod handler
  :event/select-forecast
  [[event args] cursor]
  (s/validate Forecast args)
  (om/update! cursor [:view-state :forecasts :selected] (vector (:db/id args) (:id args))))

(defmethod handler
  :event/toggle-tree-view
  [[event args] cursor]
  (s/validate Forecast args)
  (let [db-id        (:db/id args)
        id           (:id args)
        toggled?     (contains? (-> cursor :view-state :forecasts :expanded) [db-id id])
        fn           (if toggled? disj conj)
        new-state    (om/transact! cursor [:view-state :forecasts :expanded] #(fn % [db-id id]))]
    (om/update! cursor :forecasts (fetch-visible-forecasts @new-state))))

(defmethod handler
  :event/filter-forecasts
  [[event args] cursor]
  (s/validate s/Str args)
  (let [new-filter (not-empty args)
        new-state (om/update! cursor [:view-state :forecasts :filter] new-filter)]
    (om/update! cursor :forecasts (fetch-visible-forecasts @new-state))))

(defmethod handler
  :event/show-password-reset
  [[event args] cursor]
  (let [path [:login-state :phase]]
    (if (= :prompt (get-in @cursor path))
      (om/update! cursor path :reset)
      (om/update! cursor path :prompt))))
