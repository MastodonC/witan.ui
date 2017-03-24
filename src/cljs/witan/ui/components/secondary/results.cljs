(ns witan.ui.components.secondary.results
  (:require[reagent.core :as r]
           [cljs.test :refer-macros [is async]]
           [sablono.core :as sab :include-macros true]
           [witan.ui.utils :as utils]
           [witan.ui.time :as time]
           [witan.ui.data :as data]
           [witan.ui.controller :as controller]
           [witan.ui.components.icons :as icons]
           [witan.ui.strings :refer [get-string]]
           [witan.ui.components.primary :refer [switch-primary-view!]]
           [inflections.core :as i])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :as am :refer [go-loop]]))

(defn results-table
  [current-results]
  (let [collapsed (r/atom #{})
        selected  (r/atom #{})]
    (fn [current-results]
      [:div
       (doall
        (for [[group-key results] (group-by :result/key current-results)]
          [:table.pure-table.pure-table-horizontal
           {:key (str "result-grp-row" group-key)}
           [:thead
            [:tr
             [:td.result-group-name
              {:on-click #(let [fnc (if (contains? @collapsed group-key) disj conj)]
                            (swap! collapsed fnc group-key))}
              (icons/add-react-args
               (icons/tree-arrow-down)
               {:class (str "rotates "
                            (if (contains? @collapsed group-key) "rotate0" "rotate270"))})
              (str (i/capitalize (name group-key))
                   " (" (count results) ")")]
             [:td.result-group-actions
              [:button.pure-button
               {:class (when (< (count @selected) 2) "pure-button-disabled")
                :on-click #(do
                             (controller/raise! :workspace/open-as-visualisation {:location (mapv :result/location @selected)
                                                                                  :style :lineplot})
                             (switch-primary-view! :viz))}
               (get-string :string/compare)]]]]]))
       (doall
        (for [[group-key results] (group-by :result/key current-results)]
          (when (contains? @collapsed group-key)
            [:table.pure-table.pure-table-horizontal
             {:key (str "result-data-row" group-key)}
             [:tbody
              (doall
               (for [{:keys [result/key
                             result/created-at
                             result/location
                             result/downloading?] :as result}
                     (reverse
                      (sort-by :result/created-at results))]
                 ^{:key created-at}
                 [:tr
                  [:td.col-results-time {:key "time"} (time/iso-time-as-moment created-at)]
                  [:td.col-results-actions {:key "actions"}
                   [:button.pure-button
                    {:class (when downloading? "pure-button-disabled")
                     :on-click #(controller/raise! :workspace/download-result {:result result})}
                    (if downloading?
                      (icons/loading :small :dark)
                      (icons/download :small :dark))]
                   [:button.pure-button {:on-click #(do
                                                      (controller/raise! :workspace/open-as-visualisation {:location location})
                                                      (switch-primary-view! :viz))}
                    (icons/visualisation :small :dark)]
                   [:button.pure-button
                    {:on-click #(let [fnc (if (contains? @selected result) disj conj)]
                                  (swap! selected fnc result))}
                    (if (contains? @selected result)
                      (icons/checked :small :dark)
                      (icons/unchecked :small :dark))]]]))]])))])))

(defn header
  []
  [:div#results-header
   [:h1.text-center (get-string :string/workspace-result-history)]])

(defn view
  []
  (fn []
    (let [results (data/get-app-state :app/workspace-results)]
      [:div#results
       (if (empty? results)
         [:div#no-results (get-string :string/no-results)]
         [results-table results])])))
