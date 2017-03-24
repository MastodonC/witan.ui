(ns witan.ui.components.rts
  (:require [reagent.core :as r]
            [witan.ui.data :as data]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.route :as route]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [clojure.string :as str])
  (:require-macros [cljs-log.core :as log]))

(defn generate-submission-link
  [id]
  (str (.. js/document -location -origin) (route/find-path :app/rts-submit {:id id})))

(defn result-message
  []
  [:div
   {}
   (icons/tick :large)
   [:p (get-string :string/create-rts-message-created)]
   [:div
    {:style {:display :flex}}
    (shared/button {:id :mail :icon icons/email :txt :string/email} #())
    (shared/button {:id :dash :icon icons/request-to-share :txt :string/return-to-dash} #())]])

(defn requests-email-body
  [{:keys [kixi.data-acquisition.request-to-share/request-id
           kixi.data-acquisition.request-to-share/requester-id
           kixi.data-acquisition.request-to-share/schema
           kixi.data-acquisition.request-to-share/recipients
           kixi.data-acquisition.request-to-share/destinations
           kixi.data-acquisition.request-to-share/message
           kixi.data-acquisition.request-to-share/created-at] :as request}]
  (fn [{:keys [kixi.group/name]}]
    (str (get-string :string/rts-email-header-line)
         (if (str/blank? message)
           (gstring/format
            (get-string :string/rts-email-default-body)
            name
            (:schema/name schema))
           message)
         "\n\n----------------------------\n\n"
         (get-string :string/rts-email-footer-line)
         (generate-submission-link request-id)
         "\n\n----------------------------\n\n")))

(defn group-mail-row
  [body from group]
  [:div.group-mail-row
   [:div.flex
    (shared/inline-group group)
    (if-let [id (:kixi.data-acquisition.request-to-share.recipient/data-id group)]
      [:div.flex.group-mail-row-actions
       (icons/tick-circle :success)
       (shared/button {:id :foo
                       :icon icons/file
                       :txt :string/go-to-data
                       :class "group-mail-row-button"}
                      #(route/navigate! :app/rts {:id id}))]
      [:div.flex.group-mail-row-actions
       (icons/warning :warning)
       [:a {:href (str "mailto:?CC=" (str/join "," (:kixi.group/emails group))
                       "&Subject=" (gstring/urlEncode (str (get-string :string/rts-email-subject) (or from "Witan")))
                       "&body=" (gstring/urlEncode (body group)))
            :target "_blank"} (shared/button {:id :mail :icon icons/email :txt :string/send-mail
                                              :class "group-mail-row-button"} #())]])]])

(defn outbound-request-page
  [request username]
  (fn [{:keys [kixi.data-acquisition.request-to-share/request-id
               kixi.data-acquisition.request-to-share/requester-id
               kixi.data-acquisition.request-to-share/schema
               kixi.data-acquisition.request-to-share/recipients
               kixi.data-acquisition.request-to-share/destinations
               kixi.data-acquisition.request-to-share/message
               kixi.data-acquisition.request-to-share/created-at] :as request}]
    (let [noof-recips (count recipients)
          noof-submitters (count (filter :kixi.data-acquisition.request-to-share.recipient/data-id recipients))
          complete? (= noof-recips noof-submitters)
          status-icon (if complete?
                        (partial icons/tick-circle :success)
                        (partial icons/warning :warning))]
      [:div.padded-content
       [:div#heading.flex
        [:h1 (get-string :string/request-to-share-noun)]
        [:div (status-icon :large)]]
       [:h2 (get-string :string/status ":")
        (get-string (if complete? :string/rts-status-complete :string/rts-status-incomplete))]
       [:div#info.hero-notification
        [:p.info-paragraph
         [:span (get-string :string/rts-info-paragraph-1)]
         [:strong (:schema/name schema)]
         [:span (get-string :string/rts-info-paragraph-2)]
         [:strong (time/iso-time-as-moment created-at)]
         [:span (get-string :string/rts-info-paragraph-3)]
         [:strong noof-submitters]
         [:strong (get-string :string/rts-info-paragraph-4)]
         [:strong noof-recips]
         [:span (get-string :string/rts-info-paragraph-5)]]
        (if-not (clojure.string/blank? message)
          [:div
           [:p.info-paragraph
            [:span (get-string :string/rts-info-paragraph-6)]]
           [:p.info-paragraph
            [:strong message]]]
          [:div])]
       [:div#groups-index
        [:h2 (get-string :string/groups)]
        (shared/index recipients :kixi.group/name
                      (partial group-mail-row (requests-email-body request) username))]])))

(defn new-request-notice
  [request reset-me]
  [:div.hero-notification
   [:div.hero-close
    {:on-click #(do
                  (route/swap-query-string! (fn [x] (dissoc x :new)))
                  (reset! reset-me false))}
    (icons/close)]
   [:h1 (get-string :string/new-data-request-created)]
   [:p {:style {:width "70%"}}
    (get-string :string/new-data-request-created-desc)]])

(defn view
  [this]
  (let [new? (r/atom (utils/query-param :new))]
    (fn [this]
      (let [{:keys [rts/current rts/pending? rts/requests]} (data/get-app-state :app/request-to-share)
            current-request (get requests current)
            username (data/get-in-app-state :app/user :user/name)]
        (log/debug "RTS" new? current current-request)
        (cond
          pending? [:div.loading
                    (icons/loading :large)]
          current-request [:div#rts-view
                           (when @new?
                             [new-request-notice current-request new?])
                           [outbound-request-page current-request username]]
          :else     [:div.loading
                     (icons/error :large :dark)
                     [:h1 (get-string :string/error)]
                     [:h3 (get-string :string/rts-404-error)]
                     [:h4 [:a {:href (route/find-path :app/request-to-share)} (get-string :string/return-to-dash)]]])))))
