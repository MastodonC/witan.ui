(ns witan.ui.services.data
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [witan.ui.util :as util]
            [venue.core :as venue]
            [datascript :as d])
  (:require-macros [cljs-log.core :as log]))

(def state (atom {:logged-in? false}))
(defonce db-schema {})
(defonce db-conn (d/create-conn db-schema))
(defonce id-lookup (atom {}))
(defonce id-counter (atom 0))


(defn reset-db!
  []
  (reset! db-conn (d/empty-db)))

(defn logged-in? [] (:logged-in? @state))

(defn find-or-add-lookup
  "We're looking for a :db/id stored for this id. If we don't find one, add one. Return the :db/id either way."
  [ns id lookup counter]
  (let [kid (util/add-ns ns (keyword id))]
    (if-let [existing-id (get @lookup kid)]
      existing-id
      (let [new-id (swap! counter inc)]
        (swap! lookup assoc kid new-id)
        new-id))))

(defn add-descendant-ids [forecasts]
  (map (fn [item index]
         (if (and (> index 0)
                  (== (:forecast/forecast-id (nth forecasts (dec index)))
                      (:forecast/forecast-id item)))
           (assoc item :forecast/descendant-id true)
           item))
       forecasts (range 0 (count forecasts))))

(defn put-item-into-db!
  [item ns]
  (let [id (:version-id item)
        db-id (find-or-add-lookup ns id id-lookup id-counter)
        cleaned (->> item
                     (filter second)
                     (filter (fn [[k v]] (if (coll? v) (-> v empty? not) true)))
                     (util/map-add-ns ns)
                     (into {}))
        with-db-id (assoc cleaned :db/id db-id)]
    (d/transact! db-conn [with-db-id])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-ancestor-forecast
  "TODO This currently only handles the FIRST child. No support for branching."
  [id]
  (first (d/q '[:find (pull ?e [*])
                :in $ ?i
                :where [?e :forecast/descendant-id ?i]] @db-conn id)))

(defn build-descendant-list
  [db-id]
  (loop [node (d/touch (d/entity @db-conn db-id))
         results []
         remaining-nodes []]
    (let [new-results (conj results (:db/id node))
          new-remaining (concat remaining-nodes (fetch-ancestor-forecast (:forecast/version-id node)))]
      (if (not-empty new-remaining)
        (recur (d/touch (d/entity @db-conn (:db/id (first new-remaining)))) new-results (rest new-remaining))
        new-results))))

(defn filter-forecasts
  [{:keys [expand filter] :or {expand false
                               filter nil}}] ;; filter is only applied to top-level forecasts.
  (let [pred (fn [n] (if (nil? filter)
                       true
                       (util/contains-str n filter)))
        top-level (apply concat (d/q '[:find (pull ?e [*])
                                       :in $ ?pred
                                       :where [?e :forecast/version-id _]
                                       [?e :forecast/name ?n]
                                       [(get-else $ ?e :forecast/descendant-id nil) ?u]
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

(defn fetch-models
  []
  (apply concat (d/q '[:find (pull ?e [*])
                       :where [?e :model/version-id _]]
                     @db-conn)))

(defn find-model-id-by-name-and-version
  [name version]
  (d/q '[:find ?e ?id
         :in $ ?name ?version
         :where [?e :model/version-id ?id]
         [?e :model/version ?version]
         [?e :model/name ?name]]
       @db-conn
       name version))

(defn format-model-prop
  [[k v]]
  {:name k :value v})

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
  (let [forecasts (filter-forecasts (select-keys args [:expand :filter]))]
    (put! result-ch [:success {:forecasts (add-descendant-ids forecasts)
                               :has-ancestors (->>
                                               (filter #(> (:forecast/version %) 1) forecasts)
                                               (map #(vector (:db/id %) (:forecast/version-id %)))
                                               set)}])))

(defmethod request-handler
  :fetch-forecast-versions
  [owner event forecast-id result-ch]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :get-forecast
                   :args forecast-id
                   :context result-ch}))

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
(defmethod request-handler
  :fetch-models
  [owner event _ ch]
  (venue/request! {:owner owner
                   :service :service/api
                   :request :get-models
                   :context ch}))

(defmethod request-handler
  :add-forecast
  [owner event {:keys [model-name model-version model-props name description]} ch]
  (let [model-id (find-model-id-by-name-and-version model-name (js/parseInt model-version))]
    (if (not-empty model-id)
      (let [payload {:model-id (-> model-id first second)
                     :name name}
            payload (if (not-empty description) (assoc payload :description description) payload)
            payload (if (not-empty model-props) (assoc payload :model-properties (mapv format-model-prop  model-props)) payload)]
        (venue/request! {:owner owner
                         :service :service/api
                         :request :create-forecast
                         :args payload
                         :context ch}))
      (log/severe "Unable to locate a model with this name and version: " model-name model-version))))
;;;

(defmethod response-handler
  [:get-forecast :success] ;; singular
  [owner _ forecast-versions result-ch]
  (log/debug "Received" (count forecast-versions) "forecast versions.")
  (doseq [f forecast-versions]
    (put-item-into-db! f :forecast))
  (put! result-ch [:success nil]))

(defmethod response-handler
  [:get-forecast :failure]
  [owner _ msg result-ch]
  (log/debug "get-forecast failure" msg))

(defmethod response-handler
  [:get-forecasts :success] ;;plural
  [owner _ forecasts result-ch]
  (log/debug "Received" (count forecasts) "forecasts.")
  (doseq [f forecasts]
    (put-item-into-db! f :forecast))
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

(defmethod response-handler
  [:get-models :success]
  [owner _ models result-ch]
  (log/debug "Received" (count models) "models.")
  (doseq [m models]
    (put-item-into-db! m :model))
  (put! result-ch [:success (fetch-models)]))

(defmethod response-handler
  [:get-models :failure]
  [owner _ _ result-ch]
  (put! result-ch [:failure nil]))

(defmethod response-handler
  [:create-forecast :success]
  [owner _ new-forecast result-ch]
  (put-item-into-db! new-forecast :forecast)
  (put! result-ch [:success (:version-id new-forecast)]))

;;;;;;;;;;;;;;;;;;;;;

(defn- do-login!
  [logged-in?]
  (swap! state assoc :logged-in? logged-in?)
  (if logged-in?
    (venue/reactivate!)
    (set! (.. js/document -location -href) "/")))

(util/inline-subscribe!
 :api/user-logged-in
 #(do-login! true))

(util/inline-subscribe!
 :api/user-logged-out
 #(do-login! false))
