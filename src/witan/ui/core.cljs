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
            [witan.schema.core :refer [Projection]]
            [witan.ui.util :refer [prependtial]]
            [witan.ui.components.dashboard]
            [witan.ui.components.menu]
            [witan.ui.components.new-projection]
            [witan.ui.components.projection]
            [witan.ui.controllers.input]
            [witan.ui.data :as data])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]])

  (:import goog.History))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEFS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def comms
  {:input (chan)})

(defonce strings
  {:witan-title             "Witan for London"
   :projections             "projections"
   :filter                  "Filter"
   :projection-name         "Name"
   :projection-type         "Type"
   :projection-owner        "Owner"
   :projection-version      "Version"
   :projection-lastmodified "Last Modified"})

;; this is the primary routing table
(defonce navigation-state
  (atom {:routes [{:name "Dashboard"
                   :path "/"
                   :view (fn [] witan.ui.components.dashboard/view)}
                  {:name "New Projection"
                   :path "/new-projection"
                   :view (fn [] witan.ui.components.new-projection/view)}
                  {:name "Projection Wizard"
                   :path "/projection/:id"
                   :view (fn [] witan.ui.components.projection/view)}]
         :current-route ""}))

(defonce define-app-state
  (do
    (reset! data/app-state {:strings strings
                            :projections []
                            :projections-meta {:expanded #{}
                                               :selected []
                                               :has-ancestors #{}}})
    (data/load-dummy-data!)))

;; VALIDATE - make sure our app-state matches the schema
;; FIXME we should only do this in dev/testing (possibly staging?)
(doseq [p (:projections @data/app-state)]
  (s/validate Projection p))

(def history (History.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn find-app-container
  []
  (. js/document (getElementById "witan-main")))

(defn install-om!
  [view params]
  (om/root
   (prependtial (view) params)
   data/app-state
   {:target (find-app-container)
    :shared {:comms comms}})

  (om/root
   witan.ui.components.menu/view
   data/app-state
   {:target (. js/document (getElementById "witan-menu"))}))

;; this automatically patches up the routing table that is defined above
(doseq [{:keys [path view]} (:routes @navigation-state)]
  (defroute (str path)
    {:as params}
    (install-om! view params)))

(defn on-navigate [event]
  (let [path (.-token event)]
    (swap! navigation-state assoc :current-route path)
    (secretary/dispatch! path)))

(defonce set-up-history!
  (doto history
    (goog.events/listen EventType/NAVIGATE on-navigate)
    (.setEnabled true)))

(defn on-js-reload []
  ;; this is required for the figwheel reload
  (om/detach-root (find-app-container))
  (secretary/dispatch! (:current-route @navigation-state)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; MESSAGE HANDLING
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(go
  (while true
    (alt!
      (:input comms) ([v] (witan.ui.controllers.input/handler v (om/root-cursor data/app-state)))
      ;; Capture the current history for playback in the absence
      ;; of a server to store it
      (async/timeout 10000) (do #_(print "TODO: print out history: ")))))
