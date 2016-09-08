(ns witan.ui.components.secondary.data-select
  (:require [cljs.test :refer-macros [is async]]
            [sablono.core :as sab :include-macros true]
            [witan.ui.controller :as controller]
            [witan.ui.data :as data]
            [witan.ui.utils :as util]
            [witan.ui.components.icons :as icons]
            [witan.ui.components.primary :refer [switch-primary-view!]]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [devcards.core :as dc :refer [defcard deftest]]
                   [cljs-log.core :as log]))

(def mustache-regex
  (re-pattern #"\{\{ *[a-zA-Z0-9- _]+}\}"))

(defn clean-mustache
  [s]
  (-> s
      (clojure.string/replace  #"\s*\{\{\s*" "")
      (clojure.string/replace  #"\s*}}\s*" "")))

(defn get-variables-from-inputs
  [inputs]
  (->> inputs
       (map (comp :src :witan/params))
       (reduce #(concat (re-seq mustache-regex %2) %1) [])
       (set)
       (map clean-mustache)
       (not-empty)))

(defn variable-table
  [variables temps]
  [:table.pure-table.pure-table-horizontal
   {:key "variable-table"}
   [:thead
    [:tr
     [:th.col-data-num ""]
     [:th.col-data-name "Variable"]
     [:th.col-data-key "Value"]]]
   [:tbody
    (for [variable variables]
      [:tr
       {:key (str "variable-" variable)}
       [:td {:key "blank"} ">"]
       [:td {:key "name"} variable]
       [:td.pure-form {:key "value"}
        [:div.input-container
         {:style {:width "50%"}}
         [:input {:value (get temps variable)
                  :on-change #(controller/raise!
                               :workspace/adjust-temp-variable
                               {:key variable
                                :value (-> % .-target .-value)})}]]]])]])

(defn input-table
  [inputs temps]
  [:table.pure-table.pure-table-horizontal
   {:key "input-table"}
   [:thead
    [:tr
     [:th.col-data-num "#"]
     [:th.col-data-name "Name"]
     [:th.col-data-key "Key"]]]
   [:tbody
    (for [in-idx (-> inputs count range)]
      (let [in  (nth inputs in-idx)
            src (get-in in [:witan/params :src])
            tvs (->> src (re-seq mustache-regex))
            requires-render (and (not-empty tvs) ;; TODO THIS WONT SUPPORT MULTIPLE TVS IN ONE STRING
                                 (every? (partial contains? temps)
                                         (map clean-mustache tvs)))
            src' (if requires-render
                   (util/render-mustache src temps) src)
            fake (reduce (fn [a x] (clojure.string/replace a x (apply str (repeat (count x) \u00a0)))) src
                         (clojure.string/split src mustache-regex))
            fake' (if requires-render
                    (util/render-mustache fake temps) fake)]
        [:tr
         {:key (str "input-row" in-idx)}
         [:td {:key "num"}  (inc in-idx)]
         [:td {:key "name"} (:witan/name in)]
         [:td.pure-form {:key "src"
                         :style {:position "relative"}}
          [:div.input-container
           {:style {:position "absolute"
                    :left "0px"
                    :top "5px"}}
           [:input {:value src'
                    :on-change #(controller/raise!
                                 :workspace/adjust-current-data
                                 {:key (:witan/name in)
                                  :value (-> % .-target .-value)})}]
           [:span.fake-input
            {:class (when requires-render "has-input")}
            fake']]]]))]])

(defn data-select-view
  [catalog temp-variables]
  [:div#data-select
   (if-not (not-empty catalog)
     [:div#no-data (get-string :string/data-empty-catalog)]
     (let [inputs (->> catalog
                       (filter #(= :input (:witan/type %)))
                       (sort-by :witan/name))
           variables (get-variables-from-inputs inputs)]
       [:div
        (variable-table variables temp-variables)
        (input-table inputs temp-variables)]))])

(defn view
  []
  (fn []
    (let [{:keys [workspace/temp-variables] :as wsp} (data/get-app-state :app/workspace)
          {:keys [workspace/catalog]} (get wsp :workspace/current)]
      (data-select-view catalog temp-variables))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DEVCARDS

(defcard with-catalog
  (fn [data _]
    (sab/html
     (data-select-view (:catalog @data))))
  {:catalog [{:witan/name :add-births, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/add-births} {:witan/name :age-on, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-cor/age-on} {:witan/name :apply-migration, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/apply-migration} {:witan/name :calc-hist-asfr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/calc-hist-asfr, :witan/params {:fert-last-yr 2014}} {:witan/name :calc-hist-asmr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/calc-historic-asmr} {:witan/name :finish-looping?, :witan/version "1.0.0", :witan/type :predicate, :witan/fn :ccm-core/ccm-loop-pred, :witan/params {:last-proj-year 2021}} {:witan/name :in-hist-deaths-by-age-and-sex, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-deaths}} {:witan/name :in-hist-dom-in-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :domestic-in-migrants}} {:witan/name :in-hist-dom-out-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :domestic-out-migrants}} {:witan/name :in-hist-intl-in-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :international-in-migrants}} {:witan/name :in-hist-intl-out-migrants, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :international-out-migrants}} {:witan/name :in-hist-popn, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-population}} {:witan/name :in-hist-total-births, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :historic-births}} {:witan/name :in-proj-births-by-age-of-mother, :witan/version "1.0.0", :witan/type :input, :witan/fn :workspace-test/resource-csv-loader, :witan/params {:src nil, :key :ons-proj-births-by-age-mother}} {:witan/name :join-popn-latest-yr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/join-yrs} {:witan/name :out, :witan/version "1.0.0", :witan/type :output, :witan/fn :workspace-test/out} {:witan/name :prepare-starting-popn, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/prepare-starting-popn} {:witan/name :proj-dom-in-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-dom-in-mig, :witan/params {:start-yr-avg-dom-mig 2003, :end-yr-avg-dom-mig 2014}} {:witan/name :proj-dom-out-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-dom-out-mig, :witan/params {:start-yr-avg-dom-mig 2003, :end-yr-avg-dom-mig 2014}} {:witan/name :project-asfr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/project-asfr-finalyrhist-fixed, :witan/params {:fert-last-yr 2014, :start-yr-avg-fert 2014, :end-yr-avg-fert 2014}} {:witan/name :project-asmr, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/project-asmr-average-fixed, :witan/params {:start-yr-avg-mort 2010, :end-yr-avg-mort 2014}} {:witan/name :proj-intl-in-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-inter-in-mig, :witan/params {:start-yr-avg-inter-mig 2003, :end-yr-avg-inter-mig 2014}} {:witan/name :proj-intl-out-migrants, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/proj-inter-out-mig, :witan/params {:start-yr-avg-inter-mig 2003, :end-yr-avg-inter-mig 2014}} {:witan/name :combine-into-births-by-sex, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/combine-into-births-by-sex, :witan/params {:proportion-male-newborns 0.5121951219512195}} {:witan/name :combine-into-net-flows, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mig/combine-mig-flows} {:witan/name :project-births, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-fert/births-projection, :witan/params {:proportion-male-newborns 0.5121951219512195}} {:witan/name :project-deaths, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-mort/project-deaths-fixed-rates} {:witan/name :remove-deaths, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/remove-deaths} {:witan/name :select-starting-popn, :witan/version "1.0.0", :witan/type :function, :witan/fn :ccm-core/select-starting-popn}]}
  {:inspect-data true
   :frame true
   :history false})

(defcard empty-catalog
  (fn [data _]
    (sab/html
     (data-select-view (:catalog @data))))
  {:catalog []}
  {:inspect-data true
   :frame true
   :history false})
