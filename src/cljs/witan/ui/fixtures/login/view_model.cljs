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
  (.open js/window
         (str
          "mailto:witan@mastodonc.com?subject=[Witan Password Reset Request]"
          "&body=Please reset the password for the following email address: "
          email) "resetEmailWindow", "height=400,width=600,left=10,top=10"))

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
  (om/update! cursor :email email)
  (venue/request! {:owner owner
                   :service :service/api
                   :request :login
                   :args [email pass]
                   :context cursor}))

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
