(ns witan.ui.components.shared
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.data :as data]
            [witan.ui.utils :as utils]
            [goog.string :as gstring]
            [witan.ui.controller :as controller]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.components.icons :as icons])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]
   [cljs.core.async.macros :refer [go]]))

(defn search-filter
  [placeholder on-input & [{:keys [id disabled? current-search-value]}]]
  [:div.shared-search-input
   [:form.pure-form
    {:key "search-filter-form"
     :on-submit #(.preventDefault %)}
    [:div
     {:key "search-filter-inner-div"}
     (icons/search)
     [:input.pure-input-rounded
      (merge {:key "filter-input"
              :id (or id "filter-input")
              :type "text"
              :placeholder placeholder
              :disabled disabled?
              :style {:padding-left "30px"}
              :on-input (fn [e]
                          (if (fn? on-input)
                            (on-input (.. e -target -value)))
                          (.preventDefault e))}
             (when current-search-value
               {:value current-search-value}))]]]])

(defn table
  [{:keys [headers content selected?-fn on-select on-double-click on-scroll id class]
    :or {id (str (random-uuid))}}]
  [:div.shared-table
   {:id id
    :on-scroll (fn [e]
                 (when on-scroll
                   (let [el (.getElementById js/document id)
                         d (/ (.-scrollTop el) (- (.-scrollHeight el) (.-offsetHeight el)))]
                     (on-scroll d))))}
   [:table.pure-table.pure-table-horizontal.shared-table-headers
    [:thead
     [:tr
      (doall
       (for [{:keys [title weight title-align]} headers]
         (let [style (merge (if (string? weight)
                              {:min-width weight
                               :width weight}
                              {:width (str (* 100 weight) "%")})
                            (when title-align
                              {:text-align title-align}))]
           [:th {:key title
                 :style style} title])))]]]
   (if-not content
     [:div#loading.text-center (icons/loading :large)]
     [:table.pure-table.pure-table-horizontal.shared-table-rows
      [:tbody
       (doall
        (for [[i row] (map-indexed vector content)]
          [:tr
           {:key (str id "--" i)
            :class (when (and selected?-fn (selected?-fn row)) "selected")}
           (doall
            (for [{:keys [content-fn title weight]} headers]
              (let [percent (if (string? weight)
                              weight
                              (str (* 100 weight) "%"))]
                [:td {:style {:width percent}
                      :key (apply str row title)

                      :on-click (fn [e] (when on-select
                                          (on-select row)))
                      :on-double-click (fn [e] (when on-double-click
                                                 (on-double-click row)))}
                 (when content-fn (content-fn row))])))]))]])])

(defn header-string
  ([title-string]
   (header-string title-string nil))
  ([title-string subtitle-string]
   (header-string title-string nil nil))
  ([title-string subtitle-string opts]
   [:div.shared-heading
    {:key "heading"
     :class (str
             (:class opts)
             " "
             (when (contains? opts :center) "center-string"))}
    [:h1 title-string]
    (when subtitle-string
      [:h2 subtitle-string])]))

(defn header
  ([title]
   (header-string (get-string title) nil))
  ([title subtitle]
   (header-string (get-string title) (when subtitle (get-string subtitle))))
  ([title subtitle opts]
   (header-string (get-string title) (when subtitle (get-string subtitle)) opts)))

(defn button
  [{:keys [icon txt class id prevent? disabled? _id]} on-button-click]
  [:div.button-container
   {:key id}
   [:button.pure-button
    {:class class
     :id _id
     :key (str "button-" (name id))
     :disabled disabled?
     :on-click #(do
                  (when on-button-click (on-button-click id))
                  (when prevent? (.preventDefault %)))}
    (when icon (icon :small))
    (when txt
      [:span
       {:key (str "txt-" (name id))}
       (if (keyword? txt)
         (get-string txt)
         txt)])]])

(defn checkbox
  [{:keys [txt class id prevent? disabled? checked on-change]}]
  [:div.shared-checkbox
   [:label
    {:for id
     :class "pure-checkbox"}
    [:input {:type "checkbox"
             :id id
             :checked checked
             :on-change #(let [new-value (.-checked (.-target %))]
                           (on-change new-value))}
     txt]]])

