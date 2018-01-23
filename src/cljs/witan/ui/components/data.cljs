(ns witan.ui.components.data
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.components.shared :as shared :refer [editable-field]]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [clojure.string :as str]
            [inflections.core :as i]
            [cljsjs.pikaday.with-moment])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(def subview-query-param :d)
(def new-query-param :new)
(def licenses
  ["Creative Commons Attribution"
   "Creative Commons Attribution Share-Alike"
   "Creative Commons CCZero"
   "Creative Commons Non-Commercial (Any)"
   "GNU Free Documentation License"
   "Open Data Commons Attribution License"
   "Open Data Commons Open Database License (ODbL)"
   "Open Data Commons Public Domain Dedication and License (PDDL)"
   "Other (Attribution)"
   "Other (Non-Commercial)"
   "Other (Not Open)"
   "Other (Open)"
   "Other (Public Domain)"
   "UK Open Government Licence (OGL v2)"
   "UK Open Government Licence (OGL v3)"])

(def geographies
  ["Ward"
   "Borough"
   "Local Authority"
   "Output Area"
   "LSOA"
   "MSOA"
   "County"
   "Region"
   "Country"])

(def other-geography
  "Other (please specify)")

(defn bundle?
  [md]
  (= "bundle" (:kixi.datastore.metadatastore/type md)))

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
  [sharing user-sg perms]
  (reduce (fn [a [group activities]]
            (let [new-activities
                  (merge activities
                         (when (= (:kixi.group/id group) user-sg)
                           {:locked (set perms)}))]
              (assoc a group new-activities))) {} sharing))

(defn title
  [{:keys [kixi.datastore.metadatastore/name
           kixi.datastore.metadatastore/file-type] :as md} on-edit-fn]
  [editable-field
   on-edit-fn
   (shared/inline-file-title md :x-large :medium)])

(defn markdown-text
  [text]
  [:div.file-description
   {:dangerouslySetInnerHTML
    {:__html (js/marked text)}}])

(defn description
  [{:keys [kixi.datastore.metadatastore/description]} on-edit-fn]
  [editable-field
   on-edit-fn
   (if (not (clojure.string/blank? description))
     (markdown-text description)
     [:i
      {:class (if on-edit-fn
                "file-description clickable-text"
                "file-description")
       :on-click on-edit-fn}
      (get-string (if on-edit-fn
                    :string/edit-to-add-description
                    :string/no-description))])])
(defn logo
  [{:keys [kixi.datastore.metadatastore/logo]
    :or {kixi.datastore.metadatastore/logo "img/witan-logo-placeholder.png"}
    :as md} on-edit-fn]
  [editable-field
   on-edit-fn
   [:div.file-logo.flex-vcenter-center
    [:img {:src logo}]]])

(defn header-container [md on-edit-fn]
  [:div.flex.data-header-container
   [:div.flex-2-3.flex-columns
    [title md on-edit-fn]
    [description md on-edit-fn]]
   [:div.flex-1-3 [logo md on-edit-fn]]])

(defn download-file
  [id]
  #(set! (.. js/window -location -href)
         (str
          (if (:gateway/secure? @data/config) "https://" "http://")
          (or (:gateway/address @data/config) "localhost:30015")
          "/download?id="
          id)))

(defmulti metadata
  (fn [md _]
    ((juxt :kixi.datastore.metadatastore/type :kixi.datastore.metadatastore/bundle-type) md)))

(defmethod metadata :default
  [_ _])

(defmethod metadata
  ["stored" nil]
  [{:keys [kixi.datastore.metadatastore/provenance
           kixi.datastore.metadatastore/file-type
           kixi.datastore.metadatastore/size-bytes
           kixi.datastore.metadatastore/author
           kixi.datastore.metadatastore/maintainer
           kixi.datastore.metadatastore/source
           kixi.datastore.metadatastore.license/license
           kixi.datastore.metadatastore.time/temporal-coverage
           kixi.datastore.metadatastore.geography/geography
           kixi.datastore.metadatastore/source-created
           kixi.datastore.metadatastore/source-updated]} on-edit-fn]
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
                     (row :string/file-size (fn [] [:span (js/filesize size-bytes)]))
                     ))
        (vec (concat [:tr]
                     (row :string/file-uploader (fn [] [:span (:kixi.user/name prov-created-by)]))
                     (row :string/license-type (fn [] [:span lc-type]))))
        (vec (concat [:tr]
                     (row :string/file-uploaded-at (fn [] [:span (time/iso-time-as-moment prov-created-at)]))
                     (row :string/license-usage (fn [] [:span lc-usage]))))
        (vec (concat [:tr]
                     (row :string/file-provenance-source (fn [] [:span (i/capitalize prov-source)]))
                     (row :string/smallest-geography (fn [] [:span geo-level]))))
        (vec (concat [:tr]
                     (row :string/maintainer (fn [] [:span maintainer]))
                     (row :string/temporal-coverage (fn [] [:span
                                                            (when tc-from (time/iso-date-as-slash-date tc-from))
                                                            " - "
                                                            (when tc-to (time/iso-date-as-slash-date tc-to))]))))
        (vec (concat [:tr]
                     (row :string/author (fn [] [:span author]))
                     (row :string/source-created-at (fn [] [:span (when source-created (time/iso-date-as-slash-date source-created))]))))
        (vec (concat [:tr]
                     (row :string/file-source (fn [] [:span source]))
                     (row :string/source-updated-at (fn [] [:span (when source-updated (time/iso-date-as-slash-date source-updated))]))))]]]]))

