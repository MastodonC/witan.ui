(ns witan.ui.components.primary.topology
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.data :as data]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [sablono.core :as sab :include-macros true]
            [clojure.string :as str]
            cljsjs.dialog-polyfill)
  (:require-macros [devcards.core :as dc :refer [defcard]]
                   [cljs-log.core :as log]))

;; (defn get-dialog [name]
;;   (.querySelector js/document (str "#" name)))

;; (defn show-dialog [name]
;;   (when-let [dialog (get-dialog name)]
;;     (.showModal dialog)
;;     dialog))

;; (defn close-dialog [name]
;;   (when-let [dialog (get-dialog name)]
;;     (.close dialog)))

;; (defn add-model-dialog [model-list]
;;   [:dialog
;;    {:id "add-model-dialog"}
;;    [:div.modal-container
;;     [:div.modal-close-button
;;      (vec (interpose {:on-click #(close-dialog "add-model-dialog")} (icons/close :medium :dark)))]
;;     (if model-list
;;       [:div
;;        [:h2 (get-string :string/select-a-model)]
;;        [:ul
;;         (for [{:keys [witan/name
;;                       witan/version
;;                       witan/description]} model-list]
;;           ^{:key (str name version)}
;;           [:li [:span (str name)]])]]
;;       [:div#loading-modal (icons/loading :x-large)])]])

;; (defn show-add-model-dialog
;;   [e]
;;   (controller/raise! :workspace/fetch-models)
;;   (show-dialog "add-model-dialog"))

(defn model-name
  [f v]
  (str (->> (str/split (name f) #"-")
            (map #(mapv char %))
            (map #(update % 0 str/upper-case))
            (map (partial apply str))
            (str/join " ")) " - v" v))

(defn add-model-widget
  [model-list]
  [:div.add-model-widget
   (if model-list
     (for [{:keys [witan/name
                   witan/version
                   witan/description] :as model} model-list]
       ^{:key (str name version)}
       [:div.model-option
        (let [model-fn #(controller/raise!
                         :workspace/select-model {:name name
                                                  :version version})]
          (shared/button {:icon icons/grain
                          :txt (model-name name version)}
                         model-fn))])
     [:div#loading-modal (icons/loading :x-large)])])

(defn view
  [wsp]
  (controller/raise! :workspace/fetch-models)
  (fn [{:keys [workspace/current workspace/model-list workspace/running?]}]
    (let [{:keys [workspace/workflow]} current]
      [:div#topology
       #_(add-model-dialog model-list)
       [:div#right-bar
        [:div.buttons
         [:button.pure-button
          {:class (->> (vector
                        (when (or running? (not workflow)) "pure-button-disabled")
                        (when running?                     "running"))
                       (keep identity)
                       (clojure.string/join " "))
           :on-click #(when-not running? (controller/raise! :workspace/run-current))}
          (if running?
            (icons/loading :small :dark)
            (icons/cog :small :dark))
          (if running?
            (get-string :string/running)
            (get-string :string/run))]]]
       (if-not workflow
         [:div#content.text-center
          (icons/clipboard :large :dark)
          [:h2 (get-string :string/workspace-empty)]
          [:hr
           {:style {:margin "0% 20%"}}]
          [:h3 (get-string :string/workspace-select-a-model)]
          (add-model-widget (map :metadata model-list))]
         [:div#content.text-center
          (for [edge workflow]
            ^{:key (str edge)}
            [:div (str edge)])])])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Devcards

#_(defcard add-model-dialog-card
    (sab/html
     [:div
      (add-model-dialog nil)
      [:button
       {:on-click show-add-model-dialog}
       "Add Model Dialog"]]))
