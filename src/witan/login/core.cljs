(ns witan.login.core
  (:require[om.core :as om :include-macros true]
           [om-tools.core :refer-macros [defcomponentmethod]]
           [sablono.core :as html :refer-macros [html]]))

(defonce app-state (atom {:state :prompt}))
(def ls ;;login-strings
  {:sign-in               "Sign In"
   :email                 "Email"
   :password              "Password"
   :forgotten-question    "forgotten your password?"
   :forgotten-password    "Forgotten Password"
   :forgotten-instruction "Please enter your email address. If it matches one in our system we'll send you reset instructions."
   :reset-submitted       "Thanks. Your password reset request has been received."
   :reset-password        "Reset Password"
   :back                  "Back"
   :thanks                "Thanks"})

(enable-console-print!)

(defn goto-state
  "Moves the login screen to a different state"
  [cursor new-state]
  (om/update! cursor :state new-state))

(defmulti login-state-view
  "Multimedia for the different login screen states"
  (fn [cursor owner] (:state cursor)))

(defcomponentmethod
  login-state-view
  :prompt
  [cursor owner]
  (render [_]
          (html
           [:div
            [:h3 (:sign-in ls)]
            [:form {:class "pure-form pure-form-stacked" :action "dashboard.html"}
             [:input {:tab-index 1
                      :ref "email"
                      :type "email"
                      :placeholder (:email ls)
                      :required :required}]
             [:input {:tab-index 2
                      :ref "password"
                      :type "password"
                      :placeholder (:password ls)
                      :require :required}]
             [:button {:tab-index 3
                       :type "submit"
                       :class "pure-button pure-button-primary"} (:sign-in ls)]
             [:a {:id "forgotten-link"
                  :on-click #(goto-state cursor :reset)} (str "(" (:forgotten-question ls) ")")]]])))

(defcomponentmethod
  login-state-view
  :reset
  [cursor owner]
  (render [_]
          (html
           [:div {:class "forgotten-div"}
            [:h3 (:forgotten-password ls)]
            [:p
             [:span {:id "reset-instructions"} (:forgotten-instruction ls)]]
            [:form {:class "pure-form"
                    :on-submit (fn [e]
                                 (set! (.-innerText (. js/document (getElementById "reset-instructions"))) (:reset-submitted ls))
                                 (set! (.-innerText (. js/document (getElementById "reset-button"))) (:thanks ls))
                                 (set! (.-disabled (. js/document (getElementById "reset-button"))) true)
                                 (set! (.-disabled (. js/document (getElementById "reset-input"))) true)
                                 (.preventDefault e))}
             [:input {:tab-index 1
                      :id "reset-input"
                      :type "email"
                      :placeholder (:email ls)
                      :required :required}]
             [:div
              [:button {:tab-index 2
                        :id "reset-button"
                        :class "pure-button pure-button-primary"} (:reset-password ls)]
              [:button {:id "back-button"
                        :class "pure-button"
                        :on-click (fn [e]
                                    (goto-state cursor :prompt))} (:back ls)]]]])))

(om/root
 login-state-view
 app-state
 {:target (. js/document (getElementById "witan-login"))})
