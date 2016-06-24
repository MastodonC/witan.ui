(ns witan.ui.core
  (:require [goog.dom :as gdom]
            [reagent.core :as r]
            [accountant.core :as accountant]
            ;;
            [witan.ui.components.login :as login]
            [witan.ui.components.side  :as side]
            [witan.ui.components.app   :as app]
            [witan.ui.data             :as data]
            [witan.ui.route            :as route])
  (:require-macros [cljs-log.core :as log]))

(defn path
  []
  (.. js/document -location -pathname))

(when-let [node (gdom/getElement "app")]
  (defonce init
    (do
      (data/load-data!)
      (accountant/configure-navigation! {:nav-handler route/dispatch-path!
                                         :path-exists? route/path-exists?})
      (route/dispatch-path! (path))))
  (r/render [app/root-view] node))

(when-let [node (gdom/getElement "side")]
  (r/render [side/root-view] node))

(when-let [node (gdom/getElement "login")]
  (r/render [login/root-view] node))

;;

(defn on-js-reload
  []
  #_(.forceUpdate (-> login-reconciler :state @deref :root))
  #_(.forceUpdate (-> side-reconciler :state @deref :root))
  #_(.forceUpdate (-> app-reconciler :state @deref :root)))
