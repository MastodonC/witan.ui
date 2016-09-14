(ns witan.ui.components.secondary.configuration
  (:require [cljs.test :refer-macros [is async]]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.controller :as controller]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard deftest]]))

(defn format-catalog
  [catalog]
  (->> catalog
       (reduce (fn [a {:keys [witan/params witan/name witan/type]}]
                 (if-not (and params (not= :input type))
                   a
                   (reduce-kv (fn [a' k' v']
                                (-> a'
                                    (update k' #(assoc % :value v'))
                                    (update k' #(update % :fns conj name)))) a params))) {})
       (sort)))

(defn configuration-view
  [catalog]
  [:div#configuration
   (if-not (not-empty catalog)
     [:div#no-data (get-string :string/config-empty-catalog)]
     [:table.pure-table.pure-table-horizontal
      [:thead
       [:tr
        [:th.col-data-num "#"]
        [:th.col-data-name "Name"]
        [:th.col-data-key "Key"]]]
      [:tbody
       (let [fcatalog (format-catalog catalog)]
         (for [in-idx (-> fcatalog count range)]
           (let [[k {:keys [fns value]}] (nth fcatalog in-idx)]
             [:tr
              {:key (str "config-row" in-idx)}
              [:td {:key "num"}  (inc in-idx)]
              [:td {:key "name"} (str k)]
              [:td.pure-form {:key "value"}  [:input {:value value
                                                      :on-change #(controller/raise!
                                                                   :workspace/adjust-current-configuration
                                                                   {:key k
                                                                    :value (-> % .-target .-value)})}]]])))]])])

(defn view
  []
  (fn []
    (let [{:keys [workspace/catalog]}
          (get (data/get-app-state :app/workspace) :workspace/current)]
      (configuration-view catalog))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS

(defcard config-with-catalog
  (fn [data _]
    (sab/html
     (configuration-view (:catalog @data))))
  {:catalog [{:witan/name :add-births, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/add-births} {:witan/name :age-on, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-cor/age-on} {:witan/name :apply-migration, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/apply-migration} {:witan/name :calc-hist-asfr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/calc-hist-asfr, :witan/params {:fert-last-yr 2014}} {:witan/name :calc-hist-asmr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/calc-historic-asmr} {:witan/name :finish-looping?, :witan/version "1.0.0", :witan/type :predicate, :witan/fn :ccm-core/ccm-loop-pred, :witan/params {:last-proj-year 2021}} {:witan/name :in-hist-deaths-by-age-and-sex, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-deaths}} {:witan/name :in-hist-dom-in-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :domestic-in-migrants}} {:witan/name :in-hist-dom-out-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :domestic-out-migrants}} {:witan/name :in-hist-intl-in-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :international-in-migrants}} {:witan/name :in-hist-intl-out-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :international-out-migrants}} {:witan/name :in-hist-popn, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-population}} {:witan/name :in-hist-total-births, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-births}} {:witan/name :in-proj-births-by-age-of-mother, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :ons-proj-births-by-age-mother}} {:witan/name :join-popn-latest-yr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/join-yrs} {:witan/name :out, :witan/version "1.0.0", :witan/type :output, :witan/fn :workspace-test/out} {:witan/name :prepare-starting-popn, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/prepare-starting-popn} {:witan/name :proj-dom-in-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-dom-in-mig, :witan/params {:start-yr-avg-dom-mig 2003, :end-yr-avg-dom-mig 2014}} {:witan/name :proj-dom-out-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-dom-out-mig, :witan/params {:start-yr-avg-dom-mig 2003, :end-yr-avg-dom-mig 2014}} {:witan/name :project-asfr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/project-asfr-finalyrhist-fixed, :witan/params {:fert-last-yr 2014, :start-yr-avg-fert 2014, :end-yr-avg-fert 2014}} {:witan/name :project-asmr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/project-asmr-average-fixed, :witan/params {:start-yr-avg-mort 2010, :end-yr-avg-mort 2014}} {:witan/name :proj-intl-in-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-inter-in-mig, :witan/params {:start-yr-avg-inter-mig 2003, :end-yr-avg-inter-mig 2014}} {:witan/name :proj-intl-out-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-inter-out-mig, :witan/params {:start-yr-avg-inter-mig 2003, :end-yr-avg-inter-mig 2014}} {:witan/name :combine-into-births-by-sex, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/combine-into-births-by-sex, :witan/params {:proportion-male-newborns 0.5121951219512195}} {:witan/name :combine-into-net-flows, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/combine-mig-flows} {:witan/name :project-births, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/births-projection, :witan/params {:proportion-male-newborns 0.5121951219512195}} {:witan/name :project-deaths, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/project-deaths-fixed-rates} {:witan/name :remove-deaths, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/remove-deaths} {:witan/name :select-starting-popn, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/select-starting-popn}]}
  {:inspect-data true
   :frame true
   :history false})

(defcard config-empty-catalog
  (fn [data _]
    (sab/html
     (configuration-view (:catalog @data))))
  {:catalog []}
  {:inspect-data true
   :frame true
   :history false})
