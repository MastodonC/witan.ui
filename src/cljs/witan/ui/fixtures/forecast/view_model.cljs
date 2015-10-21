(ns ^:figwheel-always witan.ui.fixtures.forecast.view-model
    (:require [om.core :as om :include-macros true]
              [witan.ui.services.data :as data]
              [venue.core :as venue :include-macros true])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-initialise
  [owner cursor])

(defn on-activate
  [owner {:keys [id action version]} cursor]
  (om/update! cursor :id id)
  (om/update! cursor :action (or (not-empty action) "input"))
  (om/update! cursor :version version)
  (when (data/logged-in?)
    (venue/request! {:owner owner
                     :service :service/data
                     :request :fetch-forecast
                     :args {:id id :version version}
                     :context cursor})))

(defmethod response-handler
  [:fetch-forecast :success]
  [owner _ forecast cursor]
  (om/update! cursor :forecast forecast))

(defmethod event-handler
  :revert-forecast
  [owner _ _ cursor]
  (om/update! cursor :edited-forecast nil))

(defmethod event-handler
  :refresh-forecast
  [owner _ _ cursor]
  (.reload (.-location js/window)))
