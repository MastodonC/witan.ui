(ns witan.ui.fixtures.forecast.input-view
  (:require [om.core :as om :include-macros true]
            [om-tools.dom :as dom :include-macros true]
            [om-tools.core :refer-macros [defcomponent defcomponentmethod]]
            [sablono.core :as html :refer-macros [html]]
            [inflections.core :as i]
            [clojure.string :as str]
            ;;
            [witan.ui.util :as util]
            [witan.ui.widgets :as widgets]
            [witan.ui.strings :refer [get-string]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]))


;; header
(def header-select-width  "3%")
(def header-name-width    "auto")
(def header-version-width "10%")
(def header-lm-width      "31%")

;; row
(def row-select-width  "3%")
(def row-name-width    "auto")
(def row-version-width "10%")
(def row-lm-width      "30%")

;; browser
(def browser-height "375px")

(defcomponent
  upload-widget
  [{:keys [cursor browsing?]} owner]
  (render [_]
          (html
           (let [{:keys [upload-filename
                         upload-file
                         upload-type
                         uploading?
                         upload-error?
                         upload-success?
                         last-upload-filename
                         data-items]} cursor]
             [:div.container
              [:h3 {:key "subtitle"}
               (get-string :upload-new-data)]
              (cond

                ;;;;;;;;;;;;;;;
                ;; ERROR MESSAGE
                ;;;;;;;;;;;;;;;
                upload-error?
                [:div
                 {:key "upload-error"}
                 [:h4
                  {:key "upload-error-title"}
                  (get-string :browser-upload-error)]
                 [:button.pure-button.button-warning
                  {:key "upload-error-reset"
                   :on-click #(do
                                (venue/raise! owner :error-reset)
                                (.preventDefault %))}
                  (get-string :back)]]

                ;;;;;;;;;;;;;;;
                ;; UPLOADING SPINNER
                ;;;;;;;;;;;;;;;
                uploading?
                [:div
                 {:key "uploading"
                  :style {:text-align :center}}
                 [:h4
                  {:key "uploading-title"}
                  (get-string :browser-upload-completes)]
                 [:div
                  {:key "uploading-spinner"}
                  [:i.fa.fa-refresh.fa-spin.fa-4x.text-primary]]]

                ;;;;;;;;;;;;;;;
                ;; FORM
                ;;;;;;;;;;;;;;;
                :else
                [:div
                 {:key "upload-form-container"}
                 [:div
                  {:key "upload-form"}
                  [:button.pure-button.button-secondary
                   {:key "button"
                    :style {:margin "0" :padding "0" :height "2em"}
                    :on-click #(.stopPropagation %)}
                   [:label {:for "upload-filename" :style {:padding "2em"}} "Choose file"]]
                  [:input.hidden-file-input {:key "input"
                                             :id "upload-filename"
                                             :type "file"
                                             :on-change #(venue/raise! owner
                                                                       :pending-upload
                                                                       (first (array-seq (.. % -target -files))))}]
                  [:div
                   {:key "filename"}
                   [:small (if upload-file upload-filename (get-string :browser-no-file-selected))]]]

                 [:form.pure-form.pure-form-stacked
                  {:key "upload-form-submit"
                   :on-submit #(do
                                 (let [node (om/get-node owner "upload-data-name")
                                       idx (.-selectedIndex node) ;; if it has a selectedIndex it's a select input
                                       result (if idx (.-value (aget (.-options node) idx)) (.-value node))]
                                   (venue/raise! owner :upload-file result))
                                 (.preventDefault %))}

                  ;;;;;;;;;;;;;;;
                  ;; UPLOAD TYPE SELECTOR
                  ;;;;;;;;;;;;;;;
                  [:select {:key "upload-select"
                            :disabled (if-not upload-file "disabled")
                            :value upload-type
                            :on-change #(venue/raise! owner :pending-upload-type (.. % -target -value))}
                   [:option {:key "existing" :value "existing"} (get-string :browser-upload-option-existing) ]
                   [:option {:key "new" :value "new" } (get-string :browser-upload-option-new)]]

                  (cond
                    (and upload-file browsing?)
                    [:div
                     {:key "upload-options"}
                     (condp = upload-type
                       :existing [:div
                                  {:key "upload-options-div"}
                                  [:label {:key "label"} (get-string :browser-upload-select-existing)]
                                  [:select.full-width
                                   {:key "input":ref "upload-data-name"}
                                   (for [{:keys [:data/name] :as item} data-items]
                                     [:option {:key (str "upload-data-option-" name) :value name} name])]]
                       :new      [:div
                                  {:key "upload-options-div"}
                                  [:label {:key "label"} (get-string :browser-upload-select-new)]
                                  [:input.full-width {:key "input"
                                                      :ref "upload-data-name"
                                                      :type "text"
                                                      :required true}]])
                     [:div.spacer
                      {:key "spacer"}]
                     [:button.pure-button.button-primary.upload-button {:key "button"} (get-string :upload)]]

                    ;;;;;;;;;;;;;;;
                    ;; SUCCESS MESSAGE
                    ;;;;;;;;;;;;;;;
                    upload-success?
                    [:div
                     {:key "upload-success"}
                     [:div.spacer
                      {:key "spacer"}]
                     [:p
                      {:key "paragraph"}
                      [:i.fa.fa-check.text-success
                       {:key "upload-tick" :style {:margin-right "0.5em"}}]
                      [:span {:key "label"} (get-string :upload-success ":" last-upload-filename)]]])]])]))))

