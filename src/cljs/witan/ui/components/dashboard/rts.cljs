(ns witan.ui.components.dashboard.rts
  (:require [reagent.core :as r]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.components.icons :as icons]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.controller :as controller])
  (:require-macros [cljs-log.core :as log]))

(defn get-progress
  [{:keys [kixi.data-acquisition.request-to-share/recipients]}]
  (let [noof-recips (count recipients)
        noof-submitters (count (filter :kixi.data-acquisition.request-to-share.recipient/data-id recipients))]
    [noof-submitters noof-recips]))

(defn get-progress-str
  [[x y]]
  (str x "/" y))

(defn get-progress-status
  [[x y]]
  (if (= x y)
    (icons/tick-circle :success)
    (icons/warning :warning)))

(defn view
  []
  (let [selected-outbound (r/atom nil)]
    (fn []
      (let [requests (:rts/requests (data/get-app-state :app/request-to-share))
            buttons [{:id :button-a
                      :icon icons/open
                      :txt :string/create-request-to-share
                      :class "create-rts-button"
                      :nav :app/rts-create
                      :desc :string/create-request-to-share-desc}]]
        [:div.dashboard
         (shared/header :string/request-to-share-dash-title :string/request-to-share-dash-desc)
         [:div.content
          [:div#rts-functions
           {:key "rts-functions"}
           [:h1
            {:key "title"}
            (get-string :string/get-started)]
           [:div.buttons
            {:key "buttons"}
            (for [button buttons]
              [:div
               {:style {:display :flex}
                :key "button-container"}
               [:div
                {:key "button"}
                (shared/button button #(route/navigate! (:nav button)))]
               [:span.description
                {:key "description"}
                (get-string (:desc button))]])]]
          [:div
           {:key "tables"}
           (if (not-empty requests)
             [:div
              [:h2 {:style {:margin-left "10px"}} (get-string :string/outbound-requests)]
              (shared/table {:headers [{:title (get-string :string/status)
                                        :weight 0.05
                                        :content-fn (comp get-progress-status get-progress)}
                                       {:title (get-string :string/date)
                                        :weight 0.3
                                        :content-fn (comp time/iso-time-as-moment :kixi.data-acquisition.request-to-share/created-at)}
                                       {:title (get-string :string/schema)
                                        :weight 0.35
                                        :content-fn (comp :schema/name :kixi.data-acquisition.request-to-share/schema)}
                                       {:title (get-string :string/progress)
                                        :weight 0.2
                                        :content-fn (comp get-progress-str get-progress)}
                                       {:title ""
                                        :weight 0.1
                                        :content-fn
                                        #(when-let [id (= (:kixi.data-acquisition.request-to-share/request-id %) @selected-outbound)]
                                           (shared/button {:id :view :icon icons/open :txt :string/view}
                                                          (fn [_] (route/navigate! :app/rts {:id @selected-outbound}))))}]
                             :content (vals requests)
                             :selected?-fn #(= (:kixi.data-acquisition.request-to-share/request-id %) @selected-outbound)
                             :on-select #(reset! selected-outbound (:kixi.data-acquisition.request-to-share/request-id %))
                             :on-double-click #(do
                                                 (reset! selected-outbound (:kixi.data-acquisition.request-to-share/request-id %))
                                                 (route/navigate! :app/rts {:id @selected-outbound}))})]
             [:h3#rts-no-requests (get-string :string/rts-no-requests)])]]]))))