(defn inline-group
  [{:keys [kixi.group/name kixi.group/type kixi.group/id]}]
  (let [you? (contains? (set (data/get-in-app-state :app/user :kixi.user/groups)) id)]
    [:div.shared-inline-group
     (let [icon-fn (condp = (str type)
                     "user" icons/user
                     "group" icons/organisation
                     icons/help)]
       [:div.group-icon
        (icon-fn :small :dark)])
     [:span name]
     (when you? [:span.you {:title (get-string :string/this-is-you)}
                 (icons/star :tiny :dark)])]))

(defn inline-schema
  [{:keys [schema/name]}]
  [:div.shared-inline-schema
   [:span name]])

(defn inline-file-title
  [{:keys [kixi.datastore.metadatastore/name
           kixi.datastore.metadatastore/file-type
           kixi.datastore.metadatastore/bundle-type]} size-text size-icon]
  (let [size-el (condp = size-text
                  :div     :div
                  :span    :span
                  :tiny    :h5
                  :small   :h4
                  :medium  :h3
                  :large   :h2
                  :x-large :h1)]
    [:div.shared-inline-file-title
     (cond
       file-type   (icons/file-type file-type size-icon)
       bundle-type (icons/bundle-type bundle-type size-icon))
     [size-el name]]))

(defn index
  [data group-by-key render-fn]
  (let [groups (-> (comp first group-by-key)
                   (group-by data)
                   (into {}))
        alphabet (mapv char (range 65 91))
        id (random-uuid)
        gen-index-id (fn [a] (str id "-" a))]
    [:div.shared-index
     [:div.alpha-header
      (for [letter alphabet]
        [:span {:key letter
                :class (when (contains? groups letter) "alpha-header-clickable")
                :on-click (when (contains? groups letter)
                            #(.scrollIntoView
                              (.getElementById js/document (gen-index-id letter))))}
         letter])]
     [:hr]
     [:div.alpha-index
      (for [letter (sort (keys groups))]
        [:div.letter
         {:name letter
          :key letter}
         [:h1
          {:id (gen-index-id letter)}
          letter]
         [:ul
          (for [item (get groups letter)]
            ^{:key item}
            [:li (render-fn item)])]])]]))

(defn info-panel
  [message-k]
  [:div.shared-info-panel
   {:key "shared-info-panel"}
   [:div.icon
    {:key "icon"}
    (icons/help :medium :info)]
   [:div.message
    {:key "message"}
    (get-string message-k)]])

(defn schema-search-area
  [ph on-click & opts]
  (let [show-breakout? (r/atom false)
        selected-schema (r/atom nil)
        {:keys [id disabled?]
         :or {id (str "schema-search-field-"ph)
              disabled? false}} (first opts)]
    (fn [ph on-click & opts]
      (let [results (:schema/search-results (data/get-app-state :app/datastore))
            close-fn (fn [& _]
                       (aset (.getElementById js/document id) "value" nil)
                       (reset! show-breakout? false))
            select-fn (fn [final? schema]
                        (reset! selected-schema schema)
                        (on-click schema final?)
                        (when final?
                          (close-fn)))]
        [:div.shared-schema-search-area
         (search-filter (get-string ph)
                        #(if (clojure.string/blank? %)
                           (reset! show-breakout? false)
                           (do (reset! show-breakout? true)
                               (controller/raise! :data/search-schema {:search %})))
                        {:id id
                         :disabled? disabled?})
         [:div.breakout-area
          {:style {:height (if @show-breakout? "300px" "0px")}}
          (table {:headers [{:content-fn #(button {:icon icons/tick
                                                   :id (:schema/id %)
                                                   :prevent? true}
                                                  (fn [_] (select-fn true %)))
                             :title ""  :weight 0.12}
                            {:content-fn :schema/name     :title "Name"          :weight 0.38}
                            {:content-fn (comp :kixi.group/name :schema/author)   :title "Author"        :weight 0.3}
                            {:content-fn :schema/modified :title "Last Modified" :weight 0.2}]
                  :content results
                  :selected?-fn #(= (:schema/id %) (:schema/id @selected-schema))
                  :on-select (partial select-fn false)
                  :on-double-click (partial select-fn true)})
          [:div.close
           {:on-click close-fn}
           (icons/close)]]]))))

