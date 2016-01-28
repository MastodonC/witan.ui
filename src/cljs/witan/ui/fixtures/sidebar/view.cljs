(ns ^:figwheel-always witan.ui.fixtures.sidebar.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [clojure.string :as str]
              ;;
              [witan.ui.util :as util]
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              [witan.ui.util :refer [goto-window-location!]]
              [venue.core :as venue])
    (:require-macros [cljs-log.core :as log]))

(defcomponent view
  [cursor owner]
  (render [_]
          (html
           [:div.text-center
            [:a.sidebar-link
             {:href "/#"}
             [:i.material-icons.md-l "assessment"]
             [:span (-> (get-string :forecast) i/plural str/upper-case)]]
            [:hr.green]
            [:a.sidebar-link
             {:on-click #(.Intercom js/window "show")}
             [:i.material-icons.md-l "live_help"]
             [:span (-> (get-string :help) str/upper-case)]]
            [:a.sidebar-link
             {:on-click (fn [e]
                          (venue/raise! owner :event/logout {})
                          (.preventDefault e))}
             [:i.material-icons.md-l "power_settings_new"]
             [:span (-> (get-string :logout) str/upper-case)]]])))
