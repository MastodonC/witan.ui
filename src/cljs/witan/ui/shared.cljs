(ns witan.ui.shared
  (:require [om.next :as om]
            [sablono.core :as sab :include-macros true]
            ;;
            [witan.ui.icons :as icons])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]))

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

;;

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
