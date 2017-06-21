(ns witan.ui.components.side
  (:require [cljs.test :refer-macros [is async]]
            [goog.dom :as gdom]
            [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.data :as data]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.route :as route]
            [witan.ui.controller :as controller])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn get-icon
  [id]
  (let [props [:medium]]
    (apply
     (get
      {:workspaces  icons/workspace
       :data        icons/data
       :rts         icons/request-to-share
       :help        icons/help
       :logout      icons/logout
       :activity    icons/activity}
      id) props)))

(defn navigate!
  [route current-route]
  (when-not (= route current-route)
    (route/navigate! route)))

(defn get-details
  [id]
  (get
   {:workspaces {:fnc (partial navigate! :app/workspace-dash)
                 :tooltip :string/tooltip-workspace}
    :data       {:fnc (partial navigate! :app/data-dash)
                 :tooltip :string/tooltip-data}
    :rts        {:fnc (partial navigate! :app/request-to-share)
                 :tooltip :string/tooltip-request-to-share}
    :help       {:fnc #(controller/raise! :intercom/open-new)
                 :tooltip :string/tooltip-help}
    :logout     {:fnc #(controller/raise! :user/logout)
                 :tooltip :string/tooltip-logout}
    :activity   {:fnc (partial navigate! :app/data-dash)
                 :tooltip :string/tooltip-activity}}
   id))

(defn add-side-elements!
  [element-list current-route disabled?]
  (for [[element-type element-key] element-list]
    [:div.side-element
     {:key element-key}
     (condp = element-type
       :button
       (let [{:keys [route tooltip fnc]} (get-details element-key)]
         [:div.side-link
          {:on-click #(when fnc
                        (fnc current-route))
           :data-ot (get-string tooltip)
           :data-ot-style "dark"
           :data-ot-tip-joint "left"
           :data-ot-fixed true
           :data-ot-target true
           :data-ot-delay 0.5
           :data-ot-contain-in-viewport false
           ;;
           :style {:pointer-events (if disabled? "none" "auto")}}
          (get-icon element-key)])
       :hr [:hr])]))

(defn side-bar
  [{:keys [side/upper side/lower]} path disabled?]
  [:div#side-container
   [:div#side-upper
    (add-side-elements! upper path disabled?)]
   [:div#side-lower
    (add-side-elements! lower path disabled?)]])

(defn root-view
  []
  (r/create-class
   {:reagent-render
    (fn []
      (let [panic-message (data/get-app-state :app/panic-message)
            {:keys [route/path]} (data/get-app-state :app/route)
            side (data/get-app-state :app/side)]
        (side-bar side path panic-message)))}))


;;;;;;;;;;;;;;;;;;;;;;;;;


(defcard side-bar
  (sab/html
   [:div
    {:style {:background-color "gray"
             :position "fixed"
             :bottom "0"
             :left "0"
             :height "100%"}}
    (side-bar {:side/upper '([:button :workspaces]
                             [:button :data]
                             [:hr])
               :side/lower '([:button :help]
                             [:button :logout])} :app/foo false)]))
