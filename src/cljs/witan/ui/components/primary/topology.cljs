(ns witan.ui.components.primary.topology
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [sablono.core :as sab :include-macros true]
            cljsjs.dialog-polyfill)
  (:require-macros
   [devcards.core :as dc :refer [defcard]]))

(defn get-dialog [name]
  (.querySelector js/document (str "#" name)))

(defn show-dialog [name]
  (when-let [dialog (get-dialog name)]
    (.showModal dialog)
    dialog))

(defn close-dialog [name]
  (when-let [dialog (get-dialog name)]
    (.close dialog)))

(defn add-model-dialog []
  [:dialog
   {:id "add-model-dialog"}
   [:h1 "This is a modal dialog"]
   [:button {:on-click #(close-dialog "add-model-dialog")} "Ok"]])

(defn show-add-model-dialog
  [e]
  (show-dialog "add-model-dialog"))

(defn view
  [{:keys [workspace/name workspace/workflow]}]
  [:div#topology
   (add-model-dialog)
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
    (add-model-dialog)
    [:button
     {:on-click show-add-model-dialog}
     "Add Model Dialog"]]))
