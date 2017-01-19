(ns witan.ui.components.dashboard.data
  (:require [reagent.core :as r]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.components.icons :as icons]
            [witan.ui.utils :as utils]
            [witan.ui.route   :as route]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.data :as data])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defmulti button-press
  (fn [_ event] event))

(defmethod button-press :upload
  [selected-id _]
  (route/navigate! :app/data-create))

(defn file-metadata->dash-display
  [file-metadata]
  {:data/id (:kixi.datastore.metadatastore/id file-metadata)
   :data/name (:kixi.datastore.metadatastore/name file-metadata)
   :data/schema (:kixi.datastore.metadatastore/schema file-metadata)
   :data/owner-name (get-in file-metadata [:kixi.datastore.metadatastore/provenance :kixi.user/id])
   :data/created-at (get-in file-metadata [:kixi.datastore.metadatastore/provenance :kixi.datastore.metadatastore/created])})

(defn view
  []
  (let [selected-id (r/atom nil)]
    (fn []
      (let [raw-data (data/get-app-state :app/data-dash)
            buttons [{:id :upload :icon icons/upload :txt :string/upload :class "data-upload"}]
            modified-fn #(vector :div (utils/iso-time-as-moment (:data/created-at %)))
            datasets (mapv file-metadata->dash-display (:items raw-data))
            selected-id' @selected-id
            icon-fn #(vector :div.text-center (icons/workspace (if (:data/local %) :error :dark)))]
        [:div.dashboard
         (shared-dash/header {:title :string/data-dash-title
                              :filter-txt :string/data-dash-filter
                              :filter-fn nil
                              :buttons buttons
                              :on-button-click (partial button-press (str selected-id'))})
         [:div.content
          (shared/table {:headers [{:content-fn icon-fn               :title ""              :weight 0.03}
                                   {:content-fn :data/name       :title (get-string :string/forecast-name)          :weight 0.3}
                                   {:content-fn :data/schema       :title (get-string :string/schema)          :weight 0.3}
                                   {:content-fn :data/owner-name :title (get-string :string/author)         :weight 0.2}
                                   {:content-fn modified-fn           :title (get-string :string/created-at) :weight 0.2}]
                         :content datasets
                         :selected?-fn #(= (:data/id %) selected-id')
                         :on-select #(reset! selected-id (:data/id %))
                         :on-double-click #(route/navigate! :app/data {:id (str (:data/id %))})})]]))))
