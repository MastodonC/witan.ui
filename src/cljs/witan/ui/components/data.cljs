(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]))

(defn reverse-group->activity-map
  [all-activities sharing]
(log/debug sharing)
  (let [sharing-sets (zipmap (keys sharing)
                             (map set (vals sharing)))
        all-groups (set (apply concat (vals sharing)))]
    (reduce 
     (fn [group->activities group]
       (assoc group->activities
              group
              (reduce
               (fn [a activity]
                 (assoc a
                        activity
                        (not (nil? ((get sharing-sets activity #{})
                                    group)))))
               {}
               all-activities)))
     {}
     all-groups)))

(defn view
  []
  (let [{:keys [ds/current ds/download-pending?]} (data/get-in-app-state :app/datastore)
        md (data/get-in-app-state :app/datastore :ds/file-metadata current)]
    (if-not md
      [:div.loading
       (icons/loading :large)]
      (let [{:keys [kixi.datastore.metadatastore/file-type
                    kixi.datastore.metadatastore/name
                    kixi.datastore.metadatastore/sharing
                    kixi.datastore.metadatastore/provenance
                    kixi.datastore.metadatastore/size-bytes]} md
            activities->string  {:kixi.datastore.metadatastore/meta-read (get-string :string/file-sharing-meta-read)
                                 :kixi.datastore.metadatastore/file-read (get-string :string/file-sharing-file-read)} ;TODO get this from datastore
            ]
        [:div#data-view.padded-content
         [:h2 (get-string :string/file-name ":" name)]
         ;; ----------------------------------------------
         [:hr]
         [:div.field-entries
          [:div.field-entry
           [:strong (get-string :string/file-type ":")]
           [:span file-type]]
          [:div.field-entry
           [:strong (get-string :string/file-provenance-source ":")]
           [:span (:kixi.datastore.metadatastore/source provenance)]]
          [:div.field-entry
           [:strong (get-string :string/created-at ":")]
           [:span (utils/iso-time-as-moment (:kixi.datastore.metadatastore/created provenance))]]
          [:div.field-entry
           [:strong (get-string :string/file-size ":")]
           [:span (js/filesize size-bytes)]]
          [:div.field-entry
           [:strong (get-string :string/file-uploader ":")]
           [:span (get-in provenance [:kixi/user :kixi.user/name])]]]
         ;; ----------------------------------------------
         [:hr]
         [:div.sharing-controls
          [:h2 (get-string :string/sharing)]
          [:div.sharing-activity
           ;; meta-read
           [:strong "Groups who can see this data"]
           [:div.selected-groups
            [shared/sharing-matrix activities->string
             (reverse-group->activity-map (keys activities->string) sharing)
             (fn [[group activities] activity target-state] 
               (controller/raise! :data/sharing-change
                                  {:current current
                                   :group group
                                   :activity activity
                                   :target-state target-state}))]]
           #_[shared/group-search-area :string/data-upload-search-groups #()]]]
         ;; ----------------------------------------------
         [:hr]
         [:div.actions
          (shared/button {:id :button-a
                          :icon (if download-pending? icons/loading icons/download)
                          :txt :string/file-actions-download-file
                          :class "file-action-download"}
                         #(when-not download-pending?
                            (controller/raise! :data/download-file {:id current})))]]))))
