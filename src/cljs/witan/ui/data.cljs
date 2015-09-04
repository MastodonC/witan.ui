(ns ^:figwheel-always witan.ui.data
  (:require [datascript :as d]
            [witan.ui.util :as util])
  (:require-macros
   [cljs-log.core :as log]))

(defonce app-state (atom {}))
(defonce db-schema {})
(defonce db-conn (d/create-conn db-schema))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-string
  "Assumes that strings are always in the :strings keyword"
  [keyword]
  (-> @app-state :strings keyword))

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

(defn load-dummy-data!
  []
  (log/warn "Loading dummy data...")
  (let [forecasts [{:id "1234"
                      :name "Population Forecast for Camden"
                      :type :population
                      :n-inputs 3
                      :n-outputs [2 3]
                      :owner "Camden"
                      :version 3
                      :last-modified "Aug 10th, 2015"
                      :last-modifier "Neil"}
                     {:id "1233"
                      :name "Population Forecast for Camden"
                      :type :population
                      :n-inputs 3
                      :n-outputs [2 3]
                      :owner "Camden"
                      :version 2
                      :last-modified "Aug 8th, 2015"
                      :last-modifier "Simon"
                      :descendant-id "1234"}
                     {:id "1232"
                      :name "Population Forecast for Camden"
                      :type :population
                      :n-inputs 3
                      :n-outputs [2 3]
                      :owner "Camden"
                      :version 1
                      :last-modified "July 4th, 2015"
                      :last-modifier "GLA"
                      :descendant-id "1233"}
                     {:id "5678"
                      :name "Population Forecast for Bexley"
                      :type :population
                      :n-inputs 2
                      :n-outputs [3 1]
                      :owner "Bexley"
                      :version 2
                      :last-modified "July 22nd, 2015"
                      :last-modifier "Sarah"}
                     {:id "5676"
                      :name "Population Forecast for Bexley"
                      :type :population
                      :n-inputs 2
                      :n-outputs [3 1]
                      :owner "Bexley"
                      :version 1
                      :last-modified "June 14th, 2015"
                      :last-modifier "Sarah"
                      :descendant-id "5678"}
                     {:id "3339"
                      :name "Population Forecast for Hackney"
                      :type :population
                      :n-inputs 3
                      :n-outputs [2 2]
                      :owner "Hackney"
                      :version 1
                      :last-modified "Feb 14th, 2015"
                      :last-modifier "Deepak"}]
        _ (d/transact! db-conn forecasts)
        db-forecasts (fetch-forecasts {})
        has-ancs (->>
                  (filter #(and (-> % :id fetch-ancestor-forecast empty? not) (nil? (:descendant-id %))) db-forecasts)
                  (map #(vector (:db/id %) (:id %)))
                  set)]
    (swap! app-state assoc :forecasts db-forecasts)
    (swap! app-state assoc-in [:forecasts-meta :has-ancestors] has-ancs)))
