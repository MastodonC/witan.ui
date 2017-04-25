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
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(when-let [node (gdom/getElement "app")]
  (defonce init
    (do
      (when (cljs-env :environment)
        (.log js/console
              (str "Witan For Cities\n"
                   "ENV:  " (or (cljs-env :environment) "development") "\n"
                   "SHA:  " (or (cljs-env :build-sha) "?") "\n"
                   "DATE: " (or (cljs-env :build-dt)    "?"))))

      ;; data
      (data/load-data!)

      ;; routing
      (set! accountant/history.transformer_
            (let [transformer (goog.history.Html5History.TokenTransformer.)]
              (set! (.. transformer -retrieveToken)
                    (fn [path-prefix location]
                      (str (.-pathname location) (.-search location))))
              (set! (.. transformer -createUrl)
                    (fn [token path-prefix location]
                      (str path-prefix token)))
              transformer))
      (accountant/configure-navigation! {:nav-handler route/dispatch-path!
                                         :path-exists? route/path-exists?})
      (.setUseFragment accountant/history true)
      (route/dispatch-path! (route/path))))

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
