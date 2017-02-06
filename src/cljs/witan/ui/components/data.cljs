(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defn reverse-group->activity-map
  [all-activities sharing]
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

(defn format-description
  [description-str]
  (let [substrings (clojure.string/split description-str #"\n")]
    [:div
     (doall
      (mapcat
       #(vector [:span {:key (str "string-" %1)} %2]
                [:br {:key (str "br-" %1)}])
       (range (count substrings))
       substrings))]))

(defn view
  []
  (let [{:keys [ds/current ds/download-pending? ds/error] :as ds}
        (data/get-in-app-state :app/datastore)
        activities->string (:ds/activities ds)
        md (data/get-in-app-state :app/datastore :ds/file-metadata current)]
    (if error
      [:div.text-center.padded-content
       [:div
        (icons/error :dark :large)]
       [:div [:h3 (get-string error)]]]
      (if-not md
        [:div.loading
         (icons/loading :large)]
        (let [{:keys [kixi.datastore.metadatastore/file-type
                      kixi.datastore.metadatastore/name
                      kixi.datastore.metadatastore/description
                      kixi.datastore.metadatastore/sharing
                      kixi.datastore.metadatastore/provenance
                      kixi.datastore.metadatastore/size-bytes]} md
              sharing-groups (set (reduce concat [] (vals sharing)))]
          [:div#data-view
           [:div.container.padded-content
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
            (when description
              [:div.field-entry
               [:strong (get-string :string/file-description ":")]
               (format-description description)])
            ;; ----------------------------------------------
            [:hr]
            [:div.sharing-controls
             [:h2 (get-string :string/sharing)]
             [:div.sharing-activity
              [:div.selected-groups
               [shared/sharing-matrix activities->string
                (reverse-group->activity-map (keys activities->string) sharing)
                {:on-change
                 (fn [[group activities] activity target-state]
                   (controller/raise! :data/sharing-change
                                      {:current current
                                       :group group
                                       :activity activity
                                       :target-state target-state}))
                 :on-add
                 (fn [group]
                   (controller/raise! :data/sharing-add-group
                                      {:current current :group group}))}
                {:exclusions sharing-groups}]]]]
            ;; ----------------------------------------------
            [:hr]
            [:div.actions
             [:a {:href (str
                         "http://"
                         (or (cljs-env :witan-api-url) "localhost:30015")
                         "/download?id="
                         current)
                  :target "_blank"} (shared/button {:id :button-a
                                                    :icon icons/download
                                                    :txt :string/file-actions-download-file
                                                    :class "file-action-download"}
                                                   #())]]]])))))
