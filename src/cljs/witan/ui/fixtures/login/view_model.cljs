(ns ^:figwheel-always witan.ui.fixtures.login.view-model
    (:require [om.core :as om :include-macros true]
              [venue.core :as venue]
              [witan.ui.services.data :as data]
              [witan.ui.strings :as s])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn on-activate
  [owner args cursor]
  (om/update! cursor :logged-in? (data/logged-in?)))

(wm/create-standard-view-model! {:on-activate on-activate})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod event-handler
  :event/reset-password
  [owner _ email cursor]
  #_(.open js/window
           (str
            "mailto:witan@mastodonc.com?subject=[Witan Password Reset Request]"
            "&body=Please reset the password for the following email address: "
            email) "resetEmailWindow" "height=400,width=600,left=10,top=10"))

(defmethod event-handler
  :event/show-password-reset
  [owner _ show cursor]
  (if show
    (om/update! cursor :phase :reset)
    (om/update! cursor :phase :prompt)))

(defmethod event-handler
  :event/attempt-login
  [owner _ {:keys [email pass]} cursor]
  (om/update! cursor :phase :waiting)
  (om/update! cursor :waiting-msg :signing-in)
  (om/update! cursor :email email)
  (venue/request! {:owner owner
                   :service :service/api
                   :request :login
                   :args [email pass]
                   :context cursor}))

(defmethod event-handler
  :event/goto-sign-up
  [owner _ _ cursor]
  (om/update! cursor :phase :sign-up))

(defmethod event-handler
  :event/attempt-sign-up
  [owner _ {:keys [email password] :as args} cursor]
  (let [[email confirm-email]       email
        [password confirm-password] password]
    (if-not (= email confirm-email)
      (om/update! cursor :message (s/get-string :email-no-match))
      (if-not (= password confirm-password)
        (om/update! cursor :message (s/get-string :password-no-match))
        (if-not (>= (count password) 8)
          (om/update! cursor :message (s/get-string :password-under-length))
          (do
            (om/update! cursor :phase :waiting)
            (om/update! cursor :waiting-msg :processing-account)
            (venue/request! {:owner owner
                             :service :service/api
                             :request :sign-up
                             :args (merge {:username email :password password}
                                          (select-keys args [:invite-token :name]))
                             :context cursor})))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmethod response-handler
  [:login :success]
  [owner _ response cursor]
  (om/update! cursor :phase :prompt)
  (if response
    (do
      (om/update! cursor :message nil)
      (om/update! cursor :logged-in? true))
    (om/update! cursor :message (s/get-string :sign-in-failure))))

(defmethod response-handler
  [:login :failure]
  [owner _ response cursor]
  (om/update! cursor :message (s/get-string :api-failure))
  (om/update! cursor :phase :prompt))

(defmethod response-handler
  [:sign-up :failure]
  [owner _ response cursor]
  (om/update! cursor :message (s/get-string :sign-up-failure))
  (om/update! cursor :phase :sign-up))

(defmethod response-handler
  [:sign-up :success]
  [owner _ response cursor]
  (om/update! cursor :phase :signed-up))
