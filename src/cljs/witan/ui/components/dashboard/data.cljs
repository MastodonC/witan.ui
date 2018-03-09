(ns witan.ui.components.dashboard.data
  (:require [reagent.core :as r]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.components.icons :as icons]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [witan.ui.route   :as route]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data]
            [witan.ui.controller :as controller])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defmulti button-press
  (fn [_ event] event))

(defmethod button-press :upload
  [selected-id _]
  (route/navigate! :app/data-create))

(defmethod button-press :datapack
  [selected-id _]
  (route/navigate! :app/datapack-create))

(defn search-bar
  [{:keys [on-search]}]
  (let [current-search-value (route/get-query-param :search-term)
        current-metadata-filter (or (route/get-query-param :metadata-filter)
                                    "everything")]
    [:div.search-bar.flex-vcenter
     [:div.flex.search-input
      (shared/search-filter (get-string :string/search)
                            on-search
                            {:current-search-value current-search-value})]
     [:div.flex.search-dropdown
      (icons/filter-list :small)
      [:select {:id  "metadata-filter"
                :type "text"
                :value current-metadata-filter
                :placeholder nil
                :on-change #(let [selected (.. % -target -value)]
                              (route/assoc-query-param-drop-page! :metadata-filter selected)
                              (controller/raise! :search/dashboard (route/get-query-map)))}
       (for [[k txt] (get-string :string/search-metadata-filter)]
         [:option {:key k :value (name k)} txt])]]]))

(defn view
  []
  (let [selected-id (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this])
      :reagent-render
      (fn []
        (let [current-search (data/get-in-app-state :app/search :ks/dashboard :ks/current-search)
              current-results (get (data/get-in-app-state :app/search :ks/dashboard :ks/search->result)
                                   current-search)
              page-count (inc (.ceil js/Math (/ (get-in current-results [:paging :total])
                                                (:size current-search))))
              current-page (inc (.ceil js/Math (/ (:from current-search)
                                                  (:size current-search))))
              buttons [{:id :datapack :icon icons/datapack :txt :string/create-new-datapack :class "data-upload"}
                       {:id :upload :icon icons/upload :txt :string/upload-new-data :class "data-upload"}]
              modified-fn #(vector :div (time/iso-time-as-moment (get-in % [:kixi.datastore.metadatastore/provenance :kixi.datastore.metadatastore/created])))
              selected-id' @selected-id
              navigate-fn #(route/navigate! :app/data {:id (:kixi.datastore.metadatastore/id %)})
              actions-fn (fn [d] (when (= (:kixi.datastore.metadatastore/id d) selected-id')
                                   (vector :div (shared/button {:icon icons/search
                                                                :txt :string/view
                                                                :id (:kixi.datastore.metadatastore/id d)}
                                                               #(navigate-fn {:kixi.datastore.metadatastore/id %})))))
              name-fn #(vector :div.data-name (case (:kixi.datastore.metadatastore/type %)
                                                "stored" (icons/file-type (:kixi.datastore.metadatastore/file-type %) :small)
                                                "bundle" (icons/bundle-type (:kixi.datastore.metadatastore/bundle-type %) :small)
                                                :small) [:h4 (:kixi.datastore.metadatastore/name %)])]
          [:div#data-dash.dashboard
           (shared-dash/header {:title :string/data-dash-title
                                :filter-txt :string/data-dash-filter
                                :filter-fn nil
                                :buttons buttons
                                :subtitle (when-let [metadata-type (:metadata-type current-search)]
                                            (case metadata-type
                                              "stored" :string/dash-filter--files
                                              "datapack" :string/dash-filter--datapacks))
                                :on-button-click (partial button-press (str selected-id'))})
           [:div.search-bar-container
            (search-bar {:on-search (fn [search-term]
                                      (log/debug "Search: " search-term)
                                      (if-not (empty? search-term)
                                        (route/assoc-query-param-drop-page! :search-term search-term)
                                        (route/dissoc-query-params :search-term :page))
                                      (controller/raise! :search/dashboard (route/get-query-map)))})]
           [:div.content
            (shared/table {:headers [{:content-fn name-fn
                                      :title (get-string :string/forecast-name)
                                      :weight 0.45}
                                     {:content-fn (comp :kixi.user/name :kixi/user :kixi.datastore.metadatastore/provenance)
                                      :title (get-string :string/file-uploader)
                                      :weight 0.2}
                                     {:content-fn modified-fn
                                      :title (get-string (if (= (:metadata-type current-search) "datapack")
                                                           :string/created-at
                                                           :string/file-uploaded-at))
                                      :weight 0.2}
                                     {:content-fn actions-fn
                                      :title ""
                                      :weight 0.15}]
                           :content (:items current-results)
                           :selected?-fn #(= (:kixi.datastore.metadatastore/id %) selected-id')
                           :on-select #(reset! selected-id (:kixi.datastore.metadatastore/id %))
                           :on-double-click navigate-fn})
            (when-not (empty? (:items current-results))
              [:div.flex-center.dash-pagination
               [shared/pagination {:page-blocks (range 1 page-count)
                                   :current-page current-page}
                (fn [id]
                  (let [new-page (js/parseInt (subs id 5))]
                    (route/navigate! :app/data-dash {} (assoc (route/get-query-map)
                                                              :page new-page))))]])]]))})))
