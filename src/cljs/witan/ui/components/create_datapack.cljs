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

(defn empty-form-data
  [activities locked-activities]
  (let [user (data/get-in-app-state :app/user)]
    {:title ""
     :selected-files []
     :sharing-summary {}
     :selected-groups {{:kixi.group/id (:kixi.user/self-group user)
                        :kixi.group/name (:kixi.user/name user)
                        :kixi.group/type "user"}
                       {:values
                        (zipmap (keys activities) (repeat true))
                        :locked (set locked-activities)}}}))

(defn edit-title
  [datapack]
  [editable-field
   nil
   [:div.datapack-edit-title
    [:h2.heading (get-string :string/title)]
    (input-wrapper
     [:input {:id  "title"
              :type "text"
              :placeholder (get-string :string/create-datapack-title-ph)
              :on-change #(swap! datapack assoc :title (.. % -target -value))}])]])

(defn display-sharing-summary
  [ddatapack]
  (fn [{:keys [kixi.datastore.metadatastore/sharing]}]
    (let [group-names (set (map :kixi.group/name (:kixi.datastore.metadatastore/meta-read sharing)))]
      [shared/collapsible-text (str/join ", "  group-names)])))

(defn edit-files
  [datapack]
  (let [table-id "create-datapack-files-table"
        select-fn (fn [{:keys [kixi.datastore.metadatastore/id] :as x} & _]
                    (swap! datapack update :selected-files conj x))]
    [editable-field
     nil
     [:div.datapack-edit-file
      [:h2.heading (get-string :string/files)]
      [shared/file-search-area
       {:ph :string/create-datapack-search-files
        :on-click select-fn
        :on-init #(controller/raise! :data/refresh-files {})
        :on-search #(controller/raise! :data/search-files {:search %})
        :get-results-fn #(data/get-in-app-state :app/datastore :ds/files-search-filtered)
        :selector-key :kixi.datastore.metadatastore/id
        :table-headers-fn (fn []
                            [{:content-fn #(shared/button {:icon icons/tick
                                                           :id (str (:kixi.datastore.metadatastore/id %) "-select")
                                                           :prevent? true}
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
                              :weight 0.20}])}
       {:exclusions (:selected-files @datapack)}]
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
  [datapack activities->string]
  [editable-field
   nil
   [:div.datapack-edit-sharing
    [:h2 (get-string :string/datapack-sharing)]
    [shared/sharing-matrix
     activities->string
     (:selected-groups @datapack)
     {:on-change
      (fn [[group activities] activity target-state]
        (swap! datapack assoc-in [:selected-groups group :values activity] target-state)
        (when (every? false? (vals (get-in @datapack [:selected-groups group :values])))
          (swap! datapack update :selected-groups dissoc group)))
      :on-add
      (fn [g]
        (swap! datapack assoc-in [:selected-groups g :values] {:kixi.datastore.metadatastore/meta-read true}))}
     {:exclusions (keys (:selected-groups @datapack))}]]])

(defn create-button-disabled?
  [ddatapack]
  (clojure.string/blank? (:title ddatapack)))

(defn view
  []
  (let [activities->string (data/get-in-app-state :app/datastore :dp/activities)
        locked-activities (data/get-in-app-state :app/datastore :dp/locked-activities)
        datapack (r/atom (empty-form-data
                          activities->string
                          locked-activities))]
    (fn []
      (let [{:keys [cdp/pending? cdp/error] :as cdp} (data/get-in-app-state :app/create-datapack)]
        [:div#create-datapack-view
         (shared/header :string/create-new-datapack nil #{:center})
         [:div.flex-center
          [:div.container.padded-content
           (edit-title datapack)
           (edit-files datapack)
           (edit-sharing datapack activities->string)
           [:div.flex-vcenter-start
            (shared/button {:icon icons/datapack
                            :id :creats
                            :txt :string/create
                            :class "btn-success"
                            :prevent? true
                            :disabled? (or pending? (create-button-disabled? @datapack))}
                           #(controller/raise! :data/create-datapack {:datapack @datapack}))
            (when (:general error)
              [:div.error (:general error)])]]]]))))
