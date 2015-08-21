(ns ^:figwheel-always witan.ui.widgets
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [witan.ui.refs :as refs]
            [witan.ui.util :refer [contains-str]]))

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
          (let [{:keys [on-click]} (first opts)
                projections-meta        (om/observe owner (refs/projections-meta))
                selected-projection     (:selected projections-meta)
                ancestor-set            (set (map second (:has-ancestors projections-meta)))
                expanded-set            (set (map second (:expanded projections-meta)))
                is-selected-projection? (= (:id projection) (second selected-projection))
                has-ancestor?           (contains? ancestor-set (:id projection))
                is-expanded?            (contains? expanded-set (:id projection))
                has-descendant?         (not (nil? (:descendant-id projection)))
                classes                 [[is-selected-projection? "witan-projection-table-row-selected"]
                                         [has-descendant? "witan-projection-table-row-descendant"]]]
            (html
             [:tr.witan-projection-table-row {:key (:id projection)
                                              :class (->> classes
                                                          (filter first)
                                                          (map second)
                                                          (interpose " ")
                                                          (apply str))
                                              :on-click (fn [e]
                                                          (if (fn? on-click)
                                                            (if (and
                                                                 has-ancestor?
                                                                 (contains-str (.. e -target -className) "tree-control"))
                                                              (on-click owner :event/toggle-tree-view projection e)
                                                              (on-click owner :event/select-projection projection e)))
                                                          (.preventDefault e))}

              [:td.tree-control (cond
                                  is-expanded? [:i.fa.fa-minus-square-o.tree-control]
                                  has-ancestor? [:i.fa.fa-plus-square-o.tree-control])]
              [:td
               [:span.name (:name projection)]]
              [:td.text-center (-> projection :type name i/capitalize)]
              [:td.text-center (:owner projection)]
              [:td.text-center (:version projection)]
              [:td.text-center
               [:span (:last-modified projection)]
               [:span.modifier (:last-modifier projection)]]]))))