(defn total-bundled-size
  [meta]
  (->> meta
       :kixi.datastore.metadatastore/bundled-files
       vals
       (map :kixi.datastore.metadatastore/size-bytes)
       (reduce +)))

(defmethod metadata
  ["bundle" "datapack"]
  [{:keys [kixi.datastore.metadatastore/bundled-ids
           kixi.datastore.metadatastore/provenance] :as meta} on-edit-fn]
  (let [prov-source     (:kixi.datastore.metadatastore/source provenance)
        prov-created-at (:kixi.datastore.metadatastore/created provenance)
        prov-created-by (:kixi/user provenance)
        row             (fn [string-id value-fn]
                          [[:td.row-title [:strong (get-string string-id)]] [:td.row-value (value-fn)]])]
    [editable-field
     on-edit-fn
     [:div.file-metadata-table
      [:table.pure-table.pure-table-bordered.pure-table-odd
       [:tbody
        (vec (concat [:tr]
                     (row :string/creator
                          (fn [] [:span (:kixi.user/name prov-created-by)]))
                     (row :string/datapack-view-num-files
                          (fn [] [:span (count bundled-ids)]))))
        (vec (concat [:tr]
                     (row :string/created-at
                          (fn [] [:span (time/iso-time-as-moment prov-created-at)]))
                     (row :string/datapack-view-total-sized
                          (fn [] [:span (js/filesize (total-bundled-size meta))]))))]]]]))

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

(defn files
  [{:keys [kixi.datastore.metadatastore/bundled-files]} on-edit-fn]
  (let [invisible-files (filter :error (vals bundled-files))
        visible-files (remove :error (vals bundled-files))]
    [editable-field
     on-edit-fn
     [:div.datapack-files
      [:h3 (get-string :string/files)]
      (if-not (empty? visible-files)
        [shared/table
         {:headers [{:content-fn
                     #(vector
                       :div.flex-start
                       (shared/button {:icon icons/download
                                       :id (str (:kixi.datastore.metadatastore/id %) "-download")
                                       :prevent? true
                                       :disabled? (empty? (clojure.set/intersection
                                                           (set (map :kixi.group/id (get-in % [:kixi.datastore.metadatastore/sharing
                                                                                               :kixi.datastore.metadatastore/file-read])))
                                                           (set (data/get-in-app-state :app/user :kixi.user/groups))))}
                                      (download-file (:kixi.datastore.metadatastore/id %)))
                       [:a {:href (str "/#" (route/find-path :app/data {:id (:kixi.datastore.metadatastore/id %)}))}
                        (shared/button {:icon icons/search
                                        :id (str (:kixi.datastore.metadatastore/id %) "-open")}
                                       (fn [_]))])
                     :title ""  :weight "100px"}
                    {:content-fn #(shared/inline-file-title % :small :small)
                     :title (get-string :string/file-name)
                     :weight 0.5}
                    {:content-fn #(js/filesize (:kixi.datastore.metadatastore/size-bytes %))
                     :title (get-string :string/file-size)
                     :weight 0.2}
                    {:content-fn #(or (get-in
                                       %
                                       [:kixi.datastore.metadatastore.license/license
                                        :kixi.datastore.metadatastore.license/type]) (get-string :string/na))
                     :title (get-string :string/license)
                     :weight 0.2}]
          :content visible-files}]
        [:i (get-string :string/no-files-in-datapack)])
      (when-not (empty? invisible-files)
        [:div.invisible-files (gstring/format (get-string :string/datapack-view-invisible-file-count) (count invisible-files))])]]))

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
      [:span (cond
               (zero? unique-count)
               (get-string :string/sharing-summary-only-you)
               (= 1 unique-count)
               (get-string :string/sharing-summary-single)
               :else
               (gstring/format (get-string :string/sharing-summary) unique-count))]]]))

(defn actions
  [current]
  [editable-field
   nil
   [:div.data-actions
    (shared/button {:icon icons/download
                    :id :download
                    :txt :string/file-actions-download-file
                    :prevent? true}
                   (download-file current))]])

