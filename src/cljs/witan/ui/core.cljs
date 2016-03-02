(ns witan.ui.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [accountant.core :as accountant]
            ;;
            [witan.ui.components.login :as login]
            [witan.ui.components.side  :as side]
            [witan.ui.components.app   :as app]
            [witan.ui.data             :as data]
            [witan.ui.route            :as route]))

(if-let [node (gdom/getElement "app")]
  (do
    (accountant/configure-navigation! {:nav-handler route/dispatch-path!
                                       :path-exists? route/path-exists?})
    (route/dispatch-path! (app/path))
    (om/add-root! (data/make-reconciler) app/Main node)))

(if-let [node (gdom/getElement "side")]
  (om/add-root! (data/make-reconciler) side/Main node))

(if-let [node (gdom/getElement "login")]
  (om/add-root! (data/make-reconciler) login/Main node))
