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

(def dash-page-query-param :p)

(defmulti button-press
  (fn [_ event] event))

(defmethod button-press :upload
  [selected-id _]
  (route/navigate! :app/data-create))

(defmethod button-press :datapack
  [selected-id _]
  (route/navigate! :app/datapack-create))

(defn filter-fn
  [file-type-filter]
  (case file-type-filter
    :files (comp (partial = "stored") :kixi.datastore.metadatastore/type)
    :datapacks (comp (partial = "datapack") :kixi.datastore.metadatastore/bundle-type)
    (constantly true)))

(defn view
  []
  (let [selected-id (r/atom nil)]
    (r/create-class
     {:component-did-mount
      (fn [this]
        (controller/raise!
         :data/set-current-page
         {:page (or (utils/query-param-int dash-page-query-param 1 999) 1)}))
      :reagent-render
      (fn []
        (let [raw-data (data/get-app-state :app/data-dash)
              current-page (data/get-in-app-state :app/data-dash :dd/current-page)
              file-type-filter (:dd/file-type-filter raw-data)
              buttons [{:id :datapack :icon icons/datapack :txt :string/create-new-datapack :class "data-upload"}
                       {:id :upload :icon icons/upload :txt :string/upload-new-data :class "data-upload"}]
              modified-fn #(vector :div (time/iso-time-as-moment (get-in % [:kixi.datastore.metadatastore/provenance :kixi.datastore.metadatastore/created])))
              datasets (when (:items raw-data) (filter (filter-fn file-type-filter) (:items raw-data)))
              selected-id' @selected-id
              navigate-fn #(route/navigate! :app/data {:id (:kixi.datastore.metadatastore/id %)})
              actions-fn (fn [d] (when (= (:kixi.datastore.metadatastore/id d) selected-id')
                                   (vector :div (shared/button {:icon icons/search
                                                                :txt :string/view
                                                                :id (:kixi.datastore.metadatastore/id d)}
                                                               #(navigate-fn {:kixi.datastore.metadatastore/id %})))))
              name-fn #(vector :div.data-name (case (:kixi.datastore.metadatastore/type %)
                                                "stored" (icons/file-type (:kixi.datastore.metadatastore/file-type %) :small)
                                                "bundle" (icons/bundle-type (:kixi.datastore.metadatastore/bundle-type %) :small)) [:h4 (:kixi.datastore.metadatastore/name %)])]
          [:div#data-dash.dashboard
           (shared-dash/header {:title :string/data-dash-title
                                :filter-txt :string/data-dash-filter
                                :filter-fn nil
                                :buttons buttons
                                :subtitle (when file-type-filter
                                            (case file-type-filter
                                              :files :string/dash-filter--files
                                              :datapacks :string/dash-filter--datapacks))
                                :on-button-click (partial button-press (str selected-id'))})
           [:div.content
            (shared/table {:headers [{:content-fn name-fn
                                      :title (get-string :string/forecast-name)
                                      :weight 0.45}
                                     {:content-fn (comp :kixi.user/name :kixi/user :kixi.datastore.metadatastore/provenance)
                                      :title (get-string :string/file-uploader)
                                      :weight 0.2}
                                     {:content-fn modified-fn
                                      :title (get-string (if (= file-type-filter :datapacks)
                                                           :string/created-at
                                                           :string/file-uploaded-at))
                                      :weight 0.2}
                                     {:content-fn actions-fn
                                      :title ""
                                      :weight 0.15}]
                           :content datasets
                           :selected?-fn #(= (:kixi.datastore.metadatastore/id %) selected-id')
                           :on-select #(reset! selected-id (:kixi.datastore.metadatastore/id %))
                           :on-double-click navigate-fn})
            (when datasets
              [:div.flex-center.dash-pagination
               (if file-type-filter
                 [:span.clickable-text
                  {:on-click #(route/navigate! :app/data-dash {})}
                  (get-string :string/dash-reanable-paging)]
                 [shared/pagination {:page-blocks
                                     (range 1 (inc (.ceil js/Math (/ (get-in raw-data [:paging :total])
                                                                     (data/get-in-app-state :app/datastore :ds/page-size)))))
                                     :current-page current-page}
                  (fn [id]
                    (let [new-page (js/parseInt (subs id 5))]
                      (route/swap-query-string! #(assoc % dash-page-query-param new-page))
                      (controller/raise! :data/set-current-page {:page new-page})))])])]]))})))
