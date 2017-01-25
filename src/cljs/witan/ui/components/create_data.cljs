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
                                         (or (:selected-schema d)
                                             (= (:wants-schema? d) :no))))
(defmethod phase-validation 4 [n d] (and (phase-validation (dec n) d)
                                         (and (> (count (:info-name d)) 2)
                                              (> (count (:info-description d)) 2))))
(defmethod phase-validation 5 [n d] (and (phase-validation (dec n) d)
                                         (or (and
                                              (= (:wants-to-share? d) :yes)
                                              (> (count (:selected-groups d)) 0))
                                             (= (:wants-to-share? d) :no))))

(defn phase
  [n data & body]
  (let [id (str "step-"n)
        header-kw (keyword "string" (str "data-upload-" id))
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
  [activities]
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
                       (zipmap (keys activities) (repeat true))}}))

(defn view
  [this]
  (let [activities->string (data/get-in-app-state :app/datastore :ds/activities)
        form-data (r/atom (empty-form-data
                           activities->string))]
    (fn [this]
      (let [{:keys [cd/pending?
                    cd/message]} (data/get-app-state :app/create-data)
            disabled? pending?]
        [:div#create-data
         [:div.container
          (shared/header :string/upload-new-data :string/upload-new-data-desc)
          [:div.content.pure-g
           {:key "content"}
           [:div.pure-u-lg-2-3.pure-u-sm-1.pure-u-1
            (shared/info-panel :string/data-upload-intro)

            (cond
              pending?
              [:div.uploading
               [:h2 (get-string :string/please-wait)]
               (icons/loading :large)]
              message
              [:div.upload-error
               [:h2 (get-string :string/error)]
               (icons/error :large :dark)
               [:div.error
                (get-string
                 (case message
                   :upload-failed :string/browser-upload-error
                   :api-failure   :string/api-failure))]
               [:div
                (shared/button {:id :retry-upload
                                :icon icons/retry
                                :txt :string/try-again}
                               #(do
                                  ;; TODO it'd be nice to maintain the form data but right now the search boxes and radios don't work, so we kill it all.
                                  (reset! form-data (empty-form-data
                                                     activities->string))
                                  (controller/raise! :data/reset-errors)))]]
              :else
              [:div.upload-phases

               ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 1 - Select files
               {:style {:display (if pending? :none nil)}}
               (phase
                1 form-data
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
               ;; Step 2 - Schema opt-out/selection
               (phase
                2 form-data
                [:input {:id  "step-2-yes" :type "radio" :name "schema" :value 1
                         :on-change #(swap! form-data assoc :wants-schema? :yes)}]
                [:label {:for "step-2-yes"} (get-string :string/data-upload-step-2-radio-1)][:br]
                [:input {:id  "step-2-no" :type "radio" :name "schema" :value 2
                         :on-change #(swap! form-data assoc :wants-schema? :no)}]
                [:label {:for "step-2-no"} (get-string :string/data-upload-step-2-radio-2)]
                (when (and (:wants-schema? @form-data)
                           (= (:wants-schema? @form-data) :yes))
                  [:div [shared/schema-search-area :string/create-rts-schema-ph
                         #(swap! form-data assoc :selected-schema %1)]
                   (when (:selected-schema @form-data)
                     [:div.selected-schema
                      [:span (get-string :string/data-upload-selected-schema ":")]
                      [:span.success (str (:schema/name (:selected-schema @form-data)) " ")]
                      [:span (get-string :string/from-lower " ")]
                      [:span.success (-> (:selected-schema @form-data) :schema/author :kixi.group/name)]])]))

               ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 3 - Information
               (phase
                3 form-data
                [:form.pure-form.pure-form-stacked
                 {:on-submit #(.preventDefault %)}
                 [:field-set
                  [:div.pure-control-group
                   [:label {:for "name"} (get-string :string/data-upload-step-3-input-1-title)]
                   [:input {:id  "name"
                            :type "text"
                            :value (:info-name @form-data)
                            :placeholder
                            (get-string :string/data-upload-step-3-input-1-ph)
                            :on-change #(swap! form-data assoc :info-name (.. % -target -value))}]]
                  [:div.pure-control-group
                   [:label {:for "desc"} (get-string :string/data-upload-step-3-input-2-title)]
                   [:textarea {:id  "desc"
                               :value (:info-description @form-data)
                               :placeholder
                               (get-string :string/data-upload-step-3-input-2-ph)
                               :on-change #(swap! form-data assoc :info-description (.. % -target -value))}]]]])

               ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 4 - Sharing
               (phase
                4 form-data
                [:input {:id  "step-4-yes" :type "radio" :name "share" :value 1
                         :on-change #(swap! form-data assoc :wants-to-share? :yes)}]
                [:label {:for "step-4-yes"} (get-string :string/data-upload-step-4-radio-1)][:br]
                [:input {:id  "step-4-no" :type "radio" :name "share" :value 2
                         :on-change #(swap! form-data assoc :wants-to-share? :no)}]
                [:label {:for "step-4-no"} (get-string :string/data-upload-step-4-radio-2)]

                (when (and (:wants-to-share? @form-data)
                           (= (:wants-to-share? @form-data) :yes))
                  [:div
                   [shared/sharing-matrix
                    activities->string
                    (:selected-groups @form-data)
                    {:on-change
                     (fn [[group activities] activity target-state]
                       (swap! form-data assoc-in [:selected-groups group activity] target-state))
                     :on-add
                     #(swap! form-data assoc-in [:selected-groups %]
                             {:kixi.datastore.metadatastore/meta-read true})}
                    {:exclusions (keys (:selected-groups @form-data))}]]))

               ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
               ;; Step 5 - Confirm
               (phase
                5 form-data
                [:p (get-string :string/data-upload-step-5-decl-unsure)]
                (when true
                  (shared/button {:id  :continue
                                  :class "data-upload"
                                  :txt :string/upload}
                                 #(controller/raise! :data/upload @form-data))))])]]]]))))
