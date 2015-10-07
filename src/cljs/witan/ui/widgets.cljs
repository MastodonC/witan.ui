(ns ^:figwheel-always witan.ui.widgets
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [witan.ui.util :refer [contains-str]])
  (:require-macros [cljs-log.core :as log]))

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
                {:keys [is-selected-forecast?
                        has-ancestor?
                        is-expanded?
                        has-descendant?]} forecast
                classes [[is-selected-forecast? "witan-forecast-table-row-selected"]
                         [has-descendant? "witan-forecast-table-row-descendant"]]]
            (html
             [:tr.witan-forecast-table-row {:key (:forecast/id forecast)
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
               [:span.name.unselectable (:forecast/name forecast)]]
              [:td.text-center
               [:span.unselectable (:forecast/owner forecast)]]
              [:td.text-center
               [:span.unselectable (:forecast/version forecast)]]
              [:td.text-center
               [:span.unselectable (:forecast/created forecast)]]]))))