(defn sharing-detailed
  [{:keys [kixi.datastore.metadatastore/sharing
           kixi.datastore.metadatastore/type
           kixi.datastore.metadatastore/id] :as md} has-edit?]
  (let [activities->string (if (bundle? md)
                             data/datastore-bundle-activities
                             data/datastore-file-activities)
        locked-activities (if (bundle? md)
                            data/datastore-bundle-default-activity-permissions
                            data/datastore-file-default-activity-permissions)
        user-sg (data/get-in-app-state :app/user :kixi.user/self-group)
        sharing-groups (set (reduce concat [] (vals sharing)))]
    [editable-field
     nil
     [:div.file-sharing-detailed
      [:h2.heading.space-after (get-string :string/sharing)]
      [:div.sharing-activity
       [:div.selected-groups
        [shared/sharing-matrix activities->string
         (-> (keys activities->string)
             (reverse-group->activity-map sharing)
             (lock-activities user-sg locked-activities))
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
                               {:current id :group group}))
          :all-disabled? (not has-edit?)
          :show-search? has-edit?}
         {:exclusions sharing-groups}]]]]]))

(defn confirm-delete
  [md]
  [editable-field
   nil
   [:div.metadata-confirmation
    [:h2  (case (:kixi.datastore.metadatastore/type md)
            "stored" (get-string :string/confirm-delete-file-title)
            "bundle" (get-string :string/confirm-delete-datapack-title))]
    [:div.metadata-confirmation-text
     (case (:kixi.datastore.metadatastore/type md)
       "stored" (get-string :string/confirm-delete-file)
       "bundle" (get-string :string/confirm-delete-datapack))]
    [:div.flex-start
     (shared/button {:id :delete-yes
                     :txt :string/delete-metadata-ok
                     :class "btn-danger"
                     :prevent? true}
                    #(controller/raise!
                      (case (:kixi.datastore.metadatastore/type md)
                        "stored" :data/delete-file
                        "bundle" :data/delete-datapack)
                      {:id (:kixi.datastore.metadatastore/id md)}))
     (shared/button {:id :delete-no
                     :txt :string/delete-metadata-cancel
                     :prevent? true}
                    #(controller/raise! :data/reset-confirm-delete-metadata {}))]


    ]])

(defn input-wrapper
  [& inputs]
  [:form.pure-form
   {:on-submit #(.preventDefault %)}
   (vec (cons :div inputs))])

(defn list-any-errors
  [update-errors ks]
  (when-let [ers (not-empty (select-keys update-errors ks))]
    [:div.file-edit-metadata-error-list
     (for [error ers]
       [:div.file-edit-metadata-error
        {:key (hash error)}
        (icons/close :error :tiny)
        [:span.error error]])]))

(defn edit-title-description
  [_ _]
  (let [editing (r/atom nil)]
    (fn [md update-errors]
      (let [{:keys [kixi.datastore.metadatastore/name
                    kixi.datastore.metadatastore/description
                    kixi.datastore.metadatastore/logo]} md]
        [editable-field
         nil
         [:div.file-edit-metadata
          [:h2.heading (get-string :string/file-sharing-meta-update)]
          (list-any-errors update-errors [:kixi.datastore.metadatastore/name
                                          :kixi.datastore.metadatastore/description
                                          :kixi.datastore.metadatastore/logo])
          (input-wrapper
           [:h3 (get-string :string/file-name)]
           [:input {:id  "title"
                    :type "text"
                    :value name
                    :placeholder nil
                    :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/name] (.. % -target -value)])}]
           [:div.flex-vcenter
            [:h3 (get-string :string/file-description)]
            [:small (get-string :string/supports " ") [:a {:href "http://commonmark.org/help/"
                                                           :target "_blank"} "Markdown"]]]
           [:textarea {:id  "description"
                       :value description
                       :placeholder nil
                       :on-change #(do
                                     (reset! editing (.. % -target -value))
                                     (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/description] (.. % -target -value)]))}]
           (when @editing
             [:div
              [:i {} (get-string :string/preview) ":"]
              [:div.file-description
               (markdown-text @editing)]
              [:hr]])
           [:h3 (get-string :string/meta-image-url)]
           [:input {:id "meta-image-url"
                    :type "text"
                    :value logo
                    :placeholder (get-string :string/meta-image-url-placeholder)
                    :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/logo] (.. % -target -value)])}])]]))))

