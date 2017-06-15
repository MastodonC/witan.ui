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
   (if (not (clojure.string/blank? description))
     [:span.file-description description]
     [:i
      {:class (if on-edit-fn
                "file-description clickable-text"
                "file-description")
       :on-click on-edit-fn}
      (get-string (if on-edit-fn
                    :string/edit-to-add-description
                    :string/no-description))])])

(defn metadata
  [{:keys [kixi.datastore.metadatastore/provenance
           kixi.datastore.metadatastore/file-type
           kixi.datastore.metadatastore/size-bytes
           kixi.datastore.metadatastore/author
           kixi.datastore.metadatastore/maintainer
           kixi.datastore.metadatastore/source
           kixi.datastore.metadatastore.license/license
           kixi.datastore.metadatastore.time/temporal-coverage
           kixi.datastore.metadatastore.geography/geography
           kixi.datastore.metadatastore/source-created-at
           kixi.datastore.metadatastore/source-updated-at]} on-edit-fn]
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
                     (row :string/source-created-at (fn [] [:span source-created-at]))))
        (vec (concat [:tr]
                     (row :string/file-source (fn [] [:span source]))
                     (row :string/source-updated-at (fn [] [:span source-updated-at]))))]]]]))

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
  [{:keys [kixi.datastore.metadatastore/sharing kixi.datastore.metadatastore/id]} has-edit?]
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
                               {:current id :group group}))
          :all-disabled? (not has-edit?)
          :show-search? has-edit?}
         {:exclusions sharing-groups}]]]]]))

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
  [md update-errors]
  (let [{:keys [kixi.datastore.metadatastore/name
                kixi.datastore.metadatastore/description]} md]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h2.heading (get-string :string/file-sharing-meta-update)]
      (list-any-errors update-errors [:kixi.datastore.metadatastore/name :kixi.datastore.metadatastore/description])
      (input-wrapper
       [:h3 (get-string :string/file-name)]
       [:input {:id  "title"
                :type "text"
                :value name
                :placeholder nil
                :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/name] (.. % -target -value)])}]
       [:h3 (get-string :string/file-description)]
       [:textarea {:id  "description"
                   :value description
                   :placeholder nil
                   :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/description] (.. % -target -value)])}])]]))

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
      [:h2.heading (get-string :string/tags)]
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

