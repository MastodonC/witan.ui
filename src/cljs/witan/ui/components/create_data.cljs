(ns witan.ui.components.create-data
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [goog.string :as gstring])
  (:require-macros [cljs-log.core :as log]))

(defn valid-extension?
  [ext]
  true)

(defn upload-phase-heading
  [n msg enabled?]
  [:div.upload-phase-heading
   {:key (str "step-" n "-header")
    :class (when-not enabled? "upload-phase-heading-disabled")}
   [:div.number-circle
    {:key (str "step-header-number-" n)}
    (str n)]
   [:strong
    {:key (str "step-header-title-" n)}
    (get-string msg)]])

(defmulti phase-validation (fn [n _] n))
(defmethod phase-validation :default [_ _] false)
(defmethod phase-validation 1 [_ _] true)
(defmethod phase-validation 2 [n d] (and (phase-validation (dec n) d)
                                         (:pending-file d)))
(defmethod phase-validation 3 [n d] (and (phase-validation (dec n) d)
                                         (> (count (:info-name d)) 2)))
(defmethod phase-validation 4 [n d] (and (phase-validation (dec n) d)
                                         (or (and
                                              (= (:wants-to-share? d) :yes)
                                              (> (count (:selected-groups d)) 0))
                                             (= (:wants-to-share? d) :no))))

(defn phase
  [n data header-kw & body]
  (let [id (str "step-"n)
        show? (phase-validation n @data)]
    [:div.upload-phase
     {:id id
      :key id}
     (upload-phase-heading n header-kw show?)
     (when show?
       [:div.upload-phase-content
        {:key (str "step-" n "-content")}
        (doall
         (for [el (range (count body))]
           ^{:key (str "phase-" n "-item-" el)}
           [:span (nth body el)]))])]))

(defn empty-form-data
  [activities locked-activities]
  (let [user (data/get-in-app-state :app/user)]
    {:pending-file nil
     :wants-schema? nil
     :selected-schema nil
     :info-name ""
     :info-description ""
     :wants-to-share? nil
     :selected-groups {{:kixi.group/id (:kixi.user/self-group user)
                        :kixi.group/name (:kixi.user/name user)
                        :kixi.group/type "user"}
                       {:values
                        (zipmap (keys activities) (repeat true))
                        :locked (set locked-activities)}}}))