(defn group-search-area
  [ph on-click & opts]
  (let [show-breakout? (r/atom false)
        selected-group (r/atom nil)]
    (controller/raise! :user/refresh-groups {})
    (fn [ph on-click & opts]
      (let [{:keys [id disabled? exclusions]
             :or {id (str "group-search-field-"ph)
                  disabled? false
                  exclusions nil}} (first opts)
            results (:user/group-search-filtered (data/get-app-state :app/user))
            results (if exclusions
                      (let [excluded-groups (map :kixi.group/id exclusions)]
                        (remove (fn [x] (some #{(:kixi.group/id x)} excluded-groups)) results))
                      results)
            close-fn (fn [& _]
                       (aset (.getElementById js/document id) "value" nil)
                       (reset! selected-group nil)
                       (reset! show-breakout? false))
            select-fn (fn [final? group]
                        (on-click group final?)
                        (reset! selected-group group)
                        (when final?
                          (close-fn)))]
        [:div.shared-group-search-area
         (search-filter (get-string ph)
                        #(if (clojure.string/blank? %)
                           (reset! show-breakout? false)
                           (do (reset! show-breakout? true)
                               (controller/raise! :user/search-groups {:search %})))
                        {:id id
                         :disabled? disabled?})
         [:div.breakout-area
          {:style {:height (if @show-breakout? "300px" "0px")}}
          (table {:headers [{:content-fn #(button {:icon icons/tick
                                                   :id (:kixi.group/id %)
                                                   :prevent? true} identity)
                             :title ""  :weight 0.12}
                            {:content-fn inline-group
                             :title (get-string :string/name)
                             :weight 0.88}]
                  :content results
                  :selected?-fn #(= (:kixi.group/id %) (:kixi.group/id @selected-group))
                  :on-select (partial select-fn true)})
          [:div.close
           {:on-click close-fn}
           (icons/close)]]]))))

;; Experimenting with a new style of search area
(defn file-search-area
  [{:keys [on-init]} & _]
  (let [show-breakout? (r/atom false)
        selected-group (r/atom nil)
        table-id (str (random-uuid))]
    (when on-init
      (on-init))
    (fn [{:keys [ph on-click on-init get-results-fn selector-key on-search table-headers-fn on-scroll]} & [opts]]
      (let [{:keys [id disabled? exclusions]
             :or {id (str "search-field-"ph)
                  disabled? false
                  exclusions nil}} opts
            results (get-results-fn)
            results (if exclusions
                      (let [excluded-groups (map selector-key exclusions)]
                        (remove (fn [x] (some #{(selector-key x)} excluded-groups)) results))
                      results)
            soft-reset-fn (fn []
                            (when-let [el (.getElementById js/document table-id)]
                              (set! (.-scrollTop el) 0))
                            (reset! show-breakout? false))
            close-fn (fn [& _]
                       (aset (.getElementById js/document id) "value" nil)
                       (reset! selected-group nil)
                       (soft-reset-fn))
            select-fn (fn [final? group]
                        (when on-click
                          (on-click group final?))
                        (reset! selected-group group)
                        (when final?
                          (close-fn)))]
        [:div.shared-search-area
         (search-filter (get-string ph)
                        #(if (clojure.string/blank? %)
                           (soft-reset-fn)
                           (do (reset! show-breakout? true)
                               (on-search %)))
                        {:id id
                         :disabled? disabled?})
         [:div.breakout-area
          {:style {:height (if @show-breakout? "300px" "0px")}}
          (table {:id table-id
                  :headers (table-headers-fn)
                  :content results
                  :selected?-fn #(= (selector-key %) (selector-key @selected-group))
                  :on-select (partial select-fn true)
                  :on-double-click (partial select-fn true)
                  :on-scroll #(if (>= % 0.75)
                                (on-scroll %))})
          [:div.close
           {:on-click close-fn}
           (icons/close)]]]))))

(defn sharing-matrix
  [sharing-activites group->activities
   {:keys [on-change on-add all-disabled? show-search?]
    :or {show-search? true}} & opts]
  (let [debounce (atom false)]
    [:div.sharing-matrix
     (when show-search?
       [group-search-area
        :string/sharing-matrix-group-search-ph
        (fn [& args]
          (when-not @debounce
            (apply on-add args)
            (reset! debounce true)))
        (first opts)])
     (when (not-empty group->activities)
       [:table.pure-table.pure-table-horizontal.sharing-matrix-table-headers
        [:thead
         [:tr
          (cons
           [:th {:key "group-name"} (get-string :string/sharing-matrix-group-name)]
           (for [[key title] sharing-activites]
             [:th {:key title} title]))]]
        [:tbody
         (doall
          (for [[group activities :as row] (sort-by (comp :kixi.group/name first) group->activities)]
            (let [group-name (:kixi.group/name group)]
              [:tr
               {:key (str row)}
               [:td {:key group-name}
                (inline-group group)]
               (for [[activity-k activity-t] sharing-activites]
                 [:td
                  {:key (str group-name "-" activity-t)}
                  [:input {:type "checkbox"
                           :disabled (or all-disabled? (contains? (:locked activities) activity-k))
                           :checked (get (:values activities) activity-k)
                           :on-change #(let [new-value (.-checked (.-target %))]
                                         (on-change row activity-k new-value))}]])])))]])]))

(defn progress-bar
  [value]
  [:div.shared-progress-bar
   [:div.shared-progress-bar-inner
    {:style {:width (str (* value 100) "%")}}]])

(defn tabs
  [{:keys [tabs selected-tab on-click]}]
  [:div.shared-tabs
   (doall
    (for [[id label] tabs]
      [:div
       {:key (name id)
        :class (str "shared-tab " (when (= id selected-tab) "shared-tab-selected"))
        :on-click #(when on-click (on-click id))}
       label]))])

(defn tag
  ([tag-string]
   (tag tag-string nil nil))
  ([tag-string on-click-fn]
   (tag tag-string on-click-fn nil))
  ([tag-string on-click-fn on-cross-fn]
   [:div
    {:key (str "tag-" tag-string)
     :class (str "shared-tag " (when on-click-fn "shared-tag-clickable"))}
    (when on-cross-fn
      [:div.tag-close {:on-click #(on-cross-fn tag-string)}
       (icons/close)])
    [:span tag-string]]))

(defn editable-field
  [on-edit-fn & render-fns]
  (let [state (r/atom :idle)]
    (fn [on-edit-fn & render-fns]
      [:div.editable-field
       {:on-mouse-over #(reset! state :hovered)
        :on-mouse-leave #(reset! state :idle)}
       (vec (cons :div.editable-field-content
                  (vec render-fns)))
       (when (and on-edit-fn (= :hovered @state))
         [:span.clickable-text.edit-label
          {:on-click on-edit-fn}
          (get-string :string/edit)])])))

(defn collapsible-text
  [long-text]
  (let [expanded? (r/atom false)]
    (fn [long-text]
      [:div.shared-collapsible-text.flex-start
       [:div
        {:class (when-not @expanded? "rotate270")
         :on-click #(swap! expanded? not)}
        (icons/tree-arrow-down)]
       [:span
        {:class (when-not @expanded? "ellipsis")}
        long-text]])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defcard headings
  (let [example-text "Welcome to Witan"]
    (sab/html
     [:div
      [:h1 example-text]
      [:h2 example-text]
      [:h3 example-text]
      [:h4 example-text]
      [:h5 example-text]])))

(defcard paragraph
  (sab/html
   [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commqodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia gdeserunt mollit anim id est laborum."]))

(defcard search-filter
  (fn [data _]
    (let [filter-fn (partial swap! data assoc :result)]
      (sab/html
       (search-filter "This is a placeholder" filter-fn))))
  {:result ""}
  {:inspect-data true
   :frame true
   :history false})

(defcard table
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (table {:headers [{:content-fn #(icons/person :dark) :title ""  :weight 0.05}
                        {:content-fn :name     :title "Name"          :weight 0.55}
                        {:content-fn :owner    :title "Owner"         :weight 0.2}
                        {:content-fn :modified :title "Last Modified" :weight 0.2}]
              :content [{:name "Workspace for Camden Population"   :id 1 :owner "Bob"     :modified "Yesterday, 2pm"}
                        {:name "Workspace for Hounslow Population" :id 2 :owner "Alice"   :modified "4th Jan, 4.15pm"}
                        {:name "Workspace for Barnet Population"   :id 3 :owner "Charles" :modified "12th Jan, 10.24am"}]
              :selected?-fn #(= (:id %) (:selected-id @data))
              :on-select #(swap! data assoc :selected-id (:id %))
              :on-double-click #(swap! data assoc :last-dbl-click-id (:id %))})]))
  {:selected-id nil
   :last-dbl-click-id nil}
  {:inspect-data true
   :frame true
   :history false})

(defcard inline-file-title
  (sab/html
   [:div
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/file-type "csv"} :large :medium)]
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/file-type "csv"} :medium :small)]
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/file-type "csv"} :small :tiny)]]))

(defcard inline-file-title-bundles
  (sab/html
   [:div
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/bundle-type "datapack"} :large :medium)]
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/bundle-type "datapack"} :medium :small)]
    [:div (inline-file-title {:kixi.datastore.metadatastore/name "Foo Bar" :kixi.datastore.metadatastore/bundle-type "datapack"} :small :tiny)]]))

