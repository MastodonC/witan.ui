(ns witan.ui.macros
  (:require [cljs.core]))

(defmacro create-standard-view-model!
  []
  `(do
     (cljs.core/declare ~'on-initialise)
     (cljs.core/declare ~'on-activate)

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
                      (~'on-activate owner# args# cursor#))
          venue/IInitialise
          (~'initialise [owner# cursor#]
                        (~'on-initialise owner# cursor#))))))
