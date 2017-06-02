(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn reverse-group->activity-map
  [all-activities sharing]
  (let [sharing-sets (zipmap (keys sharing)
                             (map set (vals sharing)))
        all-groups (set (apply concat (vals sharing)))]
    (reduce
     (fn [group->activities group]
       (assoc group->activities
              group
              {:values
               (reduce
                (fn [a activity]
                  (assoc a
                         activity
                         (not (nil? ((get sharing-sets activity #{})
                                     group)))))
                {}
                all-activities)}))
     {}
     all-groups)))

(defn lock-activities
  [sharing user-sg]
  (reduce (fn [a [group activities]]
            (let [new-activities
                  (merge activities
                         (when (= (:kixi.group/id group) user-sg)
                           {:locked (set (data/get-in-app-state
                                          :app/datastore :ds/locked-activities))}))]
              (assoc a group new-activities))) {} sharing))

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

(defn editable-field
  [on-edit-fn & render-fns]
  (let [state (r/atom :idle)]
    (fn [on-edit-fn & render-fns]
      [:div.editable-field
       {:on-mouse-over #(reset! state :hovered)
        :on-mouse-leave #(reset! state :idle)}
       (vec (cons :div.editable-field-content
                  (conj
                   (vec render-fns)
                   (when (and on-edit-fn (= :hovered @state))
                     [:span.clickable-text
                      {:on-click on-edit-fn}
                      (get-string :string/edit)]))))])))

(defn title
  [{:keys [kixi.datastore.metadatastore/name]} on-edit-fn]
  [editable-field
   on-edit-fn
   [:h1.file-title name]])

(defn description
  [{:keys [kixi.datastore.metadatastore/description]} on-edit-fn]
  [editable-field
   on-edit-fn
   [:span.file-description description]])

(defn metadata
  [{:keys [kixi.datastore.metadatastore/provenance
           kixi.datastore.metadatastore/file-type
           kixi.datastore.metadatastore/size-bytes
           kixi.datastore.metadatastore/author
           kixi.datastore.metadatastore/maintainer
           kixi.datastore.metadatastore/source
           kixi.datastore.metadatastore.time/temporal-coverage
           kixi.datastore.metadatastore.license/license
           kixi.datastore.metadatastore.geography/geography]} on-edit-fn]
  (let [prov-source     (:kixi.datastore.metadatastore/source provenance)
        prov-created-at (:kixi.datastore.metadatastore/created provenance)
        prov-created-by (:kixi/user provenance)
        tc-from         (:kixi.datastore.metadatastore.time/from temporal-coverage)
        tc-to           (:kixi.datastore.metadatastore.time/to temporal-coverage)
        geo-type        (:kixi.datastore.metadatastore.geography/type geography)
        geo-level       (:kixi.datastore.metadatastore.geography/level geography)
        lc-type         (:kixi.datastore.metadatastore.license/type license)
        lc-usage        (:kixi.datastore.metadatastore.license/usage license)
        row             (fn [string-id value-fn]
                          [:tr [:td [:strong (get-string string-id)]] [:td (value-fn)]])]
    [editable-field
     on-edit-fn
     [:div.file-metadata-table
      [:table.pure-table.pure-table-bordered.pure-table-odd
       [:tbody
        (row :string/file-type (fn [] [:span file-type]))
        (row :string/file-uploader (fn [] [:span (:kixi.user/name prov-created-by)]))
        (row :string/author (fn [] [:span author]))
        (row :string/source (fn [] [:span source]))        
        (row :string/maintainer (fn [] [:span maintainer]))        
        (row :string/created-at (fn [] [:span (time/iso-time-as-moment prov-created-at)]))
        (row :string/file-size (fn [] [:span (js/filesize size-bytes)]))]]
      [:table.pure-table.pure-table-bordered.pure-table-odd
       [:tbody
        (row :string/file-provenance-source (fn [] [:span prov-source]))
        (row :string/license-type (fn [] [:span lc-type]))
        (row :string/license-usage (fn [] [:span lc-usage]))
        (row :string/smallest-geography (fn [] [:span geo-level]))
        (row :string/temporal-coverage (fn [] [:span (when tc-from (time/iso-time-as-moment tc-from)) " - " (when tc-to (time/iso-time-as-moment tc-to))]))]]]]))

(defn tags
  [{:keys [kixi.datastore.metadatastore/tags]} on-edit-fn]
  [editable-field
   on-edit-fn
   [:div.file-tags
    [:h3 (get-string :string/tags)]
    (for [tag tags]
      (shared/tag tag identity))]])

(defn sharing
  [{:keys [kixi.datastore.metadatastore/sharing]} on-edit-fn]
  (let [uniques (->> (:kixi.datastore.metadatastore/file-read sharing)
                     (reduce conj (:kixi.datastore.metadatastore/meta-read sharing))
                     (reduce conj (:kixi.datastore.metadatastore/meta-update sharing))
                     (distinct))
        unique-count (dec (count uniques))]
    [editable-field
     on-edit-fn
     [:div.file-sharing
      [:h3 (get-string :string/sharing)]
      [:span (if (= 1 unique-count)
               (get-string :string/sharing-summary-single)
               (gstring/format (get-string :string/sharing-summary) unique-count))]]]))

(defn actions
  []
  [editable-field
   nil
   [:div.file-actions
    (shared/button {:icon icons/tick
                    :id :download
                    :txt :string/file-actions-download-file
                    :prevent? true} identity)]])

(defn view
  []
  (let [{:keys [ds/current ds/download-pending? ds/error] :as ds}
        (data/get-in-app-state :app/datastore)
        activities->string (:ds/activities ds)
        md (data/get-in-app-state :app/datastore :ds/file-metadata current)
        user-sg (data/get-in-app-state :app/user :kixi.user/self-group)]
    (if error
      [:div.text-center.padded-content
       [:div
        (icons/error :dark :large)]
       [:div [:h3 (get-string error)]]]
      (if-not md
        [:div.loading
         (icons/loading :large)]
        (let [#_{:keys [kixi.datastore.metadatastore/sharing]} #_md
              #_sharing-groups #_(set (reduce concat [] (vals sharing)))]
          [:div#data-view
           (shared/header-string (:kixi.datastore.metadatastore/name md))
           (shared/tabs {:tabs {:overview "Overview"}
                         :selected-tab :overview})
           [:div.flex-center
            [:div.container.padded-content
             (title md identity)
             (description md identity)
             (metadata md identity)
             (tags md identity)
             (sharing md identity)
             (actions)]]
           #_[:div.container.padded-content
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
                [:span (time/iso-time-as-moment (:kixi.datastore.metadatastore/created provenance))]]
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
              #_[:hr]
              #_[:div.sharing-controls
                 [:h2 (get-string :string/sharing)]
                 [:div.sharing-activity
                  [:div.selected-groups
                   [shared/sharing-matrix activities->string
                    (-> (keys activities->string)
                        (reverse-group->activity-map sharing)
                        (lock-activities user-sg))
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
                           (if (:gateway/secure? data/config) "https://" "http://")
                           (or (:gateway/address data/config) "localhost:30015")
                           "/download?id="
                           current)
                    :target "_blank"} (shared/button {:id :button-a
                                                      :icon icons/download
                                                      :txt :string/file-actions-download-file
                                                      :class "file-action-download"}
                    #())]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def example-file
  {:kixi.datastore.metadatastore/size-bytes 49
   :kixi.datastore.metadatastore/sharing
   {:kixi.datastore.metadatastore/file-read
    [{:kixi.group/id "cc4d9d25-5847-4f78-9331-c31c50544cd5",
      :kixi.group/name "Mastodon C",
      :kixi.group/type "group",
      :kixi.group/created-by "0c0d29af-4f9a-46b7-a8c6-29fde09c1f8e",
      :kixi.group/created "2017-04-24T16:35:50.581Z"}
     {:kixi.group/id "44a94379-9d4f-4d5d-8511-8c0f2ceccc24", :kixi.group/name "Test User", :kixi.group/type "user", :kixi.group/created-by "614c4e89-7e0e-4bd1-9dfb-ca115b005704", :kixi.group/created "2017-04-07T14:13:46.046Z"}],
    :kixi.datastore.metadatastore/meta-read
    [{:kixi.group/id "cc4d9d25-5847-4f78-9331-c31c50544cd5", :kixi.group/name "Mastodon C", :kixi.group/type "group", :kixi.group/created-by "0c0d29af-4f9a-46b7-a8c6-29fde09c1f8e", :kixi.group/created "2017-04-24T16:35:50.581Z"}
     {:kixi.group/id "44a94379-9d4f-4d5d-8511-8c0f2ceccc24", :kixi.group/name "Test User", :kixi.group/type "user", :kixi.group/created-by "614c4e89-7e0e-4bd1-9dfb-ca115b005704", :kixi.group/created "2017-04-07T14:13:46.046Z"}],
    :kixi.datastore.metadatastore/meta-update [{:kixi.group/id "cc4d9d25-5847-4f78-9331-c31c50544cd5", :kixi.group/name "Mastodon C", :kixi.group/type "group", :kixi.group/created-by "0c0d29af-4f9a-46b7-a8c6-29fde09c1f8e", :kixi.group/created "2017-04-24T16:35:50.581Z"}
                                               {:kixi.group/id "44a94379-9d4f-4d5d-8511-8c0f2ceccc24", :kixi.group/name "Test User", :kixi.group/type "user", :kixi.group/created-by "614c4e89-7e0e-4bd1-9dfb-ca115b005704", :kixi.group/created "2017-04-07T14:13:46.046Z"}]},
   :kixi.datastore.metadatastore/file-type "txt",
   :kixi.datastore.metadatastore/header false,
   :kixi.datastore.metadatastore/provenance {:kixi.datastore.metadatastore/source "upload",
                                             :kixi.datastore.metadatastore/created "20170524T031328.649Z",
                                             :kixi/user {:kixi.user/id "614c4e89-7e0e-4bd1-9dfb-ca115b005704", :kixi.user/name "Test User", :kixi.user/created "2017-04-07T14:13:46.020Z", :kixi.user/username "test@mastodonc.com"}},
   :kixi.datastore.metadatastore/name "Test File",
   :kixi.datastore.metadatastore/id "5f28728c-5cc7-485d-8563-345fd0a65f2f",
   :kixi.datastore.metadatastore/type "stored",
   :kixi.datastore.metadatastore/description "Test Description"
   :kixi.datastore.metadatastore/author "Some Author"
   :kixi.datastore.metadatastore/maintainer "Some Maintainer"
   :kixi.datastore.metadatastore/source "GLA Demography"
   :kixi.datastore.metadatastore.license/license {:kixi.datastore.metadatastore.license/type "Public Domain"
                                                  :kixi.datastore.metadatastore.license/usage "Some usage for this license"}
   :kixi.datastore.metadatastore.geography/geography {:kixi.datastore.metadatastore.geography/type "smallest"
                                                      :kixi.datastore.metadatastore.geography/level "MSOA"}
   :kixi.datastore.metadatastore.time/temporal-coverage {:kixi.datastore.metadatastore.time/from "2017-04-07T14:13:46.046Z"
                                                         :kixi.datastore.metadatastore.time/to "2018-04-07T14:13:46.046Z"}
   :kixi.datastore.metadatastore/tags #{"London", "GLA", "Open data", "test"}})

(def metadata-keys
  [:kixi.datastore.metadatastore/provenance
   :kixi.datastore.metadatastore/file-type
   :kixi.datastore.metadatastore/size-bytes
   :kixi.datastore.metadatastore/author
   :kixi.datastore.metadatastore/maintainer
   :kixi.datastore.metadatastore/source
   :kixi.datastore.metadatastore.time/temporal-coverage
   :kixi.datastore.metadatastore.license/license
   :kixi.datastore.metadatastore.geography/geography])

(defcard title-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element [title @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/name])
  {:inspect-data true
   :frame true
   :history false})

(defcard description-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element [description @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/description])
  {:inspect-data true
   :frame true
   :history false})

(defcard metadata-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element [metadata @data identity])]))
  (select-keys example-file metadata-keys)
  {:inspect-data true
   :frame true
   :history false})

(defcard tags-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element [tags @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/tags])
  {:inspect-data true
   :frame true
   :history false})

(defcard sharing-display
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element [sharing @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/sharing])
  {:inspect-data true
   :frame true
   :history false})
