(ns ^:figwheel-always witan.ui.data
  (:require [datascript :as d]))

(defonce app-state (atom {}))
(defonce db-schema {})
(defonce db-conn (d/create-conn db-schema))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn fetch-ancestor-projection
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
          new-remaining (concat remaining-nodes (fetch-ancestor-projection (:id node)))]
      (if (not (empty? new-remaining))
        (recur (d/touch (d/entity @db-conn (:db/id (first new-remaining)))) new-results (rest new-remaining))
        new-results))))

(defn fetch-projections
  [{:keys [expand] :or {expand false}}]
  (let [top-level (apply concat (d/q '[:find (pull ?e [*])
                                       :where [?e :id _]
                                       [(get-else $ ?e :descendant-id nil) ?u]
                                       [(nil? ?u)]]
                                     @db-conn))]
    (if-not expand
      top-level
      (if (vector? expand)
        (mapcat (fn [projection]
                  (if (first (filter (fn [[ck cv]] (= (ck projection) cv)) expand))
                    (let [db-id (:db/id projection)
                          desc-tree (rest (build-descendant-list db-id))]
                      (apply conj [projection] (mapv #(merge {} (d/pull @db-conn '[*] %)) desc-tree)))
                    [projection])) top-level)
        (throw (js/Error. ":expand must be a vector of [k v] vectors"))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn load-dummy-data!
  []
  (println "Loading dummy data...")
  (let [projections [{:id "1234"
                      :name "Population Projection for Camden"
                      :type :population
                      :owner "Camden"
                      :version 3
                      :last-modified "Aug 10th, 2015"
                      :last-modifier "Neil"}
                     {:id "1233"
                      :name "Population Projection for Camden"
                      :type :population
                      :owner "Camden"
                      :version 2
                      :last-modified "Aug 8th, 2015"
                      :last-modifier "Simon"
                      :descendant-id "1234"}
                     {:id "1232"
                      :name "Population Projection for Camden"
                      :type :population
                      :owner "Camden"
                      :version 1
                      :last-modified "July 4th, 2015"
                      :last-modifier "GLA"
                      :descendant-id "1233"}
                     {:id "5678"
                      :name "Population Projection for Bexley"
                      :type :population
                      :owner "Bexley"
                      :version 2
                      :last-modified "July 22nd, 2015"
                      :last-modifier "Sarah"}
                     {:id "5676"
                      :name "Population Projection for Bexley"
                      :type :population
                      :owner "Bexley"
                      :version 1
                      :last-modified "June 14th, 2015"
                      :last-modifier "Sarah"
                      :descendant-id "5678"}
                     {:id "3339"
                      :name "Population Projection for Hackney"
                      :type :population
                      :owner "Hackney"
                      :version 1
                      :last-modified "Feb 14th, 2015"
                      :last-modifier "Deepak"}]
        _ (d/transact! db-conn projections)
        db-projections (fetch-projections {})
        has-ancs (->>
                  (filter #(and (-> % :id fetch-ancestor-projection empty? not) (nil? (:descendant-id %))) db-projections)
                  (map #(vector (:db/id %) (:id %)))
                  set)]
    (swap! app-state assoc :projections db-projections)
    (swap! app-state assoc-in [:projections-meta :has-ancestors] has-ancs)))
