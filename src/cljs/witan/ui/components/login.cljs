(ns witan.ui.components.login
  (:require [om.core :as om :include-macros true]
            [om-tools.core :refer-macros [defcomponentmethod defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [witan.ui.async :refer [raise!]]
            [witan.ui.data :refer [get-string]]))

(defmulti login-state-view
  "Multimedia for the different login screen states"
  (fn [cursor owner] (:phase cursor)))

(defcomponentmethod
  login-state-view
  :waiting
  [cursor owner]
  (render [_]
          (html [:div
                 [:h3 (get-string :signing-in)]
                 [:div#loading
                       [:i.fa.fa-refresh.fa-2x.fa-spin]]])))

(defcomponentmethod
  login-state-view
  :prompt
  [cursor owner]
  (render [_]
          (html
           [:div
            [:h3 (get-string :sign-in)]
            [:span#error-message (:message cursor)]
            [:form {:class "pure-form pure-form-stacked"
                    :on-submit (fn [e]
                                 (raise! owner :event/attempt-login {:email (.-value (om/get-node owner "email"))
                                                                       :pass (.-value (om/get-node owner "password"))})
                                 (.preventDefault e))}
             [:input {:tab-index 1
                      :ref "email"
                      :type "email"
                      :placeholder (get-string :email)
                      :required :required}]
             [:input {:tab-index 2
                      :ref "password"
                      :type "password"
                      :placeholder (get-string :password)
                      :require :required}]
             [:button {:tab-index 3
                       :type "submit"
                       :class "pure-button pure-button-primary"} (get-string :sign-in)]
             [:a {:id "forgotten-link"
                  :on-click (fn [e]
                              (raise! owner :event/show-password-reset true)
                              (.preventDefault e))} (str "(" (get-string :forgotten-question) ")")]]])))

(defcomponentmethod
  login-state-view
  :reset
  [cursor owner]
  (render [_]
          (html
           [:div {:class "forgotten-div"}
            [:h3 (get-string :forgotten-password)]
            [:p
             [:span {:id "reset-instructions"} (get-string :forgotten-instruction)]]
            [:form {:class "pure-form"
                    :on-submit (fn [e]
                                 (set! (.-innerText (. js/document (getElementById "reset-instructions"))) (get-string :reset-submitted))
                                 (set! (.-innerText (. js/document (getElementById "reset-button"))) (get-string :thanks))
                                 (set! (.-disabled (. js/document (getElementById "reset-button"))) true)
                                 (set! (.-disabled (. js/document (getElementById "reset-input"))) true)
                                 (.preventDefault e))}
             [:input {:tab-index 1
                      :id "reset-input"
                      :type "email"
                      :placeholder (get-string :email)
                      :required :required}]
             [:div
              [:button {:tab-index 2
                        :id "reset-button"
                        :class "pure-button pure-button-primary"} (get-string :reset-password)]
              [:button {:id "back-button"
                        :class "pure-button"
                        :on-click (fn [e]
                                    (raise! owner :event/show-password-reset false)
                                    (.preventDefault e))} (get-string :back)]]]])))

(defcomponent
  view
  [cursor owner args]
  (render [_]
          (om/build login-state-view cursor)))
