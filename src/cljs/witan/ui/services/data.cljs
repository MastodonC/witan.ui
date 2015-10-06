(ns witan.ui.services.data
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [witan.ui.util :as util]
            [venue.core :as venue]
            [datascript :as d])
  (:require-macros [cljs-log.core :as log]))

(def state (atom {:logged-in? false}))
(defonce db-schema {})
(defonce db-conn (d/create-conn db-schema))

(defn reset-db!
  []
  (reset! db-conn (d/empty-db)))

(defn logged-in? [] (:logged-in? @state))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-ancestor-forecast
  "TODO This currently only handles the FIRST child. No support for branching."
  [id]
  (first (d/q '[:find (pull ?e [*])
                :in $ ?i
                :where [?e :descendant-id ?i]] @db-conn id)))

(defn build-descendant-list
  [db-id]
  (loop [node (d/touch (d/entity @db-conn db-id))
         results []
         remaining-nodes []]
    (let [new-results (conj results (:db/id node))
          new-remaining (concat remaining-nodes (fetch-ancestor-forecast (:id node)))]
      (if (not-empty new-remaining)
        (recur (d/touch (d/entity @db-conn (:db/id (first new-remaining)))) new-results (rest new-remaining))
        new-results))))

(defn fetch-forecasts
  [{:keys [expand filter] :or {expand false
                               filter nil}}] ;; filter is only applied to top-level forecasts.
  (let [pred (fn [n] (if (nil? filter)
                       true
                       (util/contains-str n filter)))
        top-level (apply concat (d/q '[:find (pull ?e [*])
                                       :in $ ?pred
                                       :where [?e :id _]
                                       [?e :name ?n]
                                       [(get-else $ ?e :descendant-id nil) ?u]
                                       [(nil? ?u)]
                                       [(?pred ?n)]]
                                     @db-conn
                                     pred))]
    (if-not expand
      top-level
      (if (vector? expand)
        (mapcat (fn [forecast]
                  (if (some (fn [[ck cv]] (if (= (ck forecast) cv) [ck cv])) expand)
                    (let [db-id (:db/id forecast)
                          desc-tree (rest (build-descendant-list db-id))]
                      (apply conj [forecast] (mapv #(merge {} (d/pull @db-conn '[*] %)) desc-tree)))
                    [forecast])) top-level)
        (throw (js/Error. ":expand must be a vector of [k v] vectors"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti request-handler
  (fn [owner event args result-ch] event))

(defmulti response-handler
  (fn [owner result response cursor] result))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler owner request args response-ch))
    venue/IHandleResponse
    (handle-response [owner outcome event response context]
      (response-handler owner [event outcome] response context))))



(defmethod request-handler
  :filter-forecasts
  [owner event args result-ch]
  (let [forecasts (fetch-forecasts (select-keys args [:expand :filter]))]
    (put! result-ch [:success {:forecasts forecasts
                        :has-ancestors (->>
                                        (filter #(and (-> % :id fetch-ancestor-forecast empty? not) (nil? (:descendant-id %))) forecasts)
                                        (map #(vector (:db/id %) (:id %)))
                                        set)}])))

(defmethod request-handler
  :fetch-forecasts
  [owner event id result-ch]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :get-forecasts
                   :args id
                   :context result-ch}))

(defmethod request-handler
  :fetch-forecast
  [owner event id result-ch]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :get-forecast
                   :args id
                   :context result-ch}))

(defmethod request-handler
  :fetch-user
  [owner event id result-ch]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :get-user
                   :context result-ch}))
;;;

(defmethod response-handler
  [:get-forecast :success]
  [owner _ response result-ch]
  (put! result-ch [:success response]))

(defmethod response-handler
  [:get-forecasts :success]
  [owner _ forecasts result-ch]
  (log/debug "Received" (count forecasts) "forecasts.")
  (reset-db!)
  (d/transact! db-conn forecasts)
  (put! result-ch [:success nil]))

(defmethod response-handler
  [:get-forecasts :failure]
  [owner _ _ result-ch]
  (put! result-ch [:failure nil]))

(defmethod response-handler
  [:get-user :success]
  [owner _ user result-ch]
  (put! result-ch [:success user]))

(defmethod response-handler
  [:get-user :failure]
  [owner _ _ result-ch]
  (put! result-ch [:failure nil]))

;;;;;;;;;;;;;;;;;;;;;

(defn- do-login!
  []
  (swap! state assoc :logged-in? true)
  (venue/reactivate!))

(util/inline-subscribe!
  :api/user-logged-in
  #(do-login!))
