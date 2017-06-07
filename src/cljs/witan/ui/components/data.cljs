(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [cljsjs.pikaday.with-moment])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(def query-param :d)
(defonce subview-tab (r/atom :overview))

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
                     [:span.clickable-text.edit-label
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
                          [[:td.row-title [:strong (get-string string-id)]] [:td.row-value (value-fn)]])]
    [editable-field
     on-edit-fn
     [:div.file-metadata-table
      [:table.pure-table.pure-table-bordered.pure-table-odd
       [:tbody
        (vec (concat [:tr]
                     (row :string/file-type (fn [] [:span file-type]))
                     (row :string/file-provenance-source (fn [] [:span prov-source]))))
        (vec (concat [:tr]
                     (row :string/file-uploader (fn [] [:span (:kixi.user/name prov-created-by)]))
                     (row :string/license-type (fn [] [:span lc-type]))))
        (vec (concat [:tr]
                     (row :string/author (fn [] [:span author]))
                     (row :string/license-usage (fn [] [:span lc-usage]))))
        (vec (concat [:tr]
                     (row :string/source (fn [] [:span source]))
                     (row :string/smallest-geography (fn [] [:span geo-level]))))
        (vec (concat [:tr]
                     (row :string/maintainer (fn [] [:span maintainer]))
                     (row :string/temporal-coverage (fn [] [:span (when tc-from (time/iso-time-as-moment tc-from)) " - " (when tc-to (time/iso-time-as-moment tc-to))]))))
        (vec (concat [:tr]
                     (row :string/created-at (fn [] [:span (time/iso-time-as-moment prov-created-at)]))
                     (row :string/file-size (fn [] [:span (js/filesize size-bytes)]))))]]]]))

(defn tags
  [{:keys [kixi.datastore.metadatastore/tags]} on-edit-fn]
  [editable-field
   on-edit-fn
   [:div.file-tags
    [:h3 (get-string :string/tags)]
    (if (zero? (count tags))
      [:i (get-string :string/no-tags)]
      (for [tag tags]
        (shared/tag tag identity)))]])

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

(defn sharing-detailed
  [{:keys [kixi.datastore.metadatastore/sharing kixi.datastore.metadatastore/id]}]
  (let [activities->string (data/get-in-app-state :app/datastore :ds/activities)
        user-sg (data/get-in-app-state :app/user :kixi.user/self-group)
        sharing-groups (set (reduce concat [] (vals sharing)))]
    [editable-field
     nil
     [:div.file-sharing-detailed
      [:h2.heading (get-string :string/sharing)]
      [:div.sharing-activity
       [:div.selected-groups
        [shared/sharing-matrix activities->string
         (-> (keys activities->string)
             (reverse-group->activity-map sharing)
             (lock-activities user-sg))
         {:on-change
          (fn [[group activities] activity target-state]
            (controller/raise! :data/sharing-change
                               {:current id
                                :group group
                                :activity activity
                                :target-state target-state}))
          :on-add
          (fn [group]
            (controller/raise! :data/sharing-add-group
                               {:current id :group group}))}
         {:exclusions sharing-groups}]]]]]))

