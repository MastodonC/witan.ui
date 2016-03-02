(ns witan.ui.components.shared
  (:require [om.next :as om]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.strings :refer [get-string]]
            [witan.ui.components.icons :as icons])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn search-filter
  [placeholder on-input]
  [:div.shared-search-input
   [:form.pure-form
    {:key "search-filter-form"}
    [:div
     {:key "search-filter-inner-div"}
     (icons/search)
     [:input.pure-input-rounded
      {:key "filter-input"
       :id "filter-input"
       :type "text"
       :placeholder placeholder
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
      (for [{:keys [title weight]} headers]
        (let [percent (str (* 100 weight) "%")]
          [:th {:style {:width percent}} title]))]]]
   [:table.pure-table.pure-table-horizontal
    [:tbody
     (for [row content]
       [:tr
        {:class (when (and selected?-fn (selected?-fn row)) "selected")}
        (for [{:keys [content-fn title weight]} headers]
          (let [percent (str (* 100 weight) "%")]
            [:td {:style {:width percent}
                  :on-click (fn [e] (when on-select
                                      (on-select row)))
                  :on-double-click (fn [e] (when on-double-click
                                             (on-double-click row)))}
             (when content-fn (content-fn row))]))])]]])

(defn header
  ([title]
   (header title nil))
  ([title subtitle]
   [:div.shared-heading
    [:h1 (get-string title)]
    (when subtitle
      [:h2 (get-string subtitle)])]))

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
