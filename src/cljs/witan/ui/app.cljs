(ns ^:figwheel-always witan.ui.app
  (:require [om.core :as om]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as sablono]
            [witan.ui.data :as data]
            [witan.ui.components.login :as login]
            [witan.ui.components.menu :as menu]
            [witan.ui.components.forecast :as forecast]))

(defonce comms (atom {}))
(defonce views (atom {}))

(defn find-app-container
  []
  (.getElementById js/document "witan-app"))

(defn is-logged-in?
  []
  (-> @data/app-state :login-state :is-logged-in?))

(defcomponent app-view
  "Switches between the various app views, and shows the menu."
  [cursor owner]
  (render [this]
    (sablono/html
      [:div#container
       (om/build menu/view cursor)
       [:div#witan-main
        ;; look up the appropriate view
        (om/build ((-> cursor :view-state :current-view) @views) cursor)
        ]])))

(defcomponent login-view
  [cursor owner]
  (render [this]
    (sablono/html
      [:div
       [:div.login-bg]
       [:div#content-container
        [:div#relative-container
         [:div.login-title.trans-bg
          [:h1 "Witan"]
          [:h2 "Make more sense of your city"]]
         [:div#witan-login.trans-bg
          (om/build login/login-state-view (:login-state cursor))]]]])))

(defcomponent master-view
  "This view shows either the login view, or the app view as appropriate."
  [cursor owner]
  (render [this]
    (if (is-logged-in?)
      ;; main view
      (om/build app-view cursor)
      ;; login screen
      (om/build login-view cursor))))

(defn install-app!
  "Builds the app UI. This is called once when the app is initialised. Om handles all updating of the UI
  after that."
  []
  (om/root
    master-view
    data/app-state
    {:target (find-app-container)
     :shared {:comms @comms}}))