(ns witan.ui.core
  (:require [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [accountant.core :as accountant]
            ;;
            [witan.ui.data      :as data]
            [witan.ui.side      :as side]
            [witan.ui.app       :as app]))

(if-let [node (gdom/getElement "app")]
  (do
    (accountant/configure-navigation! {:nav-handler app/dispatch-path!
                                       :path-exists? app/path-exists?})
    (om/add-root! (data/make-reconciler) app/Main node)))

(if-let [node (gdom/getElement "side")]
  (om/add-root! (data/make-reconciler) side/Main node))