(defcomponent
  input-browser
  [{:keys [cursor browsing?] :as state} owner]
  (render [_]
          (html
           (let [{:keys [data-items
                         selected-data-item]} cursor
                         has-selected? (contains? (set data-items) selected-data-item)]
             [:div.witan-pw-input-browser
              [:div.witan-pw-input-browser-content
               [:span.text-gray {:key "title"} (get-string :browser-choose-data)]
               [:div.spacer {:key "spacer"}]

               ;;;;;;;;;;;;;;;
               ;; SEARCH
               ;;;;;;;;;;;;;;;
               [:div.pure-u-1.pure-u-md-2-3.witan-pw-input-browser-content-search
                {:key "search"}
                [:h3 {:key "subtitle"}
                 (str (get-string :search) " " (get-string :data-items))]
                [:div
                 {:key "search-input-container"}
                 [:div.search-input
                  {:key "search-input"}
                  [:div.search-input-inner
                   {:style {:display "inline-block"} :key "search-input-form"}
                   (om/build widgets/search-input
                             (str (get-string :filter) " " (get-string :data-items))
                             {:opts {:on-input #(venue/raise! %1 :filter-data-items %2)}})]
                  [:button.pure-button.button-success
                   {:key "use-button"
                    :disabled (not has-selected?)
                    :on-click #(do
                                 (venue/raise! owner :select-input)
                                 (.preventDefault %))}
                   [:i.fa.fa-check]]
                  (let [url       (:data/s3-url selected-data-item)
                        disabled? (or (not has-selected?) (not url))]
                    [:a
                     {:key "download-button"
                      :href (when-not disabled? url)}
                     [:button.pure-button.button-primary
                      {:disabled disabled?}
                      [:i.fa.fa-download]]])]]
                [:div.spacer
                 {:key "spacer"}]
                [:div.list
                 {:key "data-item-list"}
                 (for [{:keys [data/data-id data/version data/name] :as data-item} data-items]
                   [:div.data-item
                    {:key (str "data-item-" data-id "-" version)
                     :class (when (= data-item selected-data-item) "selected")
                     :on-click #(do
                                  (venue/raise! owner :select-data-item data-item)
                                  (.preventDefault %))}
                    [:span (str name " - v" version)]])]]

               ;;;;;;;;;;;;;;;
               ;; UPLOAD
               ;;;;;;;;;;;;;;;
               [:div.pure-u-1.pure-u-md-1-3.witan-pw-input-browser-content-upload
                {:key "upload"}
                (om/build upload-widget state)]]]))))

(defcomponent
  data-item-input-table-row
  [{:keys [disabled data-item default? input browsing?]} owner]
  (render [_]
          (let [processed-item (util/map-remove-ns data-item)
                {:keys [name version created edited?]} processed-item
                key-prefix (partial str (i/hyphenate name) "-")]
            (html
             [:table.pure-table.pure-table-horizontal.full-width
              {:key (key-prefix "table-body")}
              [:tbody.witan-pw-input-data-row
               {:style {:text-align "left"}}
               [:td {:key (key-prefix "selector") :style {:width row-select-width}}
                [:button.pure-button.witan-pw-browse-toggle
                 {:on-click #(do
                               (venue/raise! owner :toggle-browse-input input)
                               (.preventDefault %))
                  :disabled disabled}
                 (if browsing?
                   [:i.fa.fa-caret-down]
                   [:i.fa.fa-caret-right])]]
               [:td {:key (key-prefix "name") :style {:width row-name-width}}
                [:div
                 [:span
                  {:class (if edited? "edited" (when-not data-item "not-specified"))}
                  (or name [:i (get-string :no-input-specified)])]
                 (cond
                   (and name default?) [:small.text-gray (get-string :default-brackets)]
                   (not name) [:small.text-gray
                               (get-string :please-select-data-input)])]]
               [:td {:key (key-prefix "version") :style {:width row-version-width} :class (when edited? "edited")} version]
               [:td {:key (key-prefix "lastmodified") :style {:width row-lm-width} :class (when edited? "edited")} (util/humanize-time created)]]]))))

(defcomponent
  data-item-input-table
  [{:keys [input top? browsing-input cursor]} owner]
  (render [_]
          (let [category (:category input)
                prefix (partial str category)
                browsing? (= browsing-input input)]
            (html
             [:div
              {:key (prefix "-input-div")}
              [:div.pure-u-4-5
               {:key (prefix "-input-table-container")}
               [:table.pure-table.pure-table-horizontal.full-width
                {:key (prefix "-input-table")}
                [:thead
                 {:key (prefix "-input-table-head")}
                 [:th {:key (prefix "-input-collapser") :style {:width header-select-width}}] ;; empty, for the tree icon
                 [:th {:key (prefix "-input-name") :style {:width header-name-width}} (i/capitalize category)]
                 [:th {:key (prefix "-input-version") :style {:width header-version-width}} (when top? (get-string :forecast-version))]
                 [:th {:key (prefix "-input-lastmodified") :style {:width header-lm-width}} (when top? (get-string :forecast-lastmodified))]]]
               [:hr {:key (prefix "-input-hr")}]
               (om/build data-item-input-table-row {:data-item (or (:selected input) (:default input))
                                                    :default? (nil? (:selected input))
                                                    :input input
                                                    :browsing? browsing?
                                                    :disabled (-> cursor :forecast :forecast/in-progress?) } {:key :name})]

              [:div.pure-u-1.witan-pw-input-browser-container
               {:style {:height (if browsing? browser-height "0px")}}
               (om/build input-browser {:cursor cursor :browsing? browsing?})]
              ]))))

(defcomponent view
  [[action {:keys [edited-forecast forecast model browsing-input] :as cursor}] owner]
  (render [_]
          (html
           (let [current-forecast-inputs (or (:forecast/inputs edited-forecast)
                                             (:forecast/inputs forecast))
                 inputs     (sort-by
                             :category
                             (util/squash-maps (:model/input-data model) current-forecast-inputs :category))
                 first-input (first inputs)
                 rest-inputs (rest inputs)]
             [:div#witan-pw-action-body

              ;; first row
              [:div
               {:key "input-first-row"}
               (om/build data-item-input-table {:input first-input :top? true :browsing-input browsing-input :cursor cursor})]

              ;; other rows
              (for [input rest-inputs]
                [:div
                 {:key (str (:category input) "-input-row")}
                 (om/build data-item-input-table {:input input :top? false :browsing-input browsing-input :cursor cursor})])]))))
