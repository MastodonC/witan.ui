(ns witan.ui.controllers.search
  (:require [schema.core :as s]
            [witan.ui.ajax :as ajax]
            [witan.ui.data :as data]
            [witan.ui.activities :as activities]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [cljs-time.core :as t]
            [inflections.core :as i]
            [cljs-time.format :as tf]
            [witan.ui.route :as route]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.title :refer [set-title!]]
            [goog.string :as gstring]
            [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]]
            [cljsjs.toastr])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(declare on-query-response)

(defn set-expand-lock
  [v]
  (data/swap-app-state! :app/search
                        assoc
                        :ks/datapack-files-expand-in-progress
                        v))

(defn get-expand-lock
  []
  (data/get-in-app-state :app/search :ks/datapack-files-expand-in-progress))

(defmulti handle
  (fn [event args] event))

(defmethod handle
  :dashboard
  [event {:keys [search-term]}]
  (data/query {:search/dashboard [[{:search-term search-term}]]}
              on-query-response))

(defmethod handle
  :datapack-files
  [event {:keys [search-term]}]
  (when search-term
    (data/swap-app-state-in! [:app/search :ks/datapack-files]
                             assoc
                             :ks/current-search search-term)
    (when-not (get (data/get-in-app-state :app/search :ks/datapack-files :ks/search->result)
                   search-term)
      (data/query {:search/datapack-files [[{:search-term search-term}]]}
                  on-query-response))))

(defmethod handle
  :datapack-files-expand
  [event _]
  (let [datapack-files (data/get-in-app-state :app/search :ks/datapack-files)
        current-search (:ks/current-search datapack-files)
        current-paging (get-in datapack-files [:ks/search->result current-search :paging])
        current-item-count (count (get-in datapack-files [:ks/search->result current-search :items]))]
    ;;This lock isn't very strong, but seems to be enough else where
    (when (and (> (:total current-paging) current-item-count)
               (not (get-expand-lock)))
      (set-expand-lock true)
      (data/query {:search/datapack-files-expand [[{:search-term current-search
                                                    :from current-item-count}]]}
                  on-query-response))))

(defmethod handle
  :clear-datapack-files
  [event {:keys [search-term]}]
  (data/swap-app-state! :app/search
                        assoc
                        :ks/datapack-files {})
  (set-expand-lock false))

(defmulti on-query-response
  (fn [[k v]] k))

(defmethod on-query-response
  :search/dashboard
  [[_ {:keys [items]}]]
  (log/debug "Got: " items)
  ;;TODO need to control number of autocompletes stored

  (data/swap-app-state! :app/data-dash assoc
                        :items items
                        ;; :paging paging
                        )
  )


(defmethod on-query-response
  :search/datapack-files
  [[_ {:keys [search-term items] :as resp}]]
  (data/swap-app-state-in! [:app/search :ks/datapack-files :ks/search->result]
                           assoc
                           search-term resp))

(defmethod on-query-response
  :search/datapack-files-expand
  [[_ {:keys [search-term items paging] :as resp}]]
  (data/swap-app-state-in! [:app/search :ks/datapack-files :ks/search->result search-term :items]
                           (comp vec concat)
                           (:items resp))
  (data/swap-app-state-in! [:app/search :ks/datapack-files :ks/search->result search-term :paging :count]
                           +
                           (:count paging))
  (set-expand-lock false))
