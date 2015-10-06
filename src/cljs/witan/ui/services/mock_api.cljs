(ns witan.ui.services.mock-api
  (:require [cljs.core.async :refer [put! take! chan <! close!]]
            [venue.core :as venue])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go]]))

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
      (log/warn "Using the MOCK API service. This should not be run in production."))))
