(ns witan.ui.components.secondary
  (:require [reagent.core :as r]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [witan.ui.route :as route]
            [witan.ui.components.icons :as icons]
            ;;
            [witan.ui.components.secondary.data-select :as data-select]
            [witan.ui.components.secondary.configuration :as configuration]
            [witan.ui.components.secondary.results :as results])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as am :refer [go-loop]]))

(defn switcher
  [{:keys [titles selected-idx on-select]}]
  [:div.secondary-switcher
   (for [[idx title] (map-indexed vector titles)]
     ^{:key idx}
     [:button.pure-button
      {:style {:width (str (/ 100 (count titles)) "%")}
       :class (when (= selected-idx idx) "selected")
       :on-click #(when on-select (on-select idx))}
      title])])

(defn main-view
  []
  (let [query-param :s
        subview-idx (r/atom (or (utils/query-param-int query-param 0 1) 0))]
    (fn []
      [:div.secondary-outer-container
       [:div#switcher
        (switcher {:titles [(get-string :string/workspace-config-view)
                            (get-string :string/workspace-data-view)
                            #_(get-string :string/workspace-history-view)]
                   :selected-idx @subview-idx
                   :on-select #(do
                                 (route/swap-query-string!
                                  (fn [m] (assoc m query-param %)))
                                 (reset! subview-idx %))})]
       [:div.secondary-container
        (condp = @subview-idx
          1      [data-select/view]
          0      [configuration/view])]])))

(defn results-view
  []
  [:div.secondary-outer-container
   [results/header]
   [:div.secondary-container
    [results/view]]])