(defn edit-temporal-coverage
  [md update-errors]
  (let [swap-fn (fn [loc]
                  (fn [v]
                    (let [t (time/jstime->date-str (goog.date.DateTime. v))]
                      (controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore.time/temporal-coverage loc] t]))))
        tc-from-el (atom nil)
        tc-to-el (atom nil)]
    (r/create-class
     {:component-did-mount (fn []
                             (let [opts {:format "DD/MM/YYYY"}]
                               (reset! tc-from-el (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "tc-from")
                                                                                     :onSelect (swap-fn :kixi.datastore.metadatastore.time/from)}))))
                               (reset! tc-to-el (js/Pikaday. (clj->js (merge opts {:field (.getElementById js/document "tc-to")
                                                                                   :onSelect (swap-fn :kixi.datastore.metadatastore.time/to)}))))))
      :reagent-render (fn [md update-errors]
                        (let [{:keys [kixi.datastore.metadatastore.time/temporal-coverage]} md
                              tc-from         (:kixi.datastore.metadatastore.time/from temporal-coverage)
                              tc-to           (:kixi.datastore.metadatastore.time/to temporal-coverage)]
                          [editable-field
                           nil
                           [:div.file-edit-metadata
                            [:h3.heading (get-string :string/temporal-coverage)]
                            (list-any-errors update-errors [:kixi.datastore.metadatastore.time/temporal-coverage])
                            (input-wrapper
                             [:h4 (get-string :string/from)]
                             [:input {:id  "tc-from"
                                      :type "text"
                                      :value (when tc-from (date-str->pikaday tc-from))
                                      :placeholder nil
                                      :on-change #()}]
                             [:h4 (get-string :string/to)]
                             [:input {:id  "tc-to"
                                      :type "text"
                                      :value (when tc-to (date-str->pikaday tc-to))
                                      :placeholder nil
                                      :on-change #()}])]]))})))

(defn edit-source-dates
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
                              date-updated (:kixi.datastore.metadatastore/source-updated md)]
                          [editable-field
                           nil
                           [:div.file-edit-metadata
                            [:h3.heading (get-string :string/source-dates)]
                            (list-any-errors update-errors [:kixi.datastore.metadatastore/source-created
                                                            :kixi.datastore.metadatastore/source-updated])
                            (input-wrapper
                             [:h4 (get-string :string/source-created-at)]
                             [:input {:id  "date-created"
                                      :type "text"
                                      :value (when date-created (date-str->pikaday date-created))
                                      :placeholder nil
                                      :on-change #()}]
                             [:h4 (get-string :string/source-updated-at)]
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

(defn edit-geography
  [md update-errors]
  (let [other? (r/atom false)
        other-specified (r/atom nil)]
    (add-watch other? :resetter
               (fn [_ a old new]
                 (reset! other-specified "")))
    (fn [md update-errors]
      (let [{:keys [kixi.datastore.metadatastore.geography/geography]} md
            geo-type  (:kixi.datastore.metadatastore.geography/type geography)
            geo-level (:kixi.datastore.metadatastore.geography/level geography)]
        (let [otherx? (not (or (clojure.string/blank? geo-level)
                               (contains? (set geographies) geo-level)))]
          [editable-field
           nil
           [:div.file-edit-metadata.file-edit-geography
            [:h3.heading (get-string :string/geography)]
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
                                                                                            :kixi.datastore.metadatastore.geography/level] (.. % -target -value)]))}]]))]])))))

(defn edit-sources
  [md update-errors]
  (let [{:keys [kixi.datastore.metadatastore/author
                kixi.datastore.metadatastore/maintainer
                kixi.datastore.metadatastore/source]} md]
    [editable-field
     nil
     [:div.file-edit-metadata
      [:h3.heading (get-string :string/source-plural)]
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
                :on-change #(controller/raise! :data/swap-edit-metadata [:assoc [:kixi.datastore.metadatastore/source] (.. % -target -value)])}])]]))

(defn edit-actions
  [md flags update-errors]
  (let [saving? (contains? flags :metadata-saving)]
    [editable-field
     nil
     [:div.file-actions
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
            [:span.error (get-string :string/md-not-saved-due-to-errors)])]]))

(defn edit-metadata
  [current md]
  (let [lc-usage (get-in md [:kixi.datastore.metadatastore.license/license :kixi.datastore.metadatastore.license/usage])
        show-license-usage (r/atom lc-usage)]
    (controller/raise! :data/reset-edit-metadata current)
    (fn [current md]
      (let [{:keys [flags update-errors]} (data/get-in-app-state :app/datastore :ds/file-properties current)
            local-md (data/get-in-app-state :app/datastore :ds/file-metadata-editing)]
        [:div.file-edit-metadata-content-container
         (edit-title-description local-md update-errors)
         (edit-tags local-md update-errors)
         (edit-license local-md show-license-usage licenses update-errors)
         [:div.file-edit-metadata-container.flex
          {:style {:align-items :stretch}}
          [:div.flex-3
           [edit-source-dates local-md update-errors]
           [edit-geography local-md update-errors]]
          [:div.flex-3
           (edit-sources local-md update-errors)]
          [:div.flex-3
           [edit-temporal-coverage local-md update-errors]]]
         (edit-actions local-md flags update-errors)]))))

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
    (route/swap-query-string! #(assoc % subview-query-param i))
    (reset! subview-tab k)))

(defn user-has-permission?
  [permission user file-metadata]
  (let [ug (:kixi.user/groups user)
        vg (set (map :kixi.group/id (get-in file-metadata [:kixi.datastore.metadatastore/sharing permission])))]
    (some vg ug)))

(def user-has-edit?
  (partial user-has-permission? :kixi.datastore.metadatastore/meta-update))

(def user-has-download?
  (partial user-has-permission? :kixi.datastore.metadatastore/file-read))

;;


(defn view
  []
  (reset! subview-tab (idx->tab (or (utils/query-param-int subview-query-param 0 2) 0)))
  (let [new? (r/atom (= 1 (or (utils/query-param-int new-query-param 0 1) 0)))]
    (fn []
      (let [{:keys [ds/current ds/download-pending? ds/error] :as ds}
            (data/get-in-app-state :app/datastore)
            activities->string (:ds/activities ds)
            md (data/get-in-app-state :app/datastore :ds/file-metadata current)
            has-edit? (user-has-edit? (data/get-in-app-state :app/user) md)
            can-download? (user-has-download? (data/get-in-app-state :app/user) md)
            remove-new-fn (fn []
                            (route/swap-query-string! (fn [x] (dissoc x :new)))
                            (reset! new? false))
            go-to-edit (when has-edit? (fn []
                                         (remove-new-fn)
                                         (switch-primary-view! :edit)))
            go-to-sharing (when has-edit? (fn []
                                            (remove-new-fn)
                                            (switch-primary-view! :sharing)))]
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
             (shared/tabs {:tabs (if has-edit?
                                   {:overview (get-string :string/overview)
                                    :sharing (get-string :string/sharing)
                                    :edit (get-string :string/edit)}
                                   {:overview (get-string :string/overview)
                                    :sharing (get-string :string/sharing)})
                           :selected-tab @subview-tab
                           :on-click switch-primary-view!})
             [:div.flex-center
              [:div.container.padded-content
               (when @new?
                 [:div.hero-notification
                  [:div.hero-close
                   {:on-click remove-new-fn}
                   (icons/close)]
                  (icons/tick :success)
                  [:div.hero-content
                   (get-string :string/new-upload-information-hero)
                   [:div
                    [:i.clickable-text
                     {:on-click go-to-edit}
                     (get-string :string/click-here-to-edit-metadata)]]]])
               (case @subview-tab
                 :sharing (sharing-detailed md has-edit?)
                 :edit [edit-metadata current md]
                 ;; :overview & default
                 [:div
                  (title md go-to-edit)
                  (description md go-to-edit)
                  (metadata md go-to-edit)
                  (tags md go-to-edit)
                  (sharing md go-to-sharing)
                  (when can-download? (actions))])]]]))))))

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
