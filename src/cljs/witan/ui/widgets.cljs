(ns ^:figwheel-always witan.ui.widgets
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [witan.ui.util :refer [contains-str]]
              [witan.ui.strings :refer [get-string]])
    (:require-macros [cljs-log.core :as log]))

;; search input
(defcomponent
  search-input
  "A search input element that has a magnifying glass."
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
