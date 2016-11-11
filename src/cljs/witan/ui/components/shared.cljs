(ns witan.ui.components.shared
  (:require [sablono.core :as sab :include-macros true]
            ;;
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
   [:table.pure-table.pure-table-horizontal
    [:thead
     [:tr
      (doall
       (for [{:keys [title weight]} headers]
         (let [percent (str (* 100 weight) "%")]
           [:th {:key title
                 :style {:width percent}} title])))]]]
   (if-not content
     [:div#loading.text-center (icons/loading :large)]
     [:table.pure-table.pure-table-horizontal
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
    (icon :small)
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
        alphabet (mapv char (range 65 91))]
    [:div.shared-index
     [:div.alpha-header
      (for [letter alphabet]
        [:span {:key letter
                :class (when (contains? groups letter) "alpha-header-clickable")
                :on-click (when (contains? groups letter)
                            #(.scrollIntoView (.-target %)))}
         letter])]
     [:hr]
     [:div.alpha-index
      (for [letter (sort (keys groups))]
        [:div.letter
         {:name letter
          :key letter}
         [:h1 letter]
         [:ul
          (for [item (get groups letter)]
            ^{:key item}
            [:li (render-fn item)])]])]]))

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
   [:p "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."]))

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
