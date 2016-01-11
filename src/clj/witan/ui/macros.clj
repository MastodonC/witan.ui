(ns witan.ui.macros
  (:require [cljs.core]))

(defmacro create-standard-view-model!
  [body]
  `(do
     (cljs.core/defmulti ~'event-handler
       (cljs.core/fn [owner# event# args# cursor#] event#))

     (cljs.core/defmulti ~'response-handler
       (cljs.core/fn [owner# result# response# context#] result#))

     (cljs.core/defn ~'view-model
       []
       (cljs.core/reify
         venue/IHandleEvent
         (~'handle-event [owner# event# args# cursor#]
                         (~'event-handler owner# event# args# cursor#))
         venue/IHandleResponse
         (~'handle-response [owner# outcome# event# response# context#]
                            (~'response-handler owner# [event# outcome#] response# context#))
         venue/IActivate
         (~'activate [owner# args# cursor#]
                     (when (:on-activate ~body)
                       ((:on-activate ~body) owner# args# cursor#)))
         venue/IDeactivate
         (~'deactivate [owner# cursor#]
                       (when (:on-deactivate ~body)
                         ((:on-deactivate ~body) owner# cursor#)))
         venue/IInitialise
         (~'initialise [owner# cursor#]
                       (when (:on-initialise ~body)
                         ((:on-initialise ~body) owner# cursor#)))))))

(defmacro create-standard-service!
  []
  `(do
     (cljs.core/defmulti ~'request-handler
       (cljs.core/fn [owner# event# args# result-ch#] event#))

     (cljs.core/defmulti ~'response-handler
       (cljs.core/fn [owner# result# response# context#] result#))

     (cljs.core/defn ~'service
       []
       (cljs.core/reify
         venue/IHandleRequest
         (~'handle-request [owner# request# args# response-ch#]
                           (~'request-handler owner# request# args# response-ch#))
         venue/IHandleResponse
         (~'handle-response [owner# outcome# event# response# context#]
                            (~'response-handler owner# [event# outcome#] response# context#))))))
