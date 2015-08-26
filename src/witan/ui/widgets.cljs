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
  [placeholder owner & opts]
  (render [_]
          (let [{:keys [on-input]} (first opts)]
            (html
             [:form.pure-form
              [:div.witan-search-input
               [:i.fa.fa-search]
               [:input {:id "filter-input"
                        :type "text"
                        :placeholder placeholder
                        :on-input (fn [e]
                                    (if (fn? on-input)
                                      (on-input owner (.. e -target -value)))
                                    (.preventDefault e))}]]]))))

(defcomponent
  forecast-tr
  [forecast owner & opts]
  (render [_]
          (let [{:keys [on-click on-double-click]} (first opts)
                forecasts-meta        (om/observe owner (refs/forecasts-meta))
                selected-forecast     (:selected forecasts-meta)
                ancestor-set            (set (map second (:has-ancestors forecasts-meta)))
                expanded-set            (set (map second (:expanded forecasts-meta)))
                is-selected-forecast? (= (:id forecast) (second selected-forecast))
                has-ancestor?           (contains? ancestor-set (:id forecast))
                is-expanded?            (contains? expanded-set (:id forecast))
                has-descendant?         (not (nil? (:descendant-id forecast)))
                classes                 [[is-selected-forecast? "witan-forecast-table-row-selected"]
                                         [has-descendant? "witan-forecast-table-row-descendant"]]]
            (html
             [:tr.witan-forecast-table-row {:key (:id forecast)
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
                                                              (on-click owner :event/toggle-tree-view forecast e)
                                                              (on-click owner :event/select-forecast forecast e)))
                                                          (.preventDefault e))
                                              :on-double-click (fn [e]
                                                                 (if (fn? on-double-click)
                                                                   (on-double-click owner forecast e))
                                                                 (.preventDefault e))}

              [:td.tree-control (cond
                                  is-expanded? [:i.fa.fa-minus-square-o.tree-control]
                                  has-ancestor? [:i.fa.fa-plus-square-o.tree-control])]
              [:td
               [:span.name.unselectable (:name forecast)]]
              [:td.text-center
               [:span.unselectable (-> forecast :type name i/capitalize)]]
              [:td.text-center
               [:span.unselectable (:owner forecast)]]
              [:td.text-center
               [:span.unselectable (:version forecast)]]
              [:td.text-center
               [:span.unselectable (:last-modified forecast)]
               [:span.modifier.unselectable (:last-modifier forecast)]]]))))
