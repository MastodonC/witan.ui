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

(defn view
  []
  (let [id (data/get-in-app-state :app/datastore :ds/current)
        md (data/get-in-app-state :app/datastore :ds/file-metadata id)]
    (if-not md
      [:div.loading
       (icons/loading :large)]
      (let [{:keys [kixi.datastore.metadatastore/file-type
                    kixi.datastore.metadatastore/name
                    kixi.datastore.metadatastore/sharing
                    kixi.datastore.metadatastore/provenance
                    kixi.datastore.metadatastore/size-bytes]} md
            meta-read-groups (:kixi.datastore.metadatastore/meta-read sharing)]
        [:div#data-view.padded-content
         [:h2 (get-string :string/file-name ":" name)]
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
           [:span (:kixi.user/id provenance)]]]
         [:hr]
         [:div.sharing-controls
          [:h2 (get-string :string/sharing)]
          [:div.sharing-activity
           ;; meta-read
           [:strong "Groups who can see this data"]
           [:div.selected-groups
            [:span.success
             (gstring/format
              (get-string :string/file-sharing-meta-read)
              (count meta-read-groups))]
            [:div
             (for [group meta-read-groups]
               ^{:key identity}
               [:div
                #_[:span.removal-link
                   "(" [:a {:href "javascript:void(0)"
                            :on-click
                            #()}
                        (get-string :string/remove-lc)] ")"]
                #_(shared/inline-group group)
                [:span group]])]]
           #_[shared/group-search-area :string/data-upload-search-groups #()]]]]))))