(defn edit-license
  [md showing-atom licenses update-errors]
  (let [{:keys [kixi.datastore.metadatastore.license/license]} md
        lc-type  (:kixi.datastore.metadatastore.license/type license)
        lc-usage (:kixi.datastore.metadatastore.license/usage license)]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h2.heading (get-string :string/license)]
      (list-any-errors update-errors [:kixi.datastore.metadatastore.license/license])
      (input-wrapper
       [:h3 (get-string :string/type)]
       [:select {:id  "license-type"
                 :type "text"
                 :value lc-type
                 :placeholder nil
                 :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.license/license
                                                                                  :kixi.datastore.metadatastore.license/type] (.. % -target -value)])}
        (for [license (cons "" licenses)]
          [:option {:key license :value license} license])]
       (if @showing-atom
         [:textarea {:id  "license-usage"
                     :value lc-usage
                     :placeholder (get-string :string/license-usage-placeholder)
                     :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.license/license
                                                                                      :kixi.datastore.metadatastore.license/usage] (.. % -target -value)])}]
         [:div.clickable-text
          {:id "license-usage-reveal"
           :on-click #(reset! showing-atom true)}
          (get-string :string/license-usage-reveal)]))]]))

(defn edit-tags
  [md update-errors]
  (let [{:keys [kixi.datastore.metadatastore/tags]} md]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h2.heading.space-after (get-string :string/tags)]
      (list-any-errors update-errors [:kixi.datastore.metadatastore/tags])
      (if (zero? (count tags))
        [:i (get-string :string/no-tags)]
        (for [tag tags]
          (shared/tag tag identity
                      (fn [v]
                        (controller/raise! :data/swap-edit-metadata
                                           [:update-disj [:kixi.datastore.metadatastore/tags] v])))))
      (input-wrapper
       [:div.add-tag-container
        [:input {:id  "add-tag-input"
                 :type "text"
                 :placeholder (get-string :string/add-a-tag)
                 :on-change #()}]
        (shared/button {:icon icons/plus
                        :class "add-tag-button"
                        :id :add-tag} (fn []
                                        (when-let [el (.getElementById js/document "add-tag-input")]
                                          (when-not (clojure.string/blank? (.. el -value))
                                            (let [v (.. el -value)]
                                              (controller/raise! :data/swap-edit-metadata
                                                                 [:update-conj [:kixi.datastore.metadatastore/tags] v]))
                                            (set! (.. el -value) nil)
                                            (.focus el)))))])]]))

(defn date-str->pikaday
  [t]
  (let [[year rest] (split-at 4 t)
        [month day] (split-at 2 rest)]
    (str (apply str day) "/" (apply str month) "/" (apply str year))))

(defn clear-input-button
  [on-click]
  [:span.clickable-text
   {:on-click on-click}
   (icons/close :tiny)])

