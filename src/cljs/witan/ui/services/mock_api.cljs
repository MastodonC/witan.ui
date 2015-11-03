(ns witan.ui.services.mock-api
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go]]))

(def dummy-forecast-headers
  (atom '({:owner-name   "Mastodon 1",
           :version      2,
           :version-id   "11a58b9a-7574-443e-8e48-b1bcef39d5f0",
           :created      "2015-10-28T10:31:58",
           :forecast-id  "d400f74d-0279-487e-a853-95fe9234f1de",
           :name         "My Forecast 1",
           :owner        "30273232-cc37-424b-80a7-33b57f65d6d7",
           :description  "Description of my forecast"
           :in-progress? true
           :model-id    "c39cd8ac-ae45-4055-9668-c8447cf61347"}
          {:owner-name  "Mastodon 2",
           :version     0,
           :version-id  "0a6fe836-f830-4031-8cf8-806505742d8a",
           :created     "2015-10-28T10:31:58",
           :forecast-id "49385c66-5803-4a82-b6e2-0be9700337c8",
           :name        "My Forecast 2",
           :owner       "b984f34d-0c5b-4dcd-a34f-8b896acdac6c",
           :description "Description of my forecast"
           :model-id    "c39cd8ac-ae45-4055-9668-c8447cf61347"}
          {:owner-name  "Alice",
           :version     1,
           :version-id  "85416550-1823-4a95-9f28-649d6d9871eb",
           :created     "2015-10-28T10:31:58",
           :forecast-id "db9d9216-30bf-4bab-ba4c-647f4cd07ca4",
           :name        "My Forecast 3",
           :owner       "30273232-cc37-424b-80a7-33b57f65d6d7",
           :description "Description of my forecast"
           :model-id    "c39cd8ac-ae45-4055-9668-c8447cf61347"
           :tag         "GLA 2015"})))

(def dummy-forecasts
  "This only has forecasts for 'My Forecast 1' because it's the only mock data with >1 versions"
  (atom '({:owner-name   "Mastodon 1",
           :version      2,
           :version-id   "11a58b9a-7574-443e-8e48-b1bcef39d5f0",
           :created      "2015-10-28T10:31:58",
           :forecast-id  "d400f74d-0279-487e-a853-95fe9234f1de",
           :name         "My Forecast 1",
           :owner        "30273232-cc37-424b-80a7-33b57f65d6d7",
           :description  "Description of my forecast"
           :in-progress? true}
          {:owner-name  "Mastodon 1",
           :version     1,
           :version-id  "11a58b9a-7574-443e-8e48-b1bcef39d5f1",
           :created     "2015-10-28T10:31:58",
           :forecast-id "d400f74d-0279-487e-a853-95fe9234f1de",
           :name        "My Forecast 1",
           :owner       "30273232-cc37-424b-80a7-33b57f65d6d7",
           :description "Description of my forecast"}
          )))

(def dummy-models
  (atom '({:output-data [{:category "housing-linked population"}],
           :properties  [{:name        "Boroughs",
                          :type        "dropdown",
                          :context     "Choose a borough",
                          :enum_values ["Camden" "Richmond Upon Thames" "Hackney" "Barnet"]}],
           :owner       "b984f34d-0c5b-4dcd-a34f-8b896acdac6c",
           :name        "My Model 3",
           :description "Model with enum",
           :created     "2015-10-22T15:10:23",
           :input-data  [{:category "long population",
                          :default  {:category  "long population",
                                     :name      "London base population 1",
                                     :publisher "30273232-cc37-424b-80a7-33b57f65d6d7",
                                     :version   1,
                                     :data-id   "d43e929b-3d75-4ce0-a02e-1bf29cde2584",
                                     :s3-url    "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop.csv",
                                     :created   "2015-10-22T15:10:23"}}
                         {:category "overlay housing"}
                         {:category "trend data"}],
           :version-id  "91a8f08c-b8c1-4f1b-b912-3ff77dab5150",
           :version     1,
           :model-id    "c39cd8ac-ae45-4055-9668-c8447cf61347"}
          {:output-data [{:category "All the population data"}],
           :properties  [{:name        "Some field",
                          :type        "text",
                          :context     "Placeholder value 123",
                          :enum_values []}],
           :owner       "b984f34d-0c5b-4dcd-a34f-8b896acdac6c",
           :name        "My Model 2",
           :description "Description of my model",
           :created     "2015-10-22T15:10:23",
           :input-data  [{:category "Base population data"}],
           :version-id  "05dbcd48-2537-4dc4-884a-31465d475493",
           :version     1,
           :model-id    "5395674b-6f42-4ed8-80c7-5ea2f05c9b34"}
          {:output-data       [{:category "wishful thinking"}],
           :owner             "30273232-cc37-424b-80a7-33b57f65d6d7",
           :name              "My Model 1",
           :model/description "Description of my model",
           :created           "2015-10-22T15:10:23",
           :input-data        [{:category "Base population data"}],
           :version-id        "09af55ec-f20e-4ef8-909a-20865f8d71a6",
           :version           1,
           :model-id          "21f4c07e-703f-4057-842a-082cfb0cd85d"}
          )))

