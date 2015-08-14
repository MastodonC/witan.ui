(ns ^:figwheel-always witan.ui.core
    (:require [om.core :as om :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [schema.core :as s :include-macros true]
              [secretary.core :as secretary :refer-macros [defroute]]
              ;;
              [witan.ui.library :as l]
              [witan.schema.core :refer [Projection]]
              [witan.ui.components.dashboard]
              [witan.ui.components.menu])

    (:import goog.History))

(enable-console-print!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEFS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def strings
  {:witan-title "Witan for London"
   :projections "projections"
   :filter      "Filter"})

(defonce navigation-state
  (atom [{:name "Dashboard" :path "/" :view witan.ui.components.dashboard}
         {:name "New Projection" :path "/new-projection" :view nil}]))

(defonce app-state (atom {:strings strings
                          :projections [{:id "1234"
                                         :name "Population Projection for Camden"
                                         :type :population
                                         :owner "Camden"
                                         :version 3
                                         :last-modified "Aug 10th, 2015"
                                         :last-modifier "Neil"}
                                        {:id 5678
                                         :name "Population Projection for Bexley"
                                         :type :population
                                         :owner "Bexley"
                                         :version 3
                                         :last-modified "July 22nd, 2015"
                                         :last-modifier "Sarah"}]
                          :selected-projection {}}))

(def app-element
  (. js/document (getElementById "witan-main")))

(def history (History.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING TABLE
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; FIXME: this wants to be done automatically from the nav state above

(defroute "/new-projection" []
  (om/root
   (fn [c o]
     (om/component (html [:h1 "HELLO NEW PROJECTION"])))
   app-state
   {:target app-element}))

(defroute "/"
  []
  (om/root
   witan.ui.components.dashboard/view
   app-state
   {:target app-element}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn refresh-navigation []
  (let [token (.getToken history)
        set-active (fn [nav]
                     (assoc nav :active (= (:path nav) token)))]
    (swap! navigation-state #(map set-active %))))

(defn on-navigate [event]
  (refresh-navigation)
  (secretary/dispatch! (.-token event)))

(doto history
  (goog.events/listen EventType/NAVIGATE on-navigate)
  (.setEnabled true))

(defn on-js-reload [])


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONSTANT COMPONENTS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(om/root
 witan.ui.components.menu/view
 app-state
 {:target (. js/document (getElementById "witan-menu"))})
