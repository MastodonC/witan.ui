(ns witan.ui.components.primary.viz
  (:require [reagent.core :as r]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.data :as data]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [sablono.core :as sab :include-macros true]
            [clojure.string :as str]
            cljsjs.dialog-polyfill)
  (:require-macros [devcards.core :as dc :refer [defcard]]
                   [cljs-log.core :as log]))

(def id "viz")
(defonce ready? (r/atom false))
(defonce pymo (atom nil))
(defonce last-location (atom nil))

(defn view
  []
  (r/create-class
   {:component-will-unmount
    (fn [this])
    :component-did-mount
    (fn [this])
    :reagent-render
    (fn []
      (let [{:keys [workspace/current-viz]} (data/get-app-state :app/workspace)
            {:keys [result/location]} current-viz]
        (log/debug "RENDER INNER" location (not= @last-location location) @pymo)
        (when (not= @last-location location)
          (reset! last-location location)
          (log/info "Loading viz:" location)
          (if (not @pymo)
            (do
              (reset! pymo (.Parent js/pym id (str "http://localhost:3448/?data=" location "&style=table") #js {}))
              (.onMessage @pymo "ready" (fn [_]
                                          (log/debug "Viz is ready")
                                          (reset! ready? true))))
            (do
              (reset! ready? false)
              (.sendMessage @pymo "dataLocation" location))))
        [:div#viz-container
         (if-not location
           [:div#viz-placeholder.text-center
            (icons/pie-chart :large :dark)
            [:h2 (get-string :string/no-viz-selected)]
            [:h3 (get-string :string/no-viz-selected-desc)]]
           [:div#loading
            {:style {:background-color "transparent"
                     :display (if @ready? "none" "inherit")}}
            (icons/loading :large)])
         [:div {:id id
                :style {:display (if @ready? "inherit" "none")}}]]))}))
