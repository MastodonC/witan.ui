(ns ^:figwheel-always witan.ui.core
    (:require [om.core :as om :include-macros true]
              [venue.core :as venue :include-macros true]
              ;;
              [witan.ui.services.api]
              [witan.ui.services.data]
              ;;
              [witan.ui.fixtures.login.view]
              [witan.ui.fixtures.login.view-model]
              [witan.ui.fixtures.dashboard.view]
              [witan.ui.fixtures.dashboard.view-model]
              [witan.ui.fixtures.forecast.view]
              [witan.ui.fixtures.forecast.view-model]
              [witan.ui.fixtures.new-forecast.view]
              [witan.ui.fixtures.new-forecast.view-model]
              [witan.ui.fixtures.share.view]
              [witan.ui.fixtures.share.view-model]
              [witan.ui.fixtures.menu.view]
              [witan.ui.fixtures.menu.view-model])
    (:require-macros [cljs-log.core :as log]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(comment (defonce load-test-data
           (data/load-dummy-data!)))

(venue/defview!
  {:target "app"
   :route "/"
   :id :views/dashboard
   :view witan.ui.fixtures.dashboard.view/view
   :view-model witan.ui.fixtures.dashboard.view-model/view-model
   :state {:expanded      #{}
           :selected       []
           :has-ancestors #{}
           :filter         nil
           :refreshing?    false
           :forecasts      []}})

(venue/defview!
  {:target "app"
   :route "/forecast/:id/*action"
   :id :views/forecast
   :view witan.ui.fixtures.forecast.view/view
   :view-model witan.ui.fixtures.forecast.view-model/view-model
   :state {}})

(venue/defview!
  {:target "app"
   :route "/new-forecast"
   :id :views/new-forecast
   :view witan.ui.fixtures.new-forecast.view/view
   :view-model witan.ui.fixtures.new-forecast.view-model/view-model
   :state {}})

(venue/defview!
  {:target "app"
   :route "/share/:id"
   :id :views/share
   :view witan.ui.fixtures.share.view/view
   :view-model witan.ui.fixtures.share.view-model/view-model
   :state {}})

;;;;;;;;;;;;;;

(venue/defstatic!
  {:target "login"
   :id :views/login
   :view witan.ui.fixtures.login.view/view
   :view-model witan.ui.fixtures.login.view-model/view-model
   :state {:phase :prompt
           :message nil
           :logged-in? false
           :email nil}})

(venue/defstatic!
  {:target "menu"
   :id :statics/menu
   :view witan.ui.fixtures.menu.view/view
   :view-model witan.ui.fixtures.menu.view-model/view-model
   :state {}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(venue/defservice!
  {:id :service/api
   :handler witan.ui.services.api/service})

(venue/defservice!
  {:id :service/data
   :handler witan.ui.services.data/service})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(venue/start!)

(defn on-js-reload [] (venue/on-js-reload))
