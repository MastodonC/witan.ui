(ns witan.ui.components.primary
  (:require [reagent.core :as r]
            [witan.ui.components.icons :as icons]
            [witan.ui.data :as data]
            [cljs.reader :as reader]
            [witan.ui.utils :as utils]
            [witan.ui.route :as route]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.components.icons :as icons]
            [witan.ui.components.primary.topology :as topology])
  (:require-macros
   [cljs-log.core :as log]))

(defn switcher
  [{:keys [icon-0 icon-1 selected-idx on-select]}]
  [:div.primary-switcher
   [:div#indicator-container
    [:div#indicator
     {:class (when (= selected-idx 1) "indicator-offset-1")}]]
   [:div.icons
    [:div#icon-0.icon
     {:class (when (= selected-idx 0) "selected")
      :on-click #(when on-select (on-select 0))}
     (icon-0)]
    [:div#icon-1.icon
     {:class (when (= selected-idx 1) "selected")
      :on-click #(when on-select (on-select 1))}
     (icon-1)]]])

(defn view
  []
  (let [query-param :p
        subview-idx (r/atom (or (utils/query-param-int query-param 0 1) 0))]
    (fn []
      (let [wsp (data/get-app-state :app/workspace)
            pending? (:workspace/pending? wsp)]
        (if pending?
          [:div#container
           [:div#loading (icons/loading :large)]]
          (if-let [current (:workspace/current wsp)]
            [:div#container
             [:div#overlay
              (switcher {:icon-0 (partial icons/topology :dark :medium)
                         :icon-1 (partial icons/visualisation :dark :medium)
                         :selected-idx @subview-idx
                         :on-select #(do
                                       (route/swap-query-string!
                                        (fn [m] (assoc m query-param %)))
                                       (reset! subview-idx %))})]
             [:div#primary-content
              (condp = @subview-idx
                0 (topology/view current)
                1 [:div "????"])]]
            [:div#loading
             (icons/error :large :dark)
             [:h1 (get-string :string/error)]
             [:h3 (get-string :string/workspace-404-error)]
             [:h4 [:a {:href "/"} "Click here to view your Workspaces"]]]))))))