(defcard inline-group
  (sab/html
   [:ul
    [:li (inline-group {:kixi.group/name "Foo Bar" :kixi.group/type :user})]
    [:li (inline-group {:kixi.group/name "Foo Bar" :kixi.group/type :group})]]))

(def states
  [{:name "Alabama"}
   {:name "Alaska"}
   {:name "Arizona"}
   {:name "Arkansas"}
   {:name "California"}
   {:name "Colorado"}
   {:name "Connecticut"}
   {:name "Delaware"}
   {:name "Florida"}
   {:name "Georgia"}
   {:name "Hawaii"}
   {:name "Idaho"}
   {:name "Illinois"}
   {:name "Indiana"}
   {:name "Iowa"}
   {:name "Kansas"}
   {:name "Kentucky"}
   {:name "Louisiana"}
   {:name "Maine"}
   {:name "Maryland"}
   {:name "Massachusetts"}
   {:name "Michigan"}
   {:name "Minnesota"}
   {:name "Mississippi"}
   {:name "Missouri"}
   {:name "Montana"}
   {:name "Nebraska"}
   {:name "Nevada"}
   {:name "New Hampshire"}
   {:name "New Jersey"}
   {:name "New Mexico"}
   {:name "New York"}
   {:name "North Carolina"}
   {:name "North Dakota"}
   {:name "Ohio"}
   {:name "Oklahoma"}
   {:name "Oregon"}
   {:name "Pennsylvania"}
   {:name "Rhode Island"}
   {:name "South Carolina"}
   {:name "South Dakota"}
   {:name "Tennessee"}
   {:name "Texas"}
   {:name "Utah"}
   {:name "Vermont"}
   {:name "Virginia"}
   {:name "Washington"}
   {:name "West Virginia"}
   {:name "Wisconsin"}
   {:name "Wyoming"}])

