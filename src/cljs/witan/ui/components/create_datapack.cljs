(ns witan.ui.components.create-datapack
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared :refer [editable-field]]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [witan.ui.route :as route]
            [goog.string :as gstring]
            [clojure.string :as str])
  (:require-macros [cljs-log.core :as log]))

(defn input-wrapper
  [& inputs]
  [:form.pure-form
   {:on-submit #(.preventDefault %)}
   (vec (cons :div inputs))])

(defn edit-title
  [on-change error]
  [editable-field
   nil
   [:div.datapack-edit-title
    [:h2.heading (get-string :string/title)]
    (input-wrapper
     [:input {:id  "title"
              :type "text"
              :placeholder (get-string :string/create-datapack-title-ph)
              :on-change #(on-change (.. % -target -value))
              }])
    (when error
      [:span.error error])]])

(defn display-sharing-summary
  [ddatapack]
  (fn [{:keys [kixi.datastore.metadatastore/sharing]}]
    (let [group-names (set (map :kixi.group/name (:kixi.datastore.metadatastore/meta-read sharing)))]
      [shared/collapsible-text (str/join ", "  group-names)])))

(defn edit-files
  [datapack]
  (let [table-id "create-datapack-files-table"
        select-fn (fn [{:keys [kixi.datastore.metadatastore/id] :as x} & _]
                    (swap! datapack update :selected-files conj x))
        present-files (into #{} (map :kixi.datastore.metadatastore/id (:selected-files @datapack)))]
    [editable-field
     nil
     [:div.datapack-edit-file
      [:h2.heading (get-string :string/files)]
      [shared/file-search-area
       {:ph :string/create-datapack-search-files
        :on-click select-fn
        :on-init #(controller/raise! :search/clear-datapack-files {})
        :on-search #(controller/raise! :search/datapack-files {:search-term %})
        :on-scroll #(controller/raise! :search/datapack-files-expand {})
        :get-results-fn #(->> (data/get-in-app-state :app/search :ks/datapack-files :ks/current-search)
                              (data/get-in-app-state :app/search :ks/datapack-files :ks/search->result)
                              :items)
        :selector-key :kixi.datastore.metadatastore/id
        :table-headers-fn (fn []
                            [{:content-fn #(shared/button {:icon icons/tick
                                                           :id (str (:kixi.datastore.metadatastore/id %) "-select")
                                                           :prevent? true
                                                           :disabled? (present-files (:kixi.datastore.metadatastore/id %))}
                                                          identity)
                              :title ""  :weight "50px"}
                             {:content-fn #(shared/inline-file-title % :small :small)
                              :title (get-string :string/file-name)
                              :weight 0.50}
                             {:content-fn (comp
                                           :kixi.user/name
                                           :kixi/user
                                           :kixi.datastore.metadatastore/provenance)
                              :title (get-string :string/file-uploader)
                              :weight 0.20}
                             {:content-fn (comp
                                           time/iso-time-as-moment
                                           :kixi.datastore.metadatastore/created
                                           :kixi.datastore.metadatastore/provenance)
                              :title (get-string :string/file-uploaded-at)
                              :weight 0.20}])}]
      (when (empty? (:selected-files @datapack))
        [:i [:h4 (get-string :string/create-datapack-no-files)]])
      [:div
       {:style {:display (if (empty? (:selected-files @datapack)) "none" "inherit")}}
       [shared/table
        {:headers [{:content-fn
                    #(vector
                      :div.flex-start
                      (shared/button {:icon icons/close
                                      :id (str (:kixi.datastore.metadatastore/id %) "-select")
                                      :prevent? true}
                                     (fn [_]
                                       (swap! datapack update :selected-files
                                              (fn [files]
                                                (remove #{%} files)))))
                      (shared/button {:icon icons/search
                                      :id (str (:kixi.datastore.metadatastore/id %) "-open")
                                      :prevent? true}
                                     (fn [_]
                                       (.open
                                        js/window
                                        (str "/#" (route/find-path :app/data {:id (:kixi.datastore.metadatastore/id %)}))))))
                    :title ""  :weight "90px"}
                   {:content-fn #(shared/inline-file-title % :small :small)
                    :title (get-string :string/file-name)
                    :weight 0.43}
                   {:content-fn (display-sharing-summary @datapack)
                    :title (get-string :string/visible-to)
                    :weight 0.43}]
         :content (reverse (:selected-files @datapack))
         :id table-id}]]]]))

(defn edit-sharing
  [selected-groups activities->string]
  (fn [selected-groups activities->string]
    [editable-field
     nil
     [:div.datapack-edit-sharing
      [:h2 (get-string :string/datapack-sharing)]
      [shared/sharing-matrix
       activities->string
       @selected-groups
       {:on-change
        (fn [[group activities] activity target-state]
          (swap! selected-groups assoc-in [group :values activity] target-state)
          (when (every? false? (vals (get-in @selected-groups [group :values])))
            (swap! selected-groups dissoc group)))
        :on-add
        (fn [g]
          (swap! selected-groups assoc-in [g :values] {:kixi.datastore.metadatastore/meta-read true}))}
       {:exclusions (keys @selected-groups)}]]]))

(defn title-ok?
  [ddatapack]
  (not (clojure.string/blank? (:title ddatapack))))

(defn view
  []
  (let [activities->string data/datastore-bundle-activities
        locked-activities data/datastore-bundle-default-activity-permissions
        datapack (r/atom nil)
        errors (r/atom {:title nil})
        selected-groups (r/atom nil)
        reset-form-data! #(let [user (data/get-user)
                                dp {:title ""
                                    :selected-files []
                                    :sharing-summary {}
                                    :user-id (:kixi.user/id user)}
                                sg {{:kixi.group/id (:kixi.user/self-group user)
                                     :kixi.group/name (:kixi.user/name user)
                                     :kixi.group/type "user"}
                                    {:values (zipmap (keys activities->string) (repeat true))
                                     :locked (set locked-activities)}}]
                            (reset! datapack dp)
                            (reset! selected-groups sg))]
    (reset-form-data!)
    (fn []
      (let [{:keys [cdp/pending? cdp/error] :as cdp} (data/get-in-app-state :app/create-datapack)
            user (:kixi.user/id (data/get-user))]
        ;; shouldn't really cache user data on the page
        ;; but we do. this fixes late login.
        (when (and (not (:user-id @datapack)) user)
          (reset-form-data!))

        (cond
          error
          [:div.flex-center
           [:div.upload-error
            [:h2 (get-string :string/error)]
            (icons/error :large :error)
            [:h3.error (get-string error)]
            [:div
             (shared/button {:id :retry-upload
                             :icon icons/retry
                             :txt :string/try-again}
                            #(do
                               ;; TODO it'd be nice to maintain the form data but right now the search boxes and radios don't work, so we kill it all.
                               (reset-form-data!)
                               (controller/raise! :data/reset-errors)))]]]

          :else
          [:div#create-datapack-view
           (shared/header :string/create-new-datapack nil #{:center})
           [:div.flex-center
            [:div.container.padded-content
             (edit-title (partial swap! datapack assoc :title) (:title @errors))
             (edit-files datapack)
             [edit-sharing selected-groups activities->string]
             [:div.flex-vcenter-start
              (shared/button {:icon icons/datapack
                              :id :creats
                              :txt :string/create
                              :class "btn-success"
                              :prevent? true
                              :disabled? pending?}
                             #(if (not (title-ok? @datapack))
                                (swap! errors assoc :title (get-string :string/datapack-title-error))
                                (controller/raise! :data/create-datapack {:datapack (assoc @datapack
                                                                                           :selected-groups @selected-groups)})))
              (when (:general error)
                [:div.error (:general error)])]]]])))))
