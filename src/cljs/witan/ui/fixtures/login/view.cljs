(ns ^:figwheel-always witan.ui.fixtures.login.view
    (:require [om.core :as om :include-macros true]
              [om-tools.core :refer-macros [defcomponentmethod defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [witan.ui.strings :refer [get-string]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defmulti login-state-view
  "Multimedia for the different login screen states"
  (fn [cursor owner] (:phase cursor)))

(defcomponentmethod
  login-state-view
  :waiting
  [cursor owner]
  (render [_]
          (html [:div
                 [:h3 (get-string (:waiting-msg cursor))]
                 [:div#loading
                  [:i.fa.fa-refresh.fa-2x.fa-spin]]])))

(defcomponentmethod
  login-state-view
  :sign-up
  [cursor owner]
  (render [_]
          (html [:div
                 [:h3 (get-string :create-account)]
                 [:span#error-message (:message cursor)]
                 [:form {:class "pure-form pure-form-stacked"
                         :on-submit (fn [e]
                                      (venue/raise! owner :event/attempt-sign-up {:email [(.-value (om/get-node owner "email"))
                                                                                          (.-value (om/get-node owner "confirm-email"))]
                                                                                  :password [(.-value (om/get-node owner "password"))
                                                                                             (.-value (om/get-node owner "confirm-password"))]
                                                                                  :invite-token (.-value (om/get-node owner "token"))
                                                                                  :name (.-value (om/get-node owner "name"))})
                                      (.preventDefault e))}
                  [:input {:tab-index 1
                           :ref "token"
                           :type "text"
                           :id "token"
                           :placeholder (get-string :sign-up-token)
                           :required :required}]
                  [:input {:tab-index 2
                           :ref "name"
                           :type "text"
                           :id "name"
                           :placeholder (get-string :name)
                           :required :required}]
                  [:input {:tab-index 3
                           :ref "email"
                           :type "email"
                           :id "login-email"
                           :placeholder (get-string :email)
                           :required :required}]
                  [:input {:tab-index 4
                           :ref "confirm-email"
                           :type "email"
                           :id "confirm-email"
                           :placeholder (get-string :confirm-email)
                           :required :required}]
                  [:input {:tab-index 5
                           :ref "password"
                           :type "password"
                           :id "password"
                           :placeholder (get-string :password)
                           :required :required}]
                  [:input {:tab-index 6
                           :ref "confirm-password"
                           :type "password"
                           :id "confirm-password"
                           :placeholder (get-string :confirm-password)
                           :require :required}]
                  [:button {:tab-index 7
                            :type "submit"
                            :class "pure-button pure-button-primary"} (get-string :create-account)]]])))

(defcomponentmethod
  login-state-view
  :prompt
  [cursor owner]
  (did-mount [_]
             (when-let [node (.getElementById js/document "login-email")]
               (set! (.-value node) (:email @cursor))))
  (render [_]
          (html
           [:div
            [:h3 (get-string :sign-in)]
            [:span#error-message (:message cursor)]
            [:form {:class "pure-form pure-form-stacked"
                    :on-submit (fn [e]
                                 (venue/raise! owner :event/attempt-login {:email (.-value (om/get-node owner "email"))
                                                                           :pass (.-value (om/get-node owner "password"))})
                                 (.preventDefault e))}
             [:input {:tab-index 1
                      :ref "email"
                      :type "email"
                      :id "login-email"
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
                              (venue/raise! owner :event/show-password-reset true)
                              (.preventDefault e))} (str "(" (get-string :forgotten-question) ")")]]
            [:h3 (get-string :create-account-header)]
            [:p
             [:small.text-white (get-string :create-account-info)]]
            [:button.pure-button.pure-button-success
             {:on-click (fn [e]
                          (venue/raise! owner :event/goto-sign-up)
                          (.preventDefault e))} (get-string :create-account)]])))

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
                                 (comment (set! (.-innerText (. js/document (getElementById "reset-instructions"))) (get-string :reset-submitted))
                                          (set! (.-innerText (. js/document (getElementById "reset-button"))) (get-string :thanks))
                                          (set! (.-disabled (. js/document (getElementById "reset-button"))) true)
                                          (set! (.-disabled (. js/document (getElementById "reset-input"))) true))
                                 (.open js/window
                                        (str
                                         "mailto:witan@mastodonc.com?subject=[Witan Password Reset Request]"
                                         "&body=Please reset the password for the following email address: "
                                         (.-value (om/get-node owner "reset-email"))) "resetEmailWindow" "height=400,width=600,left=10,top=10")
                                 #_(venue/raise! owner :event/reset-password (.-value (om/get-node owner "reset-email")))
                                 (.preventDefault e))}
             [:input {:tab-index 1
                      :ref "reset-email"
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
                                    (venue/raise! owner :event/show-password-reset false)
                                    (.preventDefault e))} (get-string :back)]]]])))

(defcomponent view
  [cursor owner]
  (render [this]
          (html
           (if-not (:logged-in? cursor)
             [:div
              [:div#login-bg {:key "login-bg"}
               [:span#bg-attribution.trans-bg
                 "Photo by "
                [:a {:href "https://www.flickr.com/photos/fico86/" :target "_blank" :key "photo-attr1"}
                 "Binayak Dasgupta"] " - "
                [:a {:href "https://creativecommons.org/licenses/by/2.0/" :target "_blank" :key "photo-attr2"} "CC BY 2.0"]]]
              [:div#content-container {:key "login-content"}
               [:div#relative-container
                [:div.login-title.trans-bg {:key "login-title"}
                 [:h1 {:key "login-title-main"} (get-string :witan) ]
                 [:h2 {:key "login-title-sub"} (get-string :witan-tagline)]]
                [:div#witan-login.trans-bg {:key "login-state"}
                 (om/build login-state-view cursor)]]]]))))
