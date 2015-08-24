(ns ^:figwheel-always witan.ui.nav
    (:require [cljs.core.async :as async :refer [>! <! alts! chan close!]]
              [secretary.core :as secretary :refer-macros [defroute]]
              [om.core :as om :include-macros true]
              [witan.ui.data :as data]
              [witan.ui.util :refer [prependtial]]
              ))

(defonce comms (atom {}))
(defonce views (atom {:projection nil
                      :dashboard nil
                      :new-projection nil
                      :menu nil}))

(defn find-app-container
  []
  (. js/document (getElementById "witan-main")))

(defn install-om!
  [view params]
  ;; main view
  (om/root
   (prependtial (view) params)
   data/app-state
   {:target (find-app-container)
    :shared {:comms @comms}})
  ;; menu
  (om/root
   ((fn [] (:menu @views)))
   data/app-state
   {:target (. js/document (getElementById "witan-menu"))}))

;;;;;;;;;;;;;

(secretary/set-config! :prefix "#")

(defroute dashboard
  "/"
  {:as params}
  (install-om! (fn [] (:dashboard @views)) params))

(defroute projection-wizard
  "/projection/:id/*action"
  {:as params}
  (install-om! (fn [] (:projection @views)) params))

(defroute new-projection
  "/new-projection/"
  {:as params}
  (install-om! (fn [] (:new-projection @views)) params))
