(ns witan.ui.components.bundle-add
  (:require [reagent.core :as r]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.activities]
            [witan.ui.components.create-datapack :as cd]
            [witan.ui.components.shared :as shared :refer [editable-field file-search-area]]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [clojure.string :as str]
            [inflections.core :as i]
            [cljsjs.pikaday.with-moment])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

;; using the same thinking as the create datapack layout.

(defn show-files
  [files options]
  (let []
    (fn [files {:keys [disabled?]}]
      [editable-field
       nil
       [:div.datapack-edit-file
        [:h2.heading (get-string :string/files)]
        [shared/file-search-area
         {:ph :string/edit-datapack-search-files
          ;;:on-click #(swap! files conj %)
          :on-click #(if (utils/user-has-edit? (data/get-user) %)
                       (reset! files #{%}) ;; HACK only one file allowed at the moment
                       (.error js/toastr (get-string :string/files-with-meta-edit-only)))
          :on-init #(controller/raise! :search/clear-datapack-files {})
          :on-scroll #(controller/raise! :search/datapack-files-expand {})
          :on-search #(controller/raise! :search/datapack-files {:search-term % :use-cache? false})
          :get-results-fn #(->> (data/get-in-app-state :app/search :ks/datapack-files :ks/current-search)
                                (data/get-in-app-state :app/search :ks/datapack-files :ks/search->result)
                                :items)
          :selector-key :kixi.datastore.metadatastore/id
          :table-headers-fn (fn []
                              [{:content-fn #(shared/inline-file-title % :small :small)
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
         {:exclusions @files
          :disabled? disabled?}]
        (when-not (empty? @files)
          [shared/table
           {:headers [{:content-fn
                       #(vector
                         :div.flex-start

                         (shared/button {:icon icons/search
                                         :id (str (:kixi.datastore.metadatastore/id %) "-open")
                                         :prevent? true}
                                        (fn [_]
                                          (.open
                                           js/window
                                           (str "/#" (route/find-path :app/data {:id (:kixi.datastore.metadatastore/id %)})))))
                         (shared/button {:icon icons/delete
                                         :id (str (:kixi.datastore.metadatastore/id %) "-delete")
                                         :disabled? disabled?}
                                        (fn [_] (swap! files disj %))))
                       :title ""  :weight "100px"}
                      {:content-fn #(shared/inline-file-title % :small :small)
                       :title (get-string :string/file-name)
                       :weight 0.6}
                      {:content-fn #(js/filesize (:kixi.datastore.metadatastore/size-bytes %))
                       :title (get-string :string/file-size)
                       :weight 0.15}
                      {:content-fn #(or (get-in
                                         %
                                         [:kixi.datastore.metadatastore.license/license
                                          :kixi.datastore.metadatastore.license/type]) (get-string :string/na))
                       :title (get-string :string/license)
                       :weight 0.15}
                      ]
            :content @files}])]])))

(defn view
  []
  (let [files (r/atom #{})]
    (controller/raise! :collect/reset-bundle-add-messages nil)
    (fn []
      (let [{:keys [ba/pending?
                    ba/failure-message
                    ba/success-message
                    ba/data] :as bundle-add} (data/get-app-state :app/bundle-add)]
        [:div#create-datapack-view
         (shared/header :string/share-files-to-datapack nil #{:center})
         [:div.flex-center
          (if data
            [:div.container.padded-content
             [editable-field nil
              [:div
               [:h4
                (get-string :string/datapack-collect-intro-text-1)]
               [:h4 {}
                (get-string :string/datapack-collect-intro-text-2 " ") [:a {:href (str "#" (route/find-path :app/data-create))
                                                                            :target "_blank" }
                                                                        (get-string :string/click-here-to-upload-files)]]
               [:h4
                (get-string :string/datapack-collect-intro-text-3)]
               [:h4
                (get-string :string/datapack-collect-intro-text-4)]
               [:ul
                [:li (get-string :string/datapack-collect-intro-text-li-1)]
                [:li (get-string :string/datapack-collect-intro-text-li-2)]]]]
             [show-files files {:disabled? (or pending?
                                               success-message)}]
             [:div
              [:div.flex-vcenter-start
               (shared/button {:id :collect-bundle-add
                               :_id :collect-bundle-add
                               :txt :string/datapack-sharing-bundle-add
                               :class "btn-success"
                               :prevent? true
                               :disabled? (or pending?
                                              (empty? @files)
                                              success-message)}
                              #(controller/raise! :collect/add-files-to-datapack {:added-files @files}))
               (cond
                 success-message [:span.success success-message]
                 pending? [:span (get-string :string/sending "....")])]
              (when
                  success-message (shared/button
                                   {:id :collect-bundle-add-return
                                    :_id :collect-bundle-add-return
                                    :txt :string/collect-bundle-add-return
                                    :class "btn-success"
                                    :prevent? true
                                    :disabled? false}
                                   #(route/navigate! :app/data-dash)))
              (when failure-message [:p.error failure-message])]]
            [:div.container.padded-content
             [:span.error (get-string :string/bundle-add-bad-data)]])]]))))
