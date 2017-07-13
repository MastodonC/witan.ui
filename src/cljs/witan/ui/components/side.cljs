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

(def side-bar-config
  {:side/upper [#_[:button :workspaces]
                [:button :data
                 [[:button :data-files]
                  [:button :data-datapacks]]]
                #_[:button :rts]]
   :side/lower [[:button :activity]
                [:button :help]
                [:button :logout]]})

(defn get-details
  [id]
  (get
   {:workspaces {:fnc (partial route/navigate! :app/workspace-dash)
                 :tooltip :string/tooltip-workspace
                 :icon icons/workspace}
    :data       {:fnc (partial route/navigate! :app/data-dash)
                 :tooltip :string/tooltip-data
                 :icon icons/data}
    :data-files {:fnc (partial route/navigate! :app/data-dash {} {:type "files"})
                 :tooltip :string/tooltip-data--files
                 :icon icons/file}
    :data-datapacks {:fnc (partial route/navigate! :app/data-dash {} {:type "datapacks"})
                     :tooltip :string/tooltip-data--datapacks
                     :icon icons/datapack}
    :rts        {:fnc (partial route/navigate! :app/request-to-share)
                 :tooltip :string/tooltip-request-to-share
                 :icon icons/request-to-share}
    :help       {:fnc #(controller/raise! :intercom/open-new)
                 :tooltip :string/tooltip-help
                 :icon icons/help}
    :logout     {:fnc #(controller/raise! :user/logout)
                 :tooltip :string/tooltip-logout
                 :icon icons/logout}
    :activity   {:fnc (partial route/navigate! :app/activity)
                 :tooltip :string/tooltip-activity
                 :icon icons/activity}
    :debug      {:fnc (partial route/navigate! :app/debug)
                 :tooltip :string/tooltip-debug
                 :icon icons/bug}}
   id))

(defn add-side-elements!
  [element-list disabled?]
  (for [[element-type element-key leaf-buttons] element-list]
    [:div.side-element
     {:key element-key}
     (condp = element-type
       :button
       (let [{:keys [route tooltip fnc icon]} (get-details element-key)]
         [:div
          [:div.side-link
           {:on-click #(when fnc
                         (fnc))
            :data-ot (get-string tooltip)
            :data-ot-style "dark"
            :data-ot-tip-joint "left"
            :data-ot-fixed true
            :data-ot-target true
            :data-ot-delay 0.5
            :data-ot-contain-in-viewport false
            ;;
            :style {:pointer-events (if disabled? "none" "auto")}}
           (icon :medium)]
          (when leaf-buttons
            [:div.sub-side-element
             (add-side-elements! leaf-buttons disabled?)])])
       :hr [:hr])]))

(defn side-bar
  [{:keys [side/upper side/lower]} disabled?]
  [:div#side-container
   [:div#side-upper
    (add-side-elements! upper disabled?)]
   [:div#side-lower
    (add-side-elements! (if (:debug? data/config)
                          (cons [:button :debug] lower)
                          lower) disabled?)]])

(defn root-view
  []
  (r/create-class
   {:reagent-render
    (fn []
      (let [panic-message (data/get-app-state :app/panic-message)]
        (side-bar side-bar-config panic-message)))}))


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
