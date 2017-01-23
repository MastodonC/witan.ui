(ns witan.ui.components.shared
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.data :as data]
            [witan.ui.controller :as controller]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.components.icons :as icons])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn search-filter
  [placeholder on-input & [{:keys [id disabled?]}]]
  [:div.shared-search-input
   [:form.pure-form
    {:key "search-filter-form"
     :on-submit #(.preventDefault %)}
    [:div
     {:key "search-filter-inner-div"}
     (icons/search)
     [:input.pure-input-rounded
      {:key "filter-input"
       :id (or id "filter-input")
       :type "text"
       :placeholder placeholder
       :disabled disabled?
       :style {:padding-left "30px"}
       :on-input (fn [e]
                   (if (fn? on-input)
                     (on-input (.. e -target -value)))
                   (.preventDefault e))}]]]])

(defn table
  [{:keys [headers content selected?-fn on-select on-double-click]}]
  [:div.shared-table
   [:table.pure-table.pure-table-horizontal.shared-table-headers
    [:thead
     [:tr
      (doall
       (for [{:keys [title weight]} headers]
         (let [percent (str (* 100 weight) "%")]
           [:th {:key title
                 :style {:width percent}} title])))]]]
   (if-not content
     [:div#loading.text-center (icons/loading :large)]
     [:table.pure-table.pure-table-horizontal.shared-table-rows
      [:tbody
       (doall
        (for [row content]
          [:tr
           {:key (apply str row)
            :class (when (and selected?-fn (selected?-fn row)) "selected")}
           (doall
            (for [{:keys [content-fn title weight]} headers]
              (let [percent (str (* 100 weight) "%")]
                [:td {:style {:width percent}
                      :key (apply str row title)

                      :on-click (fn [e] (when on-select
                                          (on-select row)))
                      :on-double-click (fn [e] (when on-double-click
                                                 (on-double-click row)))}
                 (when content-fn (content-fn row))])))]))]])])

(defn header
  ([title]
   (header title nil))
  ([title subtitle]
   [:div.shared-heading
    {:key "heading"}
    [:h1 (get-string title)]
    (when subtitle
      [:h2 (get-string subtitle)])]))

(defn button
  [{:keys [icon txt class id prevent? disabled?]} on-button-click]
  [:div.button-container
   {:key id}
   [:button.pure-button
    {:class class
     :key (str "button-" (name id))
     :disabled disabled?
     :on-click #(do
                  (when on-button-click (on-button-click id))
                  (when prevent? (.preventDefault %)))}
    (when icon (icon :small))
    [:span
     {:key (str "txt-" (name id))}
     (if (keyword? txt)
       (get-string txt)
       txt)]]])

(defn inline-group
  [{:keys [kixi.group/name kixi.group/type]}]
  [:div.shared-inline-group
   (let [icon-fn (condp = type
                   :user icons/user
                   :group icons/organisation
                   icons/help)]
     [:div.group-icon
      (icon-fn :small :dark)])
   [:span name]])

(defn inline-schema
  [{:keys [schema/name]}]
  [:div.shared-inline-schema
   [:span name]])

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
    (fn [ph on-click & opts]
      (let [{:keys [id disabled? exclusions]
             :or {id (str "group-search-field-"ph)
                  disabled? false
                  exclusions nil}} (first opts)
            results (:user/group-search-results (data/get-app-state :app/user))
            results (if exclusions
                      (remove (fn [x] (some #{x} exclusions)) results)
                      results)
            close-fn (fn [& _]
                       (aset (.getElementById js/document id) "value" nil)
                       (reset! show-breakout? false))
            select-fn (fn [final? group]
                        (reset! selected-group group)
                        (on-click group final?)
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
                                                   :prevent? true}
                                                  (fn [_] (select-fn true %)))
                             :title ""  :weight 0.12}
                            {:content-fn inline-group  :title "Name"          :weight 0.88}]
                  :content results
                  :selected?-fn #(= (:kixi.group/id %) (:kixi.group/id @selected-group))
                  :on-select (partial select-fn false)
                  :on-double-click (partial select-fn true)})
          [:div.close
           {:on-click close-fn}
           (icons/close)]]]))))

(defn sharing-matrix
  [sharing-activites group->activities on-change]
  [:div.sharing-matrix
   [:table.pure-table.pure-table-horizontal.sharing-matrix-table-headers
    [:thead
     [:tr
      (cons 
       [:th {:key "group-name"} (get-string :string/sharing-matrix-group-name)]
       (for [[key title] sharing-activites]
         [:th {:key title} title]))]]
    [:tbody
     (for [[group activities :as row] group->activities]
       (let [group-name (:kixi.group/name group)]
         [:tr
          {:key (str row)}
          [:td {:key group-name}
           (inline-group group)]
          (for [[activity-k activity-t] sharing-activites]
            [:td
             {:key (str group-name "-" activity-t)}
             [:input {:type "checkbox"
                      :checked (get activities activity-k)
                      :on-change #(let [new-value (.-checked (.-target %))]
                                    (on-change row activity-k new-value))}]])]))]]])

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

(defcard sharing-matrix
  (fn [data _]
    (sab/html
     [:div
      {:style {:width "100%"}}
      (r/as-element
       [sharing-matrix (get @data :sharing-activites)
        (get @data :group->activites)
        (fn [[group activities] activity target-state] 
          (swap! data
                 #(assoc-in %
                            [:group->activites group activity]
                            target-state))
          )])]))
  {:sharing-activites {:meta-read (get-string :string/file-sharing-meta-read)
                       :file-read (get-string :string/file-sharing-file-read)}
   :group->activites {{:kixi.group/id "123"
                       :kixi.group/name "balh"
                       :kixi.group/type :group} {:meta-read true
                       :file-read false}
                      {:kixi.group/id "34531"
                       :kixi.group/name "ploop"
                       :kixi.group/type :user} {:meta-read false
                       :file-read true}}}
  {:inspect-data true
   :frame true
   :history false})
