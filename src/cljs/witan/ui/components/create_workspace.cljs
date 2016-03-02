(ns witan.ui.components.create-workspace
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller])
  (:require-macros [cljs-log.core :as log]))

(defui Main
  static om/IQuery
  (query [this]
         [{:user/groups [:group/name :group/id]}])
  Object
  (render [this]
          (let [models (:models/list (om/props this))]
            (sab/html [:div#create-workspace
                       (shared/header :string/create-workspace-title :string/create-workspace-subtitle)
                       [:div#content
                        [:form.pure-form
                         {:on-submit (fn [e]
                                       (controller/raise! this :workspace/create {:name (.-value (. js/document (getElementById "new-workspace-name")))
                                                                                  :desc (.-value (. js/document (getElementById "new-workspace-desc")))})
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
                          [:button.pure-button.button-success
                           {:type "submit"
                            :key "button"}
                           (icons/plus) (get-string :string/create)]]]
                        ]]))))
