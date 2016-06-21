(ns witan.ui.components.side
  (:require [cljs.test :refer-macros [is async]]
            [goog.dom :as gdom]
            [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            ;;
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
       :help        icons/help
       :logout      icons/logout}
      id) props)))

(defn navigate!
  [route _ current-route]
  (when-not (= route current-route)
    (route/navigate! route)))

(defn get-details
  [id]
  (get
   {:workspaces {:fnc (partial navigate! :app/workspace-dash) :tooltip :string/tooltip-workspace}
    :data       {:fnc (partial navigate! :app/data-dash)      :tooltip :string/tooltip-data}
    :help       {:fnc nil                                     :tooltip :string/tooltip-help}
    :logout     {:fnc #(controller/raise! %1 :user/logout)    :tooltip :string/tooltip-logout}}
   id))

(defn add-side-elements!
  [this element-list current-route]
  (for [[element-type element-key] element-list]
    [:div.side-element
     {:key element-key}
     (condp = element-type
       :button
       (let [{:keys [route tooltip fnc]} (get-details element-key)]
         [:div.side-link
          {:on-click #(when fnc
                        (fnc this current-route))
           :data-ot (get-string tooltip)
           :data-ot-style "dark"
           :data-ot-tip-joint "left"
           :data-ot-fixed true
           :data-ot-target true
           :data-ot-delay 0.5
           :data-ot-contain-in-viewport false}
          (get-icon element-key)])
       :hr [:hr])]))

(defn root-view
  []
  (r/create-class
   {:reagent-render
    (fn [this]
      (let [{:keys [app/route app/side]} this
            {:keys [side/upper side/lower]} side]
        [:div#side-container
         [:div#side-upper
          (add-side-elements! this upper route)]
         [:div#side-lower
          {:align "center"}
          (add-side-elements! this lower route)]]))}))


;;;;;;;;;;;;;;;;;;;;;;;;;


(defcard side-bar
  (sab/html
   [:div
    {:style {:background-color "gray"
             :position "fixed"
             :bottom "0"
             :left "0"
             :height "100%"}}
    (side-bar {:app/side {:side/upper '([:button :workspaces]
                                        [:button :data]
                                        [:hr])
                          :side/lower '([:button :help]
                                        [:button :logout])}})]))
