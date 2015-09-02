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

(defn restart-app []
  ;; restarts the by detaching the root and re-firing the secretary route
  (om/detach-root (find-app-container))
  (secretary/dispatch! (:current-route @data/app-state)))

(defn is-logged-in?
  []
  (-> @data/app-state :login-state :is-logged-in?))

(defn install-om!
  [view params]
  (if (is-logged-in?)
    (do
      ;; remove witan-login-screen
      (if-let [login-screen (. js/document (getElementById "witan-login-screen"))]
        (.removeChild (.-parentNode login-screen) login-screen))

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
       {:target (. js/document (getElementById "witan-menu"))
        :shared {:comms @comms}}))

    ;; redir to login screen
    (om/root
     ((fn [] (:login @views)))
     data/app-state
     {:target (. js/document (getElementById "witan-login"))
      :shared {:comms @comms}
      :path [:login-state]})
    ))

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
