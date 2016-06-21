(ns witan.ui.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [accountant.core :as accountant]
            ;;
            [witan.ui.components.login :as login]
            [witan.ui.components.side  :as side]
            [witan.ui.components.app   :as app]
            [witan.ui.data             :as data]
            [witan.ui.route            :as route])
  (:require-macros [cljs-log.core :as log]))

(defonce app-reconciler   (data/make-reconciler))
(defonce side-reconciler  (data/make-reconciler))
(defonce login-reconciler (data/make-reconciler))

(defonce init
  (do
    (if-let [node (gdom/getElement "app")]
      (do
        (accountant/configure-navigation! {:nav-handler route/dispatch-path!
                                           :path-exists? route/path-exists?})
        (route/dispatch-path! (app/path))
        (om/add-root! app-reconciler app/Main node)))

    (if-let [node (gdom/getElement "side")]
      (om/add-root! side-reconciler side/Main node))

    (if-let [node (gdom/getElement "login")]
      (om/add-root! login-reconciler login/Main node))))
;;

(defn on-js-reload
  []
  (.forceUpdate (-> login-reconciler :state @deref :root))
  (.forceUpdate (-> side-reconciler :state @deref :root))
  (.forceUpdate (-> app-reconciler :state @deref :root))
  (om/force-root-render! login-reconciler)
  (om/force-root-render! side-reconciler)
  (om/force-root-render! app-reconciler))
