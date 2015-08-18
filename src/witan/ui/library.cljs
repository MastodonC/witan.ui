(ns ^:figwheel-always witan.ui.library
    (:require[om.core :as om :include-macros true]
             [om-tools.dom :as dom :include-macros true]
             [om-tools.core :refer-macros [defcomponent]]
             [sablono.core :as html :refer-macros [html]]
             [inflections.core :as i]))

;; search input
(defcomponent
  search-input
  [placeholder owner]
  (render [_]
          (html
           [:form.pure-form
            [:div.witan-search-input
             [:i.fa.fa-search]
             [:input {:id "filter-input"
                      :type "text"
                      :placeholder placeholder}]]])))

(defcomponent
  projection-tr
  [projection owner & opts]
  (render [_]
          (let [{:keys [on-click]} (first opts)]
            (html
             [:tr.witan-projection-table-row {:key (:id projection)
                                              :on-click (fn [e]
                                                           (if (fn? on-click) (on-click e owner projection))
                                                           (.preventDefault e))}
              [:td.tree-control [:i.fa.fa-plus-square-o]]
              [:td (:name projection)]
              [:td.text-center (name (i/capitalize (:type projection)))]
              [:td.text-center (:owner projection)]
              [:td.text-center (:version projection)]
              [:td.text-center
               [:span (:last-modified projection)]
               [:span.modifier (:last-modifier projection)]]]))))