(defn edit-source
  [md update-errors]
  (let [swap-fn (fn [loc]
                  (fn [v]
                    (let [t (time/jstime->date-str (goog.date.DateTime. v))]
                      (controller/raise! :data/swap-edit-metadata [:assoc loc t]))))
        date-created-el (atom nil)
        date-updated-el (atom nil)]
    (r/create-class
     {:component-did-mount (fn []
                             (let [opts {:format "DD/MM/YYYY"}]
                               (reset! date-created-el
                                       (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "date-created")
                                                                          :onSelect (swap-fn [:kixi.datastore.metadatastore/source-created])}))))
                               (reset! date-updated-el
                                       (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "date-updated")
                                                                          :onSelect (swap-fn [:kixi.datastore.metadatastore/source-updated])}))))))
      :reagent-render (fn [md update-errors]
                        (let [date-created (:kixi.datastore.metadatastore/source-created md)
                              date-updated (:kixi.datastore.metadatastore/source-updated md)
                              {:keys [kixi.datastore.metadatastore/author
                                      kixi.datastore.metadatastore/maintainer
                                      kixi.datastore.metadatastore/source]} md]
                          [editable-field
                           nil
                           [:div.file-edit-metadata
                            [:h2.heading (get-string :string/source-plural)]
                            [:span.intro (get-string :string/file-edit-metadata-source-intro)]
                            (list-any-errors update-errors [:kixi.datastore.metadatastore/author
                                                            :kixi.datastore.metadatastore/maintainer
                                                            :kixi.datastore.metadatastore/source])
                            (input-wrapper
                             [:h4 (get-string :string/author)]
                             [:input {:id  "author"
                                      :type "text"
                                      :value author
                                      :placeholder nil
                                      :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/author] (.. % -target -value)])}]
                             [:h4 (get-string :string/maintainer)]
                             [:input {:id  "maintainer"
                                      :type "text"
                                      :value maintainer
                                      :placeholder nil
                                      :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/maintainer] (.. % -target -value)])}]
                             [:h4 (get-string :string/file-source)]
                             [:input {:id  "source"
                                      :type "text"
                                      :value source
                                      :placeholder nil
                                      :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/source] (.. % -target -value)])}])
                            [:hr]
                            (list-any-errors update-errors [:kixi.datastore.metadatastore/source-created
                                                            :kixi.datastore.metadatastore/source-updated])
                            (input-wrapper
                             [:div.flex.clear-input
                              [:h4 (get-string :string/source-created-at)]
                              (when date-created (clear-input-button
                                                  #(controller/raise! :data/swap-edit-metadata
                                                                      [:dissoc
                                                                       [:kixi.datastore.metadatastore/source-created]])))]
                             [:input {:id  "date-created"
                                      :type "text"
                                      :value (when date-created (date-str->pikaday date-created))
                                      :placeholder nil
                                      :on-change #()}]
                             [:div.flex.clear-input
                              [:h4 (get-string :string/source-updated-at)]
                              (when date-updated (clear-input-button
                                                  #(controller/raise! :data/swap-edit-metadata
                                                                      [:dissoc
                                                                       [:kixi.datastore.metadatastore/source-updated]])))]
                             [:input {:id  "date-updated"
                                      :type "text"
                                      :value (when date-updated (date-str->pikaday date-updated))
                                      :placeholder nil
                                      :on-change #()}])]]))})))
(defn edit-geography-input
  [level on-change]
  [:select {:id  "smallest-geography"
            :type "text"
            :value level
            :placeholder nil
            :on-change #(on-change (.. % -target -value))}
   (for [geography (concat (cons "" geographies) [other-geography])]
     [:option {:key geography :value geography} geography])])

(defn edit-temporal-coverage-and-geography
  [md update-errors]
  (let [other? (r/atom false)
        other-specified (r/atom nil)
        swap-fn (fn [loc]
                  (fn [v]
                    (let [t (time/jstime->date-str (goog.date.DateTime. v))]
                      (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.time/temporal-coverage loc] t]))))
        tc-from-el (atom nil)
        tc-to-el (atom nil)]
    (r/create-class
     {:component-did-mount (fn []
                             (add-watch other? :resetter
                                        (fn [_ a old new]
                                          (reset! other-specified "")))
                             (let [opts {:format "DD/MM/YYYY"}]
                               (reset! tc-from-el (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "tc-from")
                                                                                     :onSelect (swap-fn :kixi.datastore.metadatastore.time/from)}))))
                               (reset! tc-to-el (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "tc-to")
                                                                                   :onSelect (swap-fn :kixi.datastore.metadatastore.time/to)}))))))
      :reagent-render (fn [md update-errors]
                        (let [{:keys [kixi.datastore.metadatastore.time/temporal-coverage]} md
                              tc-from         (:kixi.datastore.metadatastore.time/from temporal-coverage)
                              tc-to           (:kixi.datastore.metadatastore.time/to temporal-coverage)
                              {:keys [kixi.datastore.metadatastore.geography/geography]} md
                              geo-type  (:kixi.datastore.metadatastore.geography/type geography)
                              geo-level (:kixi.datastore.metadatastore.geography/level geography)
                              otherx? (not (or (clojure.string/blank? geo-level)
                                               (contains? (set geographies) geo-level)))]
                          [editable-field
                           nil
                           [:div.file-edit-metadata
                            [:h2.heading (get-string :string/time-and-geog-coverage)]
                            (list-any-errors update-errors [:kixi.datastore.metadatastore.time/temporal-coverage])
                            (input-wrapper
                             [:div.flex.clear-input
                              [:h4 (get-string :string/from)]
                              (when tc-from (clear-input-button
                                             #(controller/raise! :data/swap-edit-metadata
                                                                 [:dissoc
                                                                  [:kixi.datastore.metadatastore.time/temporal-coverage
                                                                   :kixi.datastore.metadatastore.time/from]])))]
                             [:input {:id  "tc-from"
                                      :type "text"
                                      :value (when tc-from (date-str->pikaday tc-from))
                                      :placeholder nil
                                      :on-change #()}]
                             [:div.flex.clear-input
                              [:h4 (get-string :string/to)]
                              (when tc-to (clear-input-button
                                           #(controller/raise! :data/swap-edit-metadata
                                                               [:dissoc
                                                                [:kixi.datastore.metadatastore.time/temporal-coverage
                                                                 :kixi.datastore.metadatastore.time/to]])))]
                             [:input {:id  "tc-to"
                                      :type "text"
                                      :value (when tc-to (date-str->pikaday tc-to))
                                      :placeholder nil
                                      :on-change #()}])
                            [:div.file-edit-geography
                             [:hr]
                             (list-any-errors update-errors [:kixi.datastore.metadatastore.geography/geography])
                             (input-wrapper
                              [:h4 (get-string :string/smallest-geography)]
                              (edit-geography-input
                               (if (or @other? otherx?) other-geography geo-level)
                               #(do
                                  (reset! other? (= % other-geography))
                                  (when-not (= % other-geography)
                                    (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.geography/geography
                                                                                         :kixi.datastore.metadatastore.geography/type] "smallest"])
                                    (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.geography/geography
                                                                                         :kixi.datastore.metadatastore.geography/level] %]))))
                              (when (or @other? otherx?)
                                [:div
                                 [:input {:id  "smallest-geog-txt"
                                          :type "text"
                                          :value (or @other-specified geo-level)
                                          :placeholder nil
                                          :on-change #(do
                                                        (reset! other-specified (.. % -target -value))
                                                        (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.geography/geography
                                                                                                             :kixi.datastore.metadatastore.geography/type] "smallest"])
                                                        (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.geography/geography
                                                                                                             :kixi.datastore.metadatastore.geography/level] (.. % -target -value)]))}]]))]]]))})))

