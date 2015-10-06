(ns ^:figwheel-always witan.ui.fixtures.menu.view-model
    (:require [om.core :as om :include-macros true]
              [venue.core :as venue]
              [witan.ui.util :as util])
    (:require-macros [cljs-log.core :as log]
                     [witan.ui.macros :as wm]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(wm/create-standard-view-model!)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-user! [owner cursor]
       (venue/request! {:owner owner
                      :service :service/data
                      :request :fetch-user
                        :context cursor}))

(defn on-initialise
  [owner cursor]
  (util/inline-subscribe!
  :api/user-logged-in
  #(fetch-user! owner cursor)))

(defn on-activate
  [owner args cursor])

(defmethod response-handler
  [:fetch-user :success]
  [owner _ user cursor]
  (om/update! cursor :user (first user)))

(defmethod response-handler
  [:fetch-user :failure]
  [owner _ _ cursor]
  (log/debug "fetch-user failure"))
