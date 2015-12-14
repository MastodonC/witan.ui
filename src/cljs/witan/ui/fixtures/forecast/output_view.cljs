(ns witan.ui.fixtures.forecast.output-view
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            ;;
            [witan.ui.util :as util]
            [witan.ui.widgets :as widgets]
            [witan.ui.strings :refer [get-string]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]))

;; header
(def header-name-width      "50%")
(def header-created-width   "30%")
(def header-downloads-width "20%")

;; row
(def row-name-width      "50%")
(def row-created-width   "28%")
(def row-downloads-width "auto")

(defcomponent
  data-output-table-row
  [{:keys [name version created s3-url file-name]} owner]
  (render [_]
          (let [key-prefix (partial str (i/hyphenate name) "-")]
            (html
             [:table.pure-table.pure-table-horizontal.full-width
              {:key (key-prefix "table-body")}
              [:tbody
               {:style {:text-align "left"}}
               [:td {:key (key-prefix "name") :style {:width row-name-width}}
                [:div.witan-pw-output-data-row
                 [:div
                  [:span (i/capitalize name)
                   [:small file-name]]]]]
               [:td {:key (key-prefix "created") :style {:width row-created-width}}
                [:div.witan-pw-output-data-row
                 [:span (util/humanize-time created)]]]
               [:td {:key (key-prefix "downloads") :style {:width row-downloads-width}}
                ;; excel
                #_[:button.pure-button.download
                   [:i.fa.fa-file-excel-o] [:span " Excel "] [:i.fa.fa-check.text-success]]
                ;; css
                [:a {:href s3-url :target "_blank"}
                 [:button.pure-button.download
                  [:i.fa.fa-file-text-o] [:span " CSV "]]]
                ;; zip
                #_[:button.pure-button.download
                   [:i.fa.fa-file-archive-o] [:span " ZIP "] [:i.fa.fa-times.text-error]]]]]))))

(defcomponent
  data-output-table
  [{:keys [output top?]} owner]
  (render [_]
          (let [outputs (-> output first second)
                category (-> outputs first :category)
                prefix (partial str category)]
            (html

             [:div#witan-pw-action-body
              {:key (prefix "-output-div")}
              [:div.pure-u-4-5
               {:key (prefix "-output-table-container")}
               [:table.pure-table.pure-table-horizontal.full-width
                {:key (prefix "-output-table")}
                [:thead
                 {:key (prefix "-output-table-head")}
                 [:th {:key (prefix "-output-name") :style {:width header-name-width}}
                  (i/capitalize category)]
                 [:th {:key (prefix "-output-created") :style {:width header-created-width}}
                  (get-string :created)]
                 [:th {:key (prefix "-output-downloads") :style {:width header-downloads-width}}
                  (when top? (get-string :downloads))]]]
               [:hr {:key (prefix "-output-hr")}]
               (om/build-all data-output-table-row outputs {:key :name})]]))))

(defcomponent view
  [[action {:keys [forecast model] :as cursor}] owner]
  (render [_]
          (html
           (cond
             (-> forecast :forecast/version zero?)
             [:div
              [:h3 {:key "new-version-no-downloads"} (get-string :new-version-no-downloads)]
              [:h3 {:key "sad-face"} [:i.fa.fa-frown-o.fa-2x]]]
             (:forecast/in-progress? forecast)
             [:div
              [:h3 {:key "in-progress-no-downloads"} (get-string :in-progress-no-downloads)]
              [:h3 {:key "coffee"} [:i.fa.fa-coffee.fa-2x]]]
             (:forecast/error forecast)
             [:div
              [:h3 {:key "error"} "An error occurred in the model"]
              [:h4 {:key "error-message"} (:forecast/error forecast)]
              [:h3 {:key "sad-face"} [:i.fa.fa-frown-o.fa-2x]]]
             :else
             [:div
              ;; first row
              (let [outputs        (:forecast/outputs forecast)
                    first-output   (first outputs)
                    rest-outputs   (rest outputs)]
                [:div
                 {:key "download-rows"}
                 [:div
                  {:key "output-first-row"}
                  (om/build data-output-table {:output first-output :top? true :cursor cursor})]

                 ;; other rows
                 (when (not-empty rest-outputs)
                   (for [output rest-outputs]
                     [:div
                      {:key (str (:category output) "-output-row")}
                      (om/build data-output-table {:output output :top? false :cursor cursor})]))])]))))