(defn view
  [this]
  (let [activities->string (data/get-in-app-state :app/datastore :ds/activities)
        locked-activities (data/get-in-app-state :app/datastore :ds/locked-activities)
        form-data (r/atom (empty-form-data
                           activities->string
                           locked-activities))]
    (fn [this]
      (let [{:keys [cd/pending?
                    cd/pending-message
                    cd/message
                    cd/error]} (data/get-app-state :app/create-data)
            disabled? pending?]
        [:div#create-data
         [:div.container
          (shared/header :string/upload-new-data :string/upload-new-data-desc)
          [:div.content.pure-g
           {:key "content"}
           [:div.pure-u-lg-2-3.pure-u-sm-1.pure-u-1
            (shared/info-panel :string/data-upload-intro)

            (cond
              error
              [:div.upload-error
               [:h2 (get-string :string/error)]
               (icons/error :large :dark)
               [:div.error
                (get-string error)]
               [:div
                (shared/button {:id :retry-upload
                                :icon icons/retry
                                :txt :string/try-again}
                               #(do
                                  ;; TODO it'd be nice to maintain the form data but right now the search boxes and radios don't work, so we kill it all.
                                  (reset! form-data (empty-form-data
                                                     activities->string
                                                     locked-activities))
                                  (controller/raise! :data/reset-errors)))]]
              pending?
              [:div.uploading
               [:h2 (get-string :string/please-wait)]
               [:h3 (get-string (:message pending-message))]
               (icons/loading :large)
               [:div.progress-bar
                (shared/progress-bar (:progress pending-message))]]
              :else
              [:div.upload-phases

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 1 - Select files
               {:style {:display (if pending? :none nil)}}
               (phase
                1 form-data
                :string/data-upload-step-1
                [:input.hidden-file-input {:key "input-thing"
                                           :id "upload-filename"
                                           :type "file"
                                           :on-change
                                           (fn [e]
                                             (let [file (first (array-seq (.. e -target -files)))
                                                   file-name (.-name file)
                                                   ext (last (clojure.string/split file-name #"\."))]
                                               (if (valid-extension? ext)
                                                 (swap! form-data assoc :pending-file file)
                                                 (.alert js/window
                                                         (str file-name " is not a valid file type.")))))}]
                (shared/button {:id :select-upload
                                :icon icons/upload
                                :txt :string/choose-file}
                               #(.click (.getElementById js/document "upload-filename")))
                (when (:pending-file @form-data)
                  [:div.selected-file-name
                   {:key "selected-file-name"}
                   [:span (get-string :string/data-upload-selected-file ":")]
                   [:span [:span.success (str
                                          (utils/sanitize-filename (.-name (:pending-file @form-data))))]
                    [:span.success.size "("
                     (js/filesize (.-size (:pending-file @form-data))) ")"]]]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 2 - Information
               (phase
                2 form-data
                :string/data-upload-step-2
                [:form.pure-form.pure-form-stacked
                 {:on-submit #(.preventDefault %)}
                 [:field-set
                  [:div.pure-control-group
                   [:label {:for "name"} (get-string :string/data-upload-step-2-input-1-title)]
                   [:input {:id  "name"
                            :type "text"
                            :value (:info-name @form-data)
                            :placeholder
                            (get-string :string/data-upload-step-2-input-1-ph)
                            :on-change #(swap! form-data assoc :info-name (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "desc"} (get-string :string/data-upload-step-2-input-2-title)]
                   [:textarea {:id  "desc"
                               :value (:info-description @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-2-ph)
                               :on-change #(swap! form-data assoc :info-description (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "author"} (get-string :string/data-upload-step-2-input-3-title)]
                   [:textarea {:id  "author"
                               :value (:info-author @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-3-ph)
                               :on-change #(swap! form-data assoc :info-author (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "maintainer"} (get-string :string/data-upload-step-2-input-4-title)]
                   [:textarea {:id  "maintainer"
                               :value (:info-maintainer @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-4-ph)
                               :on-change #(swap! form-data assoc :info-maintainer (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "source"} (get-string :string/data-upload-step-2-input-5-title)]
                   [:textarea {:id  "source"
                               :value (:info-source @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-5-ph)
                               :on-change #(swap! form-data assoc :info-source (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "geo-smallest"} (get-string :string/data-upload-step-2-input-6-title)]
                   [:textarea {:id  "geo-smallest"
                               :value (:info-geo-smallest @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-6-ph)
                               :on-change #(swap! form-data assoc :info-geo-smallest (.. % -target -value))}]]             
                  [:div.pure-control-group
                   [:label {:for "temporal-cov-from"} (get-string :string/data-upload-step-2-input-7-title)]
                   [:textarea {:id  "temporal-from"
                               :value (:info-temporal-cov-from @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-7-ph)
                               :on-change #(swap! form-data assoc :info-temporal-cov-from (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "temporal-cov-to"} (get-string :string/data-upload-step-2-input-8-title)]
                   [:textarea {:id  "temporal-to"
                               :value (:info-temporal-cov-to @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-8-ph)
                               :on-change #(swap! form-data assoc :info-temporal-cov-to (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "tags"} (get-string :string/data-upload-step-2-input-9-title)]
                   [:textarea {:id  "tags"
                               :value (:info-tags @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-9-ph)
                               :on-change #(swap! form-data assoc :info-tags (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "license-type"} (get-string :string/data-upload-step-2-input-10-title)]
                   [:textarea {:id  "license-type"
                               :value (:info-license-type @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-10-ph)
                               :on-change #(swap! form-data assoc :info-license-type (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "license-usage"} (get-string :string/data-upload-step-2-input-11-title)]
                   [:textarea {:id  "license-usage"
                               :value (:info-license-usage @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-2-input-11-ph)
                               :on-change #(swap! form-data assoc :info-license-usage (.. % -target -value))}]]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 3 - Sharing
               (phase
                3 form-data
                :string/data-upload-step-3
                [:input {:id  "step-4-yes" :type "radio" :name "share" :value 1
                         :on-change #(swap! form-data assoc :wants-to-share? :yes)}]
                [:label {:for "step-4-yes"} (get-string :string/data-upload-step-3-radio-1)][:br]
                [:input {:id  "step-4-no" :type "radio" :name "share" :value 2
                         :on-change #(swap! form-data assoc :wants-to-share? :no)}]
                [:label {:for "step-4-no"} (get-string :string/data-upload-step-3-radio-2)]

                (when (and (:wants-to-share? @form-data)
                           (= (:wants-to-share? @form-data) :yes))
                  [:div
                   [shared/sharing-matrix
                    activities->string
                    (:selected-groups @form-data)
                    {:on-change
                     (fn [[group activities] activity target-state]
                       (swap! form-data assoc-in [:selected-groups group :values activity] target-state))
                     :on-add
                     #(swap! form-data assoc-in [:selected-groups % :values]
                             {:kixi.datastore.metadatastore/meta-read true})}
                    {:exclusions (keys (:selected-groups @form-data))}]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 4 - Confirm
               (phase
                4 form-data
                :string/data-upload-step-4
                [:p (get-string :string/data-upload-step-4-decl-unsure)]
                (when true
                  (shared/button {:id  :continue
                                  :class "data-upload"
                                  :txt :string/upload}
                                 #(controller/raise! :data/upload @form-data))))])]]]]))))
