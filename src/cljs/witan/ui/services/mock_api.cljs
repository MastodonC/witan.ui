(ns witan.ui.services.mock-api
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go]]))

(defn get-dummy-forecast-headers []
  '({:description "Description of my forecast"
     :name "My Forecast 1"
     :created "2015-10-06T12:44:17.176-00:00"
     :version-id "78b1bf97-0ebe-42ef-8031-384e504cf795"
     :in-progress? true
     :forecast-id "fd44474d-e0f8-4713-bacf-299e503e4f30"
     :version 2
     :owner "cac4ba3a-07c8-4e79-9ae0-d97317bb0d45"
     :owner-name "Mock User 1"}
    {:description "Description of my forecast"
     :name "My Forecast 2"
     :created "2015-10-06T12:44:17.210-00:00"
     :forecast-id "768f40f8-cf06-4da6-8b98-5227034f7dd5"
     :in-progress? false
     :version-id "102fef0c-aa17-41bc-9f4e-cc11d18d7ae5"
     :version 0
     :owner "6961ed51-e1d6-4890-b102-ab862893e3ba"
     :owner-name "Mock User 2"}
    {:description "Description of my forecast"
     :name "My Forecast 3"
     :created "2015-10-06T12:44:17.240-00:00"
     :version-id "197481d6-df2a-4175-a288-d596a9709322"
     :in-progress? false
     :forecast-id "7185c4e4-739e-4eb8-8e37-f3f4b618ac1d"
     :version 1
     :owner "cac4ba3a-07c8-4e79-9ae0-d97317bb0d45"
     :owner-name "Mock User 1"}))

(defn get-dummy-forecasts []
  '({:forecast-id "fd44474d-e0f8-4713-bacf-299e503e4f30"
     :version 2
     :created "2015-10-14T08:41:21.477-00:00"
     :description "Description of my forecast"
     :in-progress? true
     :name "My Forecast 1"
     :owner "d8fc0f3c-0535-4959-bf9e-505af9a59ad9"
     :owner-name "Mock User 3"
     :version-id "78b1bf97-0ebe-42ef-8031-384e504cf795"}
    {:forecast-id "fd44474d-e0f8-4713-bacf-299e503e4f30"
     :version 1
     :created "2015-10-14T08:41:21.253-00:00"
     :description "Description of my forecast"
     :in-progress? false
     :name "My Forecast 1"
     :owner "d8fc0f3c-0535-4959-bf9e-505af9a59ad9"
     :owner-name "Mock User 3"
     :version-id "f960e442-2c85-489e-9807-4eeecd6fd55a"}
    ))

(defn get-dummy-models []
  '({:description "Model with enum"
     :properties
     [{:name "Boroughs"
       :type "dropdown"
       :context "Choose a borough"
       :enum_values
       ["Camden" "Richmond Upon Thames" "Hackney" "Barnet"]}]
     :version-id "658ad4d1-7929-454d-a998-fefc48590ab0"
     :input-data ["long population" "overlay housing" "trend data"]
     :name "My Model 3"
     :output-data ["housing-linked population"]
     :input-data-defaults
     {"long population"
      {:data-id "b72fda87-e34c-4c25-9c9f-c9021ed27b9a"
       :category "long population"
       :name "London base population"
       :publisher "28ba035d-18a2-43e6-b715-b4f27e29f8b2"
       :version 1
       :s3-url
       "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop.csv"
       :created "2015-10-21T14:16:40.325-00:00"}}
     :created "2015-10-21T14:16:40.312-00:00"
     :model-id "22e48390-f6df-4e16-b5e1-566096ce4915"
     :version 1
     :owner "eb63fe94-e76e-4c3f-9654-008cb2933f22"}
    {:description "Description of my model"
     :properties []
     :version-id "4230f728-d593-4fa3-9c89-0acb94e62fea",
     :input-data ["Base population data"]
     :name "My Model 1"
     :output-data ["wishful thinking"]
     :input-data-defaults {}
     :created "2015-10-21T14:16:40.266-00:00"
     :model-id "abf84bb1-84ec-418f-adaa-b385505468ce"
     :version 1
     :owner "28ba035d-18a2-43e6-b715-b4f27e29f8b2"}
    {:description "Description of my model"
     :properties
     [{:name "Some field"
       :type "text"
       :context "Placeholder value 123"
       :enum-values []}]
     :version-id "be6a4ddb-82d3-4290-ab4a-87fe6d14985d"
     :input-data ["Base population data"]
     :name "My Model 2"
     :output-data ["All the population data"]
     :input-data-defaults {}
     :created "2015-10-21T14:16:40.292-00:00"
     :model-id "e84ad26d-aa12-4133-a0c6-ab651de8fd68"
     :version 1
     :owner "eb63fe94-e76e-4c3f-9654-008cb2933f22"}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti response-handler
  (fn [result response cursor] result))

(defmulti request-handler
  (fn [request args result-ch] request))

(defn service
  []
  (reify
    venue/IHandleRequest
    (handle-request [owner request args response-ch]
      (request-handler request args response-ch))
    venue/IHandleResponse
    (handle-response [owner outcome event response cursor]
      (response-handler [event outcome] response cursor))
    venue/IInitialise
    (initialise [owner _]
      (log/warn "Using the MOCK API service. This should not be run in production.")
      (venue/publish! :api/user-logged-in)
      )))

(defmethod request-handler
  :login
  [_ _ result-ch]
  (venue/publish! :api/user-logged-in)
  (put! result-ch [:success {:token "faketoken" :id "1234567"}]))

(defmethod request-handler
  :get-forecasts
  [_ _ result-ch]
  (put! result-ch [:success (get-dummy-forecast-headers)]))

(defmethod request-handler
  :get-forecast-versions
  [_ _ result-ch]
  (put! result-ch [:success (get-dummy-forecasts)]))

(defmethod request-handler
  :get-user
  [_ _ result-ch]
  (put! result-ch [:success {:name "Mock User"}]))

(defmethod request-handler
  :get-forecast
  [_ args result-ch]
  (let [result (some (fn [forecast]
                       (let [{:keys [forecast-id version]} forecast
                             find-forecast-id (:id args)
                             find-version (js/parseInt (:version args))]
                         (when (and (= find-forecast-id forecast-id)
                                    (= find-version version))
                           forecast)))
                     (concat
                      (get-dummy-forecast-headers)
                      (get-dummy-forecasts)))]
    (put! result-ch [:success result])))

(defmethod request-handler
  :get-models
  [_ args result-ch]
  (put! result-ch [:success (get-dummy-models)]))

(defmethod request-handler
  :get-model
  [_ args result-ch]
  (put! result-ch [:success (first (get-dummy-models))]))