(defcard index
  (sab/html
   (index states :name (fn [x] [:span (:name x)]))))

(defcard info-panel
  (sab/html
   (info-panel :string/new-data-request-created-desc)))

(defcard schema-search-area
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [schema-search-area :string/create-rts-schema-ph
        #(swap! data assoc :selected-schema %1)])]))
  {:selected-schema nil}
  {:inspect-data true
   :frame true
   :history false})

(defcard group-search-area
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [group-search-area :string/data-upload-search-groups
        #(swap! data assoc :selected-group %1)])]))
  {:selected-group nil}
  {:inspect-data true
   :frame true
   :history false})

(defcard file-search-area
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [file-search-area
        {:ph :string/create-datapack-search-files
         :on-click #(swap! data assoc :selected-file %1)
         :on-init identity
         :on-search #(swap! data assoc :search-results
                            (keep (fn [s]
                                    (when (gstring/caseInsensitiveContains (:kixi.datastore.metadatastore/name s) %)
                                      s)) (:all-files @data)))
         :get-results-fn #(:search-results @data)
         :selector-key :kixi.datastore.metadatastore/id
         :table-headers-fn (fn [selector-key select-fn]
                             [{:content-fn #(button {:icon icons/tick
                                                     :id (selector-key %)
                                                     :prevent? true}
                                                    (fn [_] (select-fn true %)))
                               :title ""  :weight 0.12}
                              {:content-fn #(inline-file-title % :small :small) :title (get-string :string/file-name) :weight 0.50}
                              {:content-fn :kixi.datastore.metadatastore/created :title (get-string :string/file-uploaded-at) :weight 0.38}])}])]))
  {:selected-file nil
   :all-files (vec (map-indexed (fn [i s]
                                  {:kixi.datastore.metadatastore/id (str i)
                                   :kixi.datastore.metadatastore/name (str (:name s) " " i)}) (concat states states)))
   :search-results []}
  {:inspect-data false
   :frame true
   :history false})