(def dummy-data-items
  (atom '({:category  "long population",
           :name      "London base population 1",
           :publisher "30273232-cc37-424b-80a7-33b57f65d6d7",
           :version   1,
           :data-id   "d43e929b-3d75-4ce0-a02e-1bf29cde2584",
           :s3-url    "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop.csv",
           :created   "2015-10-22T15:10:23"}
          {:category  "long population",
           :name      "London base population 2",
           :publisher "30273232-cc37-424b-80a7-33b57f65d6d7",
           :version   1,
           :data-id   "d43e929b-3d75-4ce0-a02e-1bf29cde2589",
           :s3-url    "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop2.csv",
           :created   "2015-10-22T15:10:23"}
          {:category  "long population",
           :name      "London base population 3",
           :publisher "30273232-cc37-424b-80a7-33b57f65d6d7",
           :version   1,
           :data-id   "d43e929b-3d75-4ce0-a02e-1bf29cde2587",
           :s3-url    "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop3.csv",
           :created   "2015-10-22T15:10:23"})))

(defn create-new-forecast!
  [args]
  (let [new-forecast (assoc args
                            :version 0
                            :version-id  "d43e929b-3d75-4ce0-a02e-1bf29cde2587"
                            :forecast-id "d43e929b-3d75-4ce0-a02e-1bf29cde2588"
                            :created     "2015-10-28T10:31:58"
                            :owner       "30273232-cc37-424b-80a7-33b57f65d6d7"
                            :owner-name  "Mock User")]
    (swap! dummy-forecast-headers conj new-forecast)
    new-forecast))

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
  (put! result-ch [:success @dummy-forecast-headers]))

(defmethod request-handler
  :get-forecast-versions
  [_ _ result-ch]
  (put! result-ch [:success @dummy-forecasts]))

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
                      @dummy-forecast-headers
                      @dummy-forecasts))]
    (put! result-ch [:success result])))

(defmethod request-handler
  :get-models
  [_ args result-ch]
  (put! result-ch [:success @dummy-models]))

(defmethod request-handler
  :get-model
  [_ args result-ch]
  (put! result-ch [:success (first @dummy-models)]))

(defmethod request-handler
  :get-data-items
  [_ args result-ch]
  (put! result-ch [:success @dummy-data-items]))

(defmethod request-handler
  :create-forecast
  [_ args result-ch]
  (let [new-forecast (create-new-forecast! args)]
    (put! result-ch [:success new-forecast])))

(defmethod request-handler
  :get-upload-token
  [_ args result-ch]
  (put! result-ch [:success {:dummy "dummy"}]))

(defmethod request-handler
  :create-data-item
  [_ {:keys [category name]} result-ch]
  (swap! dummy-data-items conj {:category  category,
                                :name      name,
                                :publisher "30273232-cc37-424b-80a7-33b57f65d6d7",
                                :version   1,
                                :data-id   (str "d43e929b-3d75-4ce0-a02e-" name),
                                :s3-url    "https://s3.eu-central-1.amazonaws.com/witan-test-data/Long+Pop.csv",
                                :created   "2015-10-22T15:10:23"})
  (put! result-ch [:success nil]))
