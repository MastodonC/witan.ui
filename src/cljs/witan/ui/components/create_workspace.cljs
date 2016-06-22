(ns witan.ui.components.create-workspace
  (:require [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller])
  (:require-macros [cljs-log.core :as log]))

(defn view
  [this]
  (let [message (get-in  this [:app/create-workspace :cw/message])
        pending? (get-in this [:app/create-workspace :cw/pending?])]
    [:div#create-workspace
     (shared/header :string/create-workspace-title :string/create-workspace-subtitle)
     [:div#content
      [:form.pure-form
       {:on-submit (fn [e]
                     (when-not pending?
                       (controller/raise! :workspace/create {:name (.-value (. js/document (getElementById "new-workspace-name")))
                                                             :desc (.-value (. js/document (getElementById "new-workspace-desc")))}))
                     (.preventDefault e))}
       [:fieldset
        [:div
         {:key "name"}
         [:h2 {:key "title"}
          (get-string :string/create-workspace-name)]
         [:input.pure-input-1 {:type "text"
                               :id "new-workspace-name"
                               :key "name"
                               :required true
                               :placeholder (get-string :string/create-workspace-name-ph)}]]
        [:div
         {:key "description"}
         [:h2 {:key "title"}
          (get-string :string/create-workspace-desc)
          [:em (get-string :string/optional)]]
         [:textarea.pure-input-1 {:type "text"
                                  :id "new-workspace-desc"
                                  :key "desc"
                                  :placeholder (get-string :string/create-workspace-desc-ph)}]]
        [:hr]
        (when message
          [:h3.icon-and-text.error (icons/error) (get-string message)])
        (when-not pending?
          [:button.pure-button.button-success
           {:type "submit"
            :key "button"}
           (icons/plus) (get-string :string/create)])
        (when pending?
          (icons/loading))]]]]))