(defn input-wrapper
  [& inputs]
  [:form.pure-form
   {:on-submit #(.preventDefault %)}
   (vec (cons :div inputs))])

(defn edit-title-description
  [{:keys [kixi.datastore.metadatastore/name
           kixi.datastore.metadatastore/description] :as md}]
  [editable-field
   nil
   [:div.file-edit-metadata
    [:h2.heading (get-string :string/file-sharing-meta-update)]
    (input-wrapper
     [:h3 (get-string :string/file-name)]
     [:input {:id  "title"
              :type "text"
              :value name
              :placeholder nil
              :on-change #()}]
     [:h3 (get-string :string/file-description)]
     [:textarea {:id  "description"
                 :value description
                 :placeholder nil
                 :on-change #()}])]])

(defn edit-license
  [{:keys [kixi.datastore.metadatastore.license/license] :as md} showing-atom]
  (let [lc-type  (:kixi.datastore.metadatastore.license/type license)
        lc-usage (:kixi.datastore.metadatastore.license/usage license)]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h2.heading (get-string :string/license)]
      (input-wrapper
       [:h3 (get-string :string/type)]
       [:input {:id  "license-type"
                :type "text"
                :value lc-type
                :placeholder nil
                :on-change #()}]
       (if @showing-atom
         [:textarea {:id  "license-usage"
                     :value lc-usage
                     :placeholder (get-string :string/license-usage-placeholder)
                     :on-change #()}]
         [:span.clickable-text
          {:id "license-usage-reveal"
           :on-click #(reset! showing-atom true)}
          (get-string :string/license-usage-reveal)]))]]))

(defn edit-tags
  [{:keys [kixi.datastore.metadatastore/tags]}]
  [editable-field
   nil
   [:div.file-edit-metadata
    [:h2.heading (get-string :string/tags)]
    (if (zero? (count tags))
      [:i (get-string :string/no-tags)]
      (for [tag tags]
        (shared/tag tag identity)))
    (input-wrapper
     [:div.add-tag-container
      [:input {:id  "add-tag"
               :type "text"
               :placeholder (get-string :string/add-a-tag)
               :on-change #()}]
      (shared/button {:icon icons/plus
                      :class "add-tag-button"
                      :id :add-tag} #())])]])

(defn edit-temporal-coverage
  [md]
  (let []
    (r/create-class
     {:component-did-mount (fn []
                             (js/Pikaday. #js {:field (.getElementById js/document "tc-from")})
                             (js/Pikaday. #js {:field (.getElementById js/document "tc-to")}))
      :reagent-render (fn [{:keys [kixi.datastore.metadatastore.time/temporal-coverage]}]
                        (let [tc-from         (:kixi.datastore.metadatastore.time/from temporal-coverage)
                              tc-to           (:kixi.datastore.metadatastore.time/to temporal-coverage)]
                          [editable-field
                           nil
                           [:div.file-edit-metadata
                            [:h3.heading (get-string :string/temporal-coverage)]
                            (input-wrapper
                             [:h4 (get-string :string/from)]
                             [:input {:id  "tc-from"
                                      :type "text"
                                      :value tc-from
                                      :placeholder nil
                                      :on-change #()}]
                             [:h4 (get-string :string/to)]
                             [:input {:id  "tc-to"
                                      :type "text"
                                      :value tc-to
                                      :placeholder nil
                                      :on-change #()}])]]))})))

(defn edit-geography
  [{:keys [kixi.datastore.metadatastore.geography/geography]}]
  (let [geo-type        (:kixi.datastore.metadatastore.geography/type geography)
        geo-level       (:kixi.datastore.metadatastore.geography/level geography)]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h3.heading (get-string :string/geography)]
      (input-wrapper
       [:h4 (get-string :string/smallest-geography)]
       [:input {:id  "smallest-geography"
                :type "text"
                :value geo-level
                :placeholder nil
                :on-change #()}])]]))

(defn edit-sources
  [{:keys [kixi.datastore.metadatastore/author
           kixi.datastore.metadatastore/maintainer]}]
  [editable-field
   nil
   [:div.file-edit-metadata
    [:h3.heading (get-string :string/source-plural)]
    (input-wrapper
     [:h4 (get-string :string/author)]
     [:input {:id  "author"
              :type "text"
              :value author
              :placeholder nil
              :on-change #()}]
     [:h4 (get-string :string/maintainer)]
     [:input {:id  "maintainer"
              :type "text"
              :value maintainer
              :placeholder nil
              :on-change #()}])]])

(defn edit-metadata
  [md]
  (let [lc-usage (get-in md [:kixi.datastore.metadatastore.license/license :kixi.datastore.metadatastore.license/usage])
        show-license-usage (r/atom lc-usage)]
    (fn [md]
      [:div.file-edit-metadata-container
       (edit-title-description md)
       (edit-tags md)
       (edit-license md show-license-usage)
       [:div.flex
        {:style {:align-items :stretch}}
        (edit-sources md)
        [edit-temporal-coverage]
        (edit-geography md)]])))

(def tabs
  [[0 :overview]
   [1 :sharing]
   [2 :edit]])

(defn idx->tab
  [i]
  (get (into {} tabs) i))

(defn tab->idx
  [i]
  (get (zipmap (map second tabs) (map first tabs)) i))

(defn switch-primary-view!
  [k]
  (let [i (tab->idx k)]
    (route/swap-query-string! #(assoc % query-param i))
    (reset! subview-tab k)))

;;


(defn view
  []
  (reset! subview-tab (idx->tab (or (utils/query-param-int query-param 0 2) 0)))
  (fn []
    (let [{:keys [ds/current ds/download-pending? ds/error] :as ds}
          (data/get-in-app-state :app/datastore)
          activities->string (:ds/activities ds)
          md (data/get-in-app-state :app/datastore :ds/file-metadata current)
          go-to-edit (partial switch-primary-view! :edit)
          go-to-sharing (partial switch-primary-view! :sharing)]
      (if error
        [:div.text-center.padded-content
         [:div
          (icons/error :dark :large)]
         [:div [:h3 (get-string error)]]]
        (if-not md
          [:div.loading
           (icons/loading :large)]
          [:div#data-view
           (shared/header-string (:kixi.datastore.metadatastore/name md))
           (shared/tabs {:tabs {:overview (get-string :string/overview)
                                :sharing (get-string :string/sharing)
                                :edit (get-string :string/edit)}
                         :selected-tab @subview-tab
                         :on-click switch-primary-view!})
           [:div.flex-center
            [:div.container.padded-content
             (case @subview-tab
               :sharing (sharing-detailed md)
               :edit [edit-metadata md]
               ;; :overview & default
               [:div
                (title md go-to-edit)
                (description md go-to-edit)
                (metadata md go-to-edit)
                (tags md go-to-edit)
                (sharing md go-to-sharing)
                (actions)])]]])))))

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