(defn edit-actions
  [md flags update-errors]
  (fn [md flags update-errors]
    (let [saving? (contains? flags :metadata-saving)
          tombstone? (contains? flags :metadata-tombstone)]
      [editable-field
       nil
       [:div.data-edit-actions
        [:div.flex-vcenter
         (shared/button {:icon icons/tick
                         :id :save
                         :txt :string/save
                         :class "btn-success"
                         :prevent? true
                         :disabled? saving?}
                        #(controller/raise! :data/metadata-change md))

         (cond saving?
               [:span.success (get-string :string/saving "...")]
               (not-empty update-errors)
               [:span.error (get-string :string/md-not-saved-due-to-errors)])]
        [:div.flex-vcenter

         (shared/button {:icon icons/delete
                         :id :delete
                         :txt :string/delete
                         :class "btn-danger"
                         :prevent? true
                         :disabled? saving?}
                        (fn [_]
                          (controller/raise! :data/confirm-delete-metadata {})))]]])))

(defn edit-metadata
  [current md]
  (let [lc-usage (get-in md [:kixi.datastore.metadatastore.license/license :kixi.datastore.metadatastore.license/usage])
        show-license-usage (r/atom lc-usage)]
    (controller/raise! :data/reset-edit-metadata current)
    (fn [current md]
      (let [{:keys [flags update-errors]} (data/get-in-app-state :app/datastore :ds/file-properties current)
            local-md (data/get-in-app-state :app/datastore :ds/file-metadata-editing)]
        [:div.file-edit-metadata-content-container
         [edit-title-description local-md update-errors]
         (edit-tags local-md update-errors)
         (when-not (bundle? md)
           [:div
            (edit-license local-md show-license-usage licenses update-errors)
            [:div.file-edit-metadata-container.flex
             {:style {:align-items :stretch}}
             [:div.flex-2
              [edit-source local-md update-errors]]
             [:div.flex-2
              [edit-temporal-coverage-and-geography local-md update-errors]]]])
         [edit-actions local-md flags update-errors]]))))

(defn display-sharing-summary
  [ddatapack]
  (fn [{:keys [kixi.datastore.metadatastore/sharing]}]
    (let [group-names (set (map :kixi.group/name (:kixi.datastore.metadatastore/meta-read sharing)))]
      [shared/collapsible-text (str/join ", "  group-names)])))

(defn edit-files
  [md]
  (let []
    (fn [{:keys [kixi.datastore.metadatastore/bundled-files]}]
      (let [invisible-files (filter :error (vals bundled-files))
            visible-files (remove :error (vals bundled-files))]
        [editable-field
         nil
         [:div.datapack-edit-files
          [:h2.heading (get-string :string/files)]
          [shared/file-search-area
           {:ph :string/edit-datapack-search-files
            :on-click #(controller/raise! :data/add-file-to-datapack {:datapack md
                                                                      :add-file %})
            :on-init #(controller/raise! :data/refresh-files {})
            :on-search #(controller/raise! :data/search-files {:search %})
            :get-results-fn #(data/get-in-app-state :app/datastore :ds/files-search-filtered)
            :selector-key :kixi.datastore.metadatastore/id
            :table-headers-fn (fn []
                                [{:content-fn #(shared/button {:icon icons/tick
                                                               :id (str (:kixi.datastore.metadatastore/id %) "-select")
                                                               :prevent? true}
                                                              identity)
                                  :title ""  :weight 0.10}
                                 {:content-fn #(shared/inline-file-title % :small :small)
                                  :title (get-string :string/file-name)
                                  :weight 0.50}
                                 {:content-fn (comp
                                               :kixi.user/name
                                               :kixi/user
                                               :kixi.datastore.metadatastore/provenance)
                                  :title (get-string :string/file-uploader)
                                  :weight 0.20}
                                 {:content-fn (comp
                                               time/iso-time-as-moment
                                               :kixi.datastore.metadatastore/created
                                               :kixi.datastore.metadatastore/provenance)
                                  :title (get-string :string/file-uploaded-at)
                                  :weight 0.20}])}
           {:exclusions visible-files}]
          (if-not (empty? visible-files)
            [shared/table
             {:headers [{:content-fn
                         #(vector
                           :div.flex-start
                           (shared/button {:icon icons/download
                                           :id (str (:kixi.datastore.metadatastore/id %) "-download")
                                           :prevent? true
                                           :disabled? (empty? (clojure.set/intersection
                                                               (set (map :kixi.group/id (get-in % [:kixi.datastore.metadatastore/sharing
                                                                                                   :kixi.datastore.metadatastore/file-read])))
                                                               (set (data/get-in-app-state :app/user :kixi.user/groups))))}
                                          (download-file (:kixi.datastore.metadatastore/id %)))
                           [:a {:href (str "/#" (route/find-path :app/data {:id (:kixi.datastore.metadatastore/id %)}))}
                            (shared/button {:icon icons/search
                                            :id (str (:kixi.datastore.metadatastore/id %) "-open")}
                                           (fn [_]))]
                           (shared/button {:icon icons/delete
                                           :id (str (:kixi.datastore.metadatastore/id %) "-delete")}
                                          (fn [_]
                                            (controller/raise! :data/remove-file-from-datapack {:datapack md
                                                                                                :remove-file %}))))
                         :title ""  :weight "150px"}
                        {:content-fn #(shared/inline-file-title % :small :small)
                         :title (get-string :string/file-name)
                         :weight 0.4}
                        {:content-fn #(js/filesize (:kixi.datastore.metadatastore/size-bytes %))
                         :title (get-string :string/file-size)
                         :weight 0.15}
                        {:content-fn #(or (get-in
                                           %
                                           [:kixi.datastore.metadatastore.license/license
                                            :kixi.datastore.metadatastore.license/type]) (get-string :string/na))
                         :title (get-string :string/license)
                         :weight 0.15}
                        {:content-fn (display-sharing-summary md)
                         :title (get-string :string/visible-to)
                         :weight 0.2}]
              :content visible-files}]
            [:i (get-string :string/no-files-in-datapack)])
          (when-not (empty? invisible-files)
            [:div.invisible-files (gstring/format (get-string :string/datapack-view-invisible-file-count) (count invisible-files))])]]))))

