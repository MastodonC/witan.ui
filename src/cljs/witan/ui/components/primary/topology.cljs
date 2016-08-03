(ns witan.ui.components.primary.topology
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [sablono.core :as sab :include-macros true]
            cljsjs.dialog-polyfill)
  (:require-macros [devcards.core :as dc :refer [defcard]]
                   [cljs-log.core :as log]))

(defn get-dialog [name]
  (.querySelector js/document (str "#" name)))

(defn show-dialog [name]
  (when-let [dialog (get-dialog name)]
    (.showModal dialog)
    dialog))

(defn close-dialog [name]
  (when-let [dialog (get-dialog name)]
    (.close dialog)))

(defn add-model-dialog [model-list]
  [:dialog
   {:id "add-model-dialog"}
   [:div.modal-container
    [:div.modal-close-button
     (vec (interpose {:on-click #(close-dialog "add-model-dialog")} (icons/close :medium :dark)))]
    (if model-list
      [:div
       [:h2 (get-string :string/select-a-model)]
       [:ul
        (for [{:keys [witan/name
                      witan/version
                      witan/description]} model-list]
          ^{:key (str name version)}
          [:li [:span (str name)]])]]
      [:div#loading-modal (icons/loading :x-large)])]])

(defn show-add-model-dialog
  [e]
  (controller/raise! :workspace/fetch-models)
  (show-dialog "add-model-dialog"))

(defn view
  [{:keys [workspace/name workspace/workflow]} model-list]
  [:div#topology
   (add-model-dialog model-list)
   [:div#heading
    [:h1 name]]
   [:div#right-bar
    [:div.buttons
     [:button.pure-button
      {:on-click show-add-model-dialog}
      (icons/plus :small)
      (get-string :string/workspace-add-model)]]]
   (when-not workflow
     [:div.text-center
      (icons/clipboard :large :dark)
      [:h2 (get-string :string/workspace-empty)]
      [:h3 (get-string :string/workspace-empty-prompt)]])])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Devcards

(defcard add-model-dialog-card
  (sab/html
   [:div
    (add-model-dialog nil)
    [:button
     {:on-click show-add-model-dialog}
     "Add Model Dialog"]]))
