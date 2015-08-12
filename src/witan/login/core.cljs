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
   :reset-password        "Reset Password"
   :back                  "Back"})

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
            [:form {:class "pure-form"}
             [:input {:type "email"
                      :placeholder (:email ls)}]
             [:input {:type "password"
                      :placeholder (:password ls)}]
             [:button {:type "submit"
                       :class "pure-button pure-button-primary"} (:sign-in ls)]
             [:a {:id "forgotten-link"
                  :on-click #(goto-state cursor :reset)} (str "(" (:forgotten-question ls) ")")]]])))

(defcomponentmethod
  login-state-view
  :reset
  [cursor owner]
  (render [_]
          (html
           [:div {:id "forgotten-div"}
            [:h3 (:forgotten-password ls)]
            [:p
             [:span (:forgotten-instruction ls)]]
            [:form {:class "pure-form"}
             [:input {:ref "reset-email"
                      :type "email"
                      :placeholder (:email ls)}]
             [:div
              [:button {:class "pure-button pure-button-primary"
                        :on-click (fn [_] false)} (:reset-password ls)]
              [:button {:id "back-button"
                        :class "pure-button"
                        :on-click (fn [_] (goto-state cursor :prompt) false)} (:back ls)]]]])))
(om/root
 login-state-view
 app-state
 {:target (. js/document (getElementById "witan-login"))})