(defcard sharing-matrix
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [sharing-matrix (get @data :sharing-activites)
        (get @data :group->activites)
        {:on-change
         (fn [[group activities] activity target-state]
           (swap! data
                  #(assoc-in %
                             [:group->activites group :values activity]
                             target-state)))
         :on-add #()}])]))
  {:sharing-activites {:meta-read (get-string :string/file-sharing-meta-read)
                       :file-read (get-string :string/file-sharing-file-read)}
   :group->activites {{:kixi.group/id "123"
                       :kixi.group/name "balh"
                       :kixi.group/type "group"} {:values {:meta-read true
                                                           :file-read false}
                                                  :locked #{:meta-read}}
                      {:kixi.group/id "34531"
                       :kixi.group/name "ploop"
                       :kixi.group/type "user"} {:values {:meta-read false
                                                          :file-read true}}}}
  {:inspect-data true
   :frame true
   :history false})

(defcard progress-bar
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (for [v (:values @data)]
        (progress-bar v))]))
  {:values [0 0.1 0.5 0.75 1.0]}
  {:inspect-data true
   :frame true
   :history false})

(defcard tabs
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (tabs (assoc @data
                   :on-click #(swap! data assoc :selected-tab %)))]))
  {:tabs {:foo "Overview" :bar "Sharing" :baz "Edit"}
   :selected-tab :foo}
  {:inspect-data true
   :frame true
   :history false})

(defcard tags
  (fn [data _]
    (let [state-names (map :name states)]
      (sab/html
       [:div
        {:style {:width "100%"}}
        [:div
         "Standard"
         (doall (for [t (take 5 state-names)]
                  (tag t)))]
        [:div
         "Clickable"
         (doall (for [t (take 5 (drop 5 state-names))]
                  (tag t identity)))]

        [:div
         "Closeable"
         (doall (for [t (take 5 (drop 10 state-names))]
                  (tag t identity identity)))]]))))

(defcard editable-field
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [editable-field
        nil
        [:div "Just a card, no editing."]])
      (r/as-element
       [editable-field
        identity
        [:div "Hover to edit."]])])))

(defcard collapsible-text
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [collapsible-text (clojure.string/join "," (map :name states))])])))

(defn pagination
  [_ _]
  (fn
    [{:keys [page-blocks current-page]} on-click]
    (let [current-page (if (satisfies? IDeref current-page)
                         @current-page
                         current-page)]
      [:div.flex-start
       (button {:id (str "page-" (dec current-page))
                :class "btn-pagination"
                :prevent? false
                :disabled? (<= current-page 1)
                :txt (get-string :string/previous)}
               on-click)
       (for [page page-blocks]
         (button {:id (str "page-" page)
                  :txt page
                  :class (if (= page current-page)
                           "btn-pagination btn-success"
                           "btn-pagination")}
                 on-click))

       (button {:id (str "page-" (inc current-page))
                :class "btn-pagination"
                :prevent? false
                :disabled? (>= current-page (count page-blocks))
                :txt (get-string :string/next)}
               on-click)])))

(defcard pagination-panel
  (fn [data _]
    (let [page-blocks (range 1 11)]
      (sab/html
       [:div
        {:style {:width "100%"}}
        [pagination @data (fn [id]
                            (swap! data assoc :current-page (js/parseInt (subs id 5)))
                            )]])))
  {:page-blocks (range 1 11)
   :current-page 1}
  {:inspect-data true
   :frame true
   :history false})
