(ns witan.ui.components.app
  (:require [reagent.core :as r]
            [sablono.core :as sab]
            ;;
            [witan.ui.components.dashboard.workspaces :as workspace-dash]
            [witan.ui.components.dashboard.data :as data-dash]
            [witan.ui.components.dashboard.rts :as rts-dash]
            [witan.ui.components.rts :as rts]
            [witan.ui.components.split :as split]
            [witan.ui.components.create-workspace :as create-ws]
            [witan.ui.components.create-rts :as create-rts]
            [witan.ui.components.create-data :as create-data]
            [witan.ui.components.create-datapack :as create-datapack]
            [witan.ui.components.data :as data-view]
            [witan.ui.components.activities :as activities]
            [witan.ui.components.debug :as debug]
            [witan.ui.components.about :as about]
            [witan.ui.components.panic :refer [panic-screen]]
            [witan.ui.utils :as utils]
            [witan.ui.data :as data]
            [witan.ui.route :as route])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as am :refer [go-loop]]))

(def route->component
  {:app/workspace-dash   workspace-dash/view
   :app/data-dash        data-dash/view
   :app/data-create      create-data/view
   :app/datapack-create  create-datapack/view
   :app/data             data-view/view
   :app/workspace        split/view
   :app/create-workspace create-ws/view
   :app/request-to-share rts-dash/view
   :app/rts-create       create-rts/view
   :app/rts              rts/view
   :app/activity         activities/view
   :app/debug            debug/view
   :app/about            about/view})

(defn root-view
  []
  (r/create-class
   {:reagent-render
    (fn []
      (let [panic-message (data/get-app-state :app/panic-message)
            {:keys [route/path]} (data/get-app-state :app/route)]
        (if-not panic-message
          [(get route->component path)]
          (panic-screen panic-message))))}))
