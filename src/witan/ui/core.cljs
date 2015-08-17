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
  (atom [{:name "Dashboard"
          :path "/"
          :view (fn [] witan.ui.components.dashboard/view)}
         {:name "New Projection"
          :path "/new-projection"
          :view nil}]))

(defonce app-state (atom {:strings strings
                          :projections [{:id "1234"
                                         :name "Population Projection for Camden"
                                         :type :population
                                         :owner "Camden"
                                         :version 3
                                         :last-modified "Aug 10th, 2015"
                                         :last-modifier "Neil"
                                         :previous-version []}
                                        {:id 5678
                                         :name "Population Projection for Bexley"
                                         :type :population
                                         :owner "Bexley"
                                         :version 2
                                         :last-modified "July 22nd, 2015"
                                         :last-modifier "Sarah"
                                         :previous-version []}]
                          :selected-projection {}}))

;; VALIDATE - make sure our app-state matches the schema
(map #(s/validate Projection %) (:projections @app-state))

(def history (History.))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ROUTING FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn find-app-container
  []
  (. js/document (getElementById "witan-main")))

(defn install-om!
  [view]
  (om/root
   (view)
   app-state
   {:target (find-app-container)})

  (om/root
   witan.ui.components.menu/view
   app-state
   {:target (. js/document (getElementById "witan-menu"))}))

;; this automatically patches up the routing table that is defined above
(doseq [{:keys [path view]} @navigation-state]
  (defroute (str path) []
    (install-om! view)))

(defn refresh-navigation []
  (let [token (.getToken history)
        set-active (fn [nav]
                     (assoc nav :active (= (apply str (rest (:path nav))) token)))]
    (swap! navigation-state #(map set-active %))))

(defn on-navigate [event]
  (refresh-navigation)
  (secretary/dispatch! (.-token event)))

(doto history
  (goog.events/listen EventType/NAVIGATE on-navigate)
  (.setEnabled true))

(defn on-js-reload []
  (comment
    (om/detach-root (find-app-container))
    (install-om! (:view (first (filter :active @navigation-state))))))
