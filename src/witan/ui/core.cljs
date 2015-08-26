(ns ^:figwheel-always witan.ui.core
  (:require [cljs.core.async :as async :refer [>! <! alts! chan close!]]
            [om.core :as om :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [schema.core :as s :include-macros true]
            [secretary.core :as secretary :refer-macros [defroute]]
            ;;
            [witan.schema.core :refer [Forecast]]
            [witan.ui.controllers.input]
            [witan.ui.data :as data]
            [witan.ui.nav :as nav]
            [witan.ui.components.forecast]
            [witan.ui.components.dashboard]
            [witan.ui.components.menu]
            [witan.ui.components.new-forecast]
            [witan.ui.components.share])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]])

  (:import goog.History))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEFS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce define-comms-channels
  (do
    (reset! nav/comms
            {:input (chan)})))

(defonce define-views
  (do
    (reset! nav/views
            {:forecast     witan.ui.components.forecast/view
             :dashboard      witan.ui.components.dashboard/view
             :new-forecast witan.ui.components.new-forecast/view
             :menu           witan.ui.components.menu/view
             :share          witan.ui.components.share/view})))

(defonce strings
  {:witan-title             "Witan for London"
   :forecasts             "forecasts"
   :filter                  "Filter"
   :forecast-name         "Name"
   :forecast-type         "Type"
   :forecast-owner        "Owner"
   :forecast-version      "Version"
   :forecast-lastmodified "Last Modified"})

(defonce define-app-state
  (do
    (reset! data/app-state {:strings strings
                            :current-route nil
                            :forecasts []
                            :forecasts-meta {:expanded #{}
                                               :selected []
                                               :has-ancestors #{}
                                               :filter nil}})
    (data/load-dummy-data!)))

;; VALIDATE - make sure our app-state matches the schema
;; FIXME we should only do this in dev/testing (possibly staging?)
(doseq [p (:forecasts @data/app-state)]
  (s/validate Forecast p))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def history (History.))

(defn on-navigate [event]
  (let [path (.-token event)]
    (swap! data/app-state assoc :current-route path)
    (secretary/dispatch! path)))

(defonce set-up-history!
  (doto history
    (goog.events/listen EventType/NAVIGATE on-navigate)
    (.setEnabled true)))

(defn on-js-reload []
  ;; this is required for the figwheel reload
  (om/detach-root (nav/find-app-container))
  (secretary/dispatch! (:current-route @data/app-state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MESSAGE HANDLING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(go
  (while true
    (alt!
      (:input @nav/comms) ([v] (witan.ui.controllers.input/handler v (om/root-cursor data/app-state)))
      ;; Capture the current history for playback in the absence
      ;; of a server to store it
      (async/timeout 10000) (do #_(print "TODO: print out history: ")))))
