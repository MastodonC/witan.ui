(ns ^:figwheel-always witan.ui.nav
  (:require [cljs.core.async :as async :refer [>! <! alts! chan close!]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [om.core :as om :include-macros true]
            [witan.ui.data :as data]))

(defonce comms (atom {}))
(defonce views (atom {:forecast nil
                      :dashboard nil
                      :new-forecast nil
                      :menu nil
                      :share nil}))

(defn find-app-container
  []
  (. js/document (getElementById "witan-main")))

(defn install-om!
  [view params]
  ;; main view
  (om/root
   (view)
   data/app-state
   {:target (find-app-container)
    :shared {:comms @comms}
    :opts params})
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

(defroute forecast-wizard
  "/forecast/:id/*action"
  {:as params}
  (install-om! (fn [] (:forecast @views)) params))

(defroute new-forecast
  "/new-forecast/"
  {:as params}
  (install-om! (fn [] (:new-forecast @views)) params))

(defroute share
  "/share/:id/"
  {:as params}
  (install-om! (fn [] (:share @views)) params))
