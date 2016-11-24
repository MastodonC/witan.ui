(ns witan.ui.components.create-rts
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller])
  (:require-macros [cljs-log.core :as log]))

(defn assess-form-validity
  "A very crude way to validate we have enough information to make an
   RTS/RFD"
  [{:keys [recipients schema destinations]}]
  (let [recipients-result (when-not (pos? (count recipients)) :recipients)
        schema-result (when-not schema :schema)
        destinations-result (when-not (pos? (count destinations)) :destinations)]
    (set (keep identity [recipients-result
                         schema-result
                         destinations-result]))))

(defn user-search-area
  [recipients]
  (let [results (:user/group-search-results (data/get-app-state :app/user))
        ids (set (map :kixi.group/id @recipients))]
    [:div.user-list
     (for [{:keys [kixi.group/name kixi.group/id] :as group} (sort-by :kixi.group/type results)
           :when (not (contains? ids id))]
       [:div.user-list-item {:key name}
        (shared/button {:icon icons/plus
                        :id id
                        :prevent? true}
                       #(swap! recipients conj group))
        (shared/inline-group group)])]))

(defn schema-search-area
  [selected-schema]
  (let [results (:schema/search-results (data/get-app-state :app/datastore))]
    [:table.schema-list.pure-table
     [:thead
      [:tr
       [:th]
       [:th "Name"]
       [:th "Author"]]]
     [:tbody
      (doall
       (for [{:keys [schema/name schema/id schema/author] :as schema} results
             :let [author-name (:kixi.group/name author)]
             :when (not= schema @selected-schema)]
         [:tr.schema-list-item {:key name}
          [:td
           (shared/button {:icon icons/tick
                           :id id
                           :prevent? true}
                          #(reset! selected-schema schema))]
          [:td
           [:span name]]
          [:td author-name]]))]]))

(defn view
  [this]
  (let [show-recipient-breakout?   (r/atom false)
        show-schema-breakout?      (r/atom false)
        show-destination-breakout? (r/atom false)
        recipients                 (r/atom [])
        destinations               (r/atom [])
        schema                     (r/atom nil)
        invalid-fields             (r/atom #{})
        close-fn                   (fn [a n]
                                     (reset! a false)
                                     (aset (.getElementById js/document n) "value" nil))
        recipients-search-field    "recipients-search-field"
        schema-search-field        "schema-search-field"
        destinations-search-field  "destinations-search-field"
        request-message-field      "request-message-field"
        close-recipients-fn        (partial close-fn show-recipient-breakout? recipients-search-field)
        close-schema-fn            (partial close-fn show-schema-breakout? schema-search-field)
        close-destinations-fn      (partial close-fn show-destination-breakout? destinations-search-field)]
    (fn [this]
      (let [{:keys [crts/pending?
                    crts/message]} (data/get-app-state :app/create-rts)
            disabled? pending?]
        [:div#create-rts
         [:div.container
          (shared/header :string/create-request-to-share :string/create-rts-subtitle)
          [:div.content.pure-g
           [:div.pure-u-lg-2-3.pure-u-sm-1.pure-u-1

            ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
            ;; recipient users
            [:div
             {:key "recipients"}
             [:h2 {:key "title"}
              (get-string :string/create-rts-user)]
             (when (contains? @invalid-fields :recipients)
               [:p.error (get-string :string/create-rts-recipients-invalid)])
             (shared/search-filter
              (get-string :string/create-rts-user-ph)
              #(if (clojure.string/blank? %)
                 (reset! show-recipient-breakout? false)
                 (do (reset! show-recipient-breakout? true)
                     (controller/raise! :user/search-groups {:search %})))
              {:id recipients-search-field
               :disabled? disabled?})

             (when (not-empty @recipients)
               [:div.user-list
                [:p [:strong (get-string :string/create-rts-will-be-sent-to)]]
                (for [{:keys [kixi.group/name kixi.group/id] :as group} @recipients]
                  [:div.user-list-item {:key name}
                   (shared/button {:icon icons/delete
                                   :id id
                                   :prevent? true
                                   :disabled? disabled?}
                                  #(do
                                     (reset! recipients (remove #{group} @recipients))))
                   (shared/inline-group group)])])
             [:div.breakout-area
              {:style {:height (if @show-recipient-breakout? "300px" "0px")}}
              [:div.close
               {:on-click close-recipients-fn}
               (icons/close)]
              [:h3 (get-string :string/search-results)]
              [:div.container
               [user-search-area recipients]]]]

            ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
            ;; schema
            [:div
             {:key "schema"}
             [:h2 {:key "title"}
              (get-string :string/schema)]
             (when (contains? @invalid-fields :schema)
               [:p.error (get-string :string/create-rts-schema-invalid)])
             (shared/search-filter
              (get-string :string/create-rts-schema-ph)
              #(if (clojure.string/blank? %)
                 (reset! show-schema-breakout? false)
                 (do (reset! show-schema-breakout? true)
                     (controller/raise! :data/search-schema {:search %})))
              {:id schema-search-field
               :disabled? disabled?})

             (when @schema
               [:div.schema-list
                [:p [:strong (get-string :string/create-rts-selected-schema)]]
                [:div.schema-list-item
                 (shared/button {:icon icons/delete
                                 :id (:schema/id @schema)
                                 :prevent? true
                                 :disabled? disabled?}
                                #(reset! schema nil))
                 (shared/inline-schema @schema)]])
             [:div.breakout-area
              {:style {:height (if @show-schema-breakout? "300px" "0px")}}
              [:div.close
               {:on-click close-schema-fn}
               (icons/close)]
              [:h3 (get-string :string/search-results)]
              [:div.container
               [schema-search-area schema]]]]

            ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
            ;; destination groups
            [:div
             {:key "destinations"}
             [:h2 {:key "title"}
              (get-string :string/create-rts-destination)]
             (when (contains? @invalid-fields :destinations)
               [:p.error (get-string :string/create-rts-destinations-invalid)])
             (shared/search-filter
              (get-string :string/create-rts-destination-ph)
              #(if (clojure.string/blank? %)
                 (reset! show-destination-breakout? false)
                 (do (reset! show-destination-breakout? true)
                     (controller/raise! :user/search-groups {:search %})))
              {:id destinations-search-field
               :disabled? disabled?})

             (when (not-empty @destinations)
               [:div.user-list
                [:p [:strong (get-string :string/create-rts-will-be-shared-with)]]
                (for [{:keys [kixi.group/name kixi.group/id] :as group} @destinations]
                  [:div.user-list-item {:key name}
                   (shared/button {:icon icons/delete
                                   :id id
                                   :prevent? true
                                   :disabled? disabled?}
                                  #(reset! destinations (remove #{group} @destinations)))
                   (shared/inline-group group)])])
             [:div.breakout-area
              {:style {:height (if @show-destination-breakout? "300px" "0px")}}
              [:div.close
               {:on-click close-destinations-fn}
               (icons/close)]
              [:h3 (get-string :string/search-results)]
              [:div.container
               [user-search-area destinations]]]]

            [:div
             {:key "request-message"}
             [:h2 (get-string :string/message)
              [:em (get-string :string/optional)]]
             [:form.pure-form
              {:on-submit #(.preventDefault %)}
              [:textarea.pure-input-1 {:type "text"
                                       :id request-message-field
                                       :disabled disabled?
                                       :key "rmf"
                                       :placeholder (get-string :string/create-rts-message-ph)}]]]

            [:hr]
            (when message
              [:h3.icon-and-text.error (icons/error) (get-string message)])
            (if pending? (icons/loading)
                [:button.pure-button.button-success
                 {:type "submit"
                  :key "button"
                  :id "submit-button"
                  :on-click #(let [payload {:recipients   @recipients
                                            :schema       @schema
                                            :destinations @destinations
                                            :message (.-value (.getElementById js/document request-message-field))}
                                   assessment (->> payload
                                                   (assess-form-validity)
                                                   (reset! invalid-fields)
                                                   (not-empty))]
                               (close-recipients-fn)
                               (close-schema-fn)
                               (close-destinations-fn)
                               (when-not assessment
                                 (controller/raise! :rts/create payload)))}
                 (icons/plus) (get-string :string/create)])]]]]))))