(defn basic-collect
  [md]
  (let [groups (r/atom #{})
        message (r/atom nil)
        sending? (r/atom false)]
    (fn [md]
      [editable-field
       nil
       [:div.datapack-basic-collect
        [:h2.heading (get-string :string/collect)]
        (shared/info-panel :string/collect-info)
        [:hr]
        [:div
         [shared/group-search-area
          :string/create-rts-user-ph
          #(swap! groups conj %1) {:disabled? @sending?}]
         (when (not-empty @groups)
           (input-wrapper
            [shared/table
             {:headers [{:content-fn
                         #(vector
                           :div.flex-start
                           (shared/button {:icon icons/delete
                                           :disabled? @sending?
                                           :id (str (:kixi.group/id %) "-remove")}
                                          (fn [_]
                                            (swap! groups disj %))))
                         :title ""  :weight 0.1}
                        {:content-fn shared/inline-group
                         :title (get-string :string/name)
                         :title-align :left
                         :weight 0.9}]
              :content @groups}]
            [:div.flex-vcenter
             [:h3 (get-string :string/message)]
             [:small (get-string :string/supports " ") [:a {:href "http://commonmark.org/help/"
                                                            :target "_blank"} "Markdown"]]]
            [:textarea {:id  "description"
                        :value @message
                        :placeholder (get-string :string/collect-message-ph)
                        :disabled (when @sending? :disabled)
                        :on-change #(reset! message (.. % -target -value))}]
            [:div.flex-vcenter-start
             (shared/button {:id :send-cas-request
                             :_id :send-cas-request
                             :txt :string/collect-send-request
                             :prevent? true
                             :disabled? (or @sending? (clojure.string/blank? @message))}
                            (fn [_]
                              (reset! sending? true)
                              (controller/raise! :data/send-basic-collect-request {:groups @groups
                                                                                   :message :message})))
             (when @sending? [:span (get-string :string/sending "...")])]))]]])))

(def tabs
  [[0 :overview]
   [1 :sharing]
   [2 :edit]
   [3 :files]
   ;;   [4 :collect]
   ])

(defn idx->tab
  [i]
  (get (into {} tabs) i :overview))

(defn tab->idx
  [i]
  (get (zipmap (map second tabs) (map first tabs)) i 0))

(defn switch-primary-view!
  [k]
  (let [i (tab->idx k)]
    (route/swap-query-string! #(assoc % subview-query-param i))
    (controller/raise! :data/switch-data-view-subview-idx {:idx i})))

(defn md->tab-config
  [md has-edit?]
  (cond
    (= "stored" (:kixi.datastore.metadatastore/type md))
    (if has-edit?
      {:overview (get-string :string/overview)
       :sharing (get-string :string/sharing)
       :edit (get-string :string/edit)}
      {:overview (get-string :string/overview)
       :sharing (get-string :string/sharing)})
    (= "datapack" (:kixi.datastore.metadatastore/bundle-type md))
    (if has-edit?
      {:overview (get-string :string/overview)
       :files (get-string :string/files)
       ;; :collect (get-string :string/collect)
       :sharing (get-string :string/sharing)
       :edit (get-string :string/edit)}
      {:overview (get-string :string/overview)
       :sharing (get-string :string/sharing)})
    :else {}))

;;

(defn view
  []
  (let [new? (r/atom (= 1 (or (utils/query-param-int new-query-param 0 1) 0)))]
    (fn []
      (let [subview-tab (idx->tab
                         (data/get-in-app-state :app/datastore :ds/data-view-subview-idx))
            {:keys [ds/current ds/download-pending? ds/error] :as ds}
            (data/get-in-app-state :app/datastore)
            activities->string data/datastore-file-activities
            md (data/get-in-app-state :app/datastore :ds/file-metadata current)
            confirming-delete? (data/get-in-app-state :app/datastore :ds/confirming-delete?)
            has-edit? (utils/user-has-edit? (data/get-user) md)
            can-download? (utils/user-has-download? (data/get-user) md)
            remove-new-fn (fn []
                            (route/swap-query-string! (fn [x] (dissoc x :new)))
                            (reset! new? false))
            go-to-edit (when has-edit? (fn []
                                         (remove-new-fn)
                                         (switch-primary-view! :edit)))
            go-to-files (when has-edit? (fn []
                                          (remove-new-fn)
                                          (switch-primary-view! :files)))
            go-to-sharing (when has-edit? (fn []
                                            (remove-new-fn)
                                            (switch-primary-view! :sharing)))]
        (cond
          error
          [:div.text-center.padded-content
           [:div
            (icons/error :dark :large)]
           [:div [:h3 (get-string error)]]]

          (not md)
          [:div.loading
           (icons/loading :large)]

          :else
          [:div#data-view
           (shared/header-string (:kixi.datastore.metadatastore/name md)
                                 ""
                                 {:class (if (bundle? md)
                                           "header-bg-bundle"
                                           "header-bg-file")})
           (when-not confirming-delete?
             (shared/tabs {:tabs (md->tab-config md has-edit?)
                           :selected-tab subview-tab
                           :on-click switch-primary-view!}))
           [:div.flex-center
            (if confirming-delete?
              [:div.container.padded-content
               (confirm-delete md)]
              [:div.container.padded-content
               (when @new?
                 [:div.hero-notification
                  [:div.hero-close
                   {:on-click remove-new-fn}
                   (icons/close)]
                  (icons/tick :success)
                  [:div.hero-content
                   (case (:kixi.datastore.metadatastore/type md)
                     "stored" (get-string :string/new-upload-file-information-hero)
                     "bundle" (get-string :string/new-upload-bundle-information-hero))
                   [:div
                    [:i.clickable-text
                     {:on-click go-to-edit}
                     (case (:kixi.datastore.metadatastore/type md)
                       "stored" (get-string :string/click-here-to-edit-file-metadata)
                       "bundle" (get-string :string/click-here-to-edit-bundle-metadata))]]]])
               (case subview-tab
                 :sharing (sharing-detailed md has-edit?)
                 :edit [edit-metadata current md]
                 :files [edit-files md]
                 :collect [basic-collect md]
                 ;; :overview & default
                 [:div
                  (header-container md go-to-edit)
                  (metadata md go-to-edit)
                  (sharing md go-to-sharing)
                  (tags md go-to-edit)
                  (when (bundle? md) (files md go-to-files))
                  (when can-download? (actions current))])])]])))))

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
   :kixi.datastore.metadatastore/logo "https://tfl.gov.uk/cdn/static/cms/images/boroughs-map-static.gif"
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
     [:div#data-view
      {:style {:width "100%"}}
      (r/as-element [title @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/name
                             :kixi.datastore.metadatastore/file-type])
  {:inspect-data true
   :frame true
   :history false})

(defcard description-display
  (fn [data _]
    (sab/html
     [:div#data-view
      {:style {:width "100%"}}
      (r/as-element [description @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/description])
  {:inspect-data true
   :frame true
   :history false})

(defcard header-container
  (fn [data _]
    (sab/html
     [:div#data-view
      {:style {:width "100%"}}
      (r/as-element [header-container @data identity])]
     ))
  example-file
  {:inspect-data false
   :frame true
   :history false})

(defcard header-container-long
  (fn [data _]
    (sab/html
     [:div#data-view
      {:style {:width "100%"}}
      (r/as-element [header-container @data identity])]
     ))
  (assoc example-file :kixi.datastore.metadatastore/description (repeat 100 "foobar "))
  {:inspect-data false
   :frame true
   :history false})

(defcard logo
  (fn [data _]
    (sab/html
     [:div#data-view
      {:style {:width "100%"}}
      (r/as-element [logo @data identity])]))
  (select-keys example-file [:kixi.datastore.metadatastore/logo])
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
