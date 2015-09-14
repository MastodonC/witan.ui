(ns ^:figwheel-always witan.ui.nav
  (:require [cljs.core.async :as async :refer [>! <! alts! chan close!]]
            [secretary.core :as secretary :refer-macros [defroute]]
            [om.core :as om :include-macros true]
            [witan.ui.data :as data]))

(secretary/set-config! :prefix "#")

(defn- update-app-state [ks v] (swap! data/app-state assoc-in ks v))

(defroute dashboard
          "/"
          {:as params}
          (swap! data/app-state assoc-in [:view-state :current-view] :dashboard))

(defroute forecast-wizard
          "/forecast/:id/*action"
          {:as params}
          (update-app-state [:view-state :forecast-wizard :action] (:action params))
          (update-app-state [:view-state :forecast-wizard :id] (:id params))
          (update-app-state [:view-state :current-view] :forecast))

(defroute new-forecast
          "/new-forecast/"
          {:as params}
          (update-app-state [:view-state :current-view] :new-forecast))

(defroute share
          "/share/:id/"
          {:as params}
          (update-app-state [:view-state :share :id] (:id params))
          (update-app-state [:view-state :current-view] :share))