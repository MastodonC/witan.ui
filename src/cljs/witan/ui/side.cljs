(ns witan.ui.side
  (:require [cljs.test :refer-macros [is async]]
            [goog.dom :as gdom]
            [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.app :as app])
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
       :settings    icons/cog
       :help        icons/help
       :logout      icons/logout}
      id) props)))

(defn get-details
  [id]
  (get
   {:workspaces {:route :app/workspace-dash :tooltip :string/tooltip-workspace}
    :data       {:route :app/data-dash      :tooltip :string/tooltip-data}
    :settings   {:route :app/workspace      :tooltip :string/tooltip-workspace}
    :help       {:route :app/workspace-dash :tooltip :string/tooltip-data}
    :logout     {:route :app/data-dash      :tooltip :string/tooltip-workspace}}
   id))

(defn add-side-elements!
  [this element-list current-route]
  (for [[element-type element-key] element-list]
    [:div.side-element
     {:key element-key}
     (condp = element-type
       :button
       (let [{:keys [route tooltip]} (get-details element-key)]
         [:div.side-link
          {:on-click #(when-not (= route current-route)
                        (app/navigate! route {:id 123}))
           :data-ot (get-string tooltip)
           :data-ot-style "dark"
           :data-ot-tip-joint "left"
           :data-ot-fixed true
           :data-ot-target true
           :data-ot-delay 0.5
           :data-ot-contain-in-viewport false}
          (get-icon element-key)])
       :hr [:hr])]))

(defui Main
  static om/IQuery
  (query [this]
         [{:app/side [:side/upper :side/lower]} :app/route])
  Object
  (render [this]
          (let [{:keys [app/route app/side]} (om/props this)
                {:keys [side/upper side/lower]} side]
            (sab/html [:div#side-container
                       [:div#side-upper
                        (add-side-elements! this upper route)]
                       [:div#side-lower
                        {:align "center"}
                        (add-side-elements! this lower route)]]))))


;;;;;;;;;;;;;;;;;;;;;;;;;


(def side-bar (om/factory Main))

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
                                        [:hr]
                                        [:button :settings])
                          :side/lower '([:button :help]
                                        [:button :logout])}})]))
