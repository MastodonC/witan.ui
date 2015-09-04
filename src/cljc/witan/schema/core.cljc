(ns witan.schema.core
  (:require [schema.core :as s :include-macros true]))

(def Events
  "Any control events should be included here"
  #{
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; INPUT EVENTS
    ;; These are events that result from user input
    ;; Handled by: controllers/input.cljs
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    :event/attempt-login       ;; attempts the login process
    :event/select-forecast     ;; indicates that a forecast is currently selected
    :event/toggle-tree-view    ;; indicates that the forecast tree should expand at the specified branch
    :event/filter-forecasts    ;; filter the forecasts by name
    :event/show-password-reset ;; indicates that the 'reset your password' dialog should be shown

    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
    ;; API EVENTS
    ;; These are events that correspond to an API call. These should only
    ;; be called by handlers, definitely NOT the views.
    ;; Handled by: controllers/api.cljs
    ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

    :api/login                 ;; attemps a login
    })

(def ForecastTypes
  "Valid Forecast types"
  (s/enum
   :population
   :employment))

(def ForecastIdType
  "The type of an ID"
  s/Str)

(def Forecast
  "A schema for a schema"
  {(s/required-key :id)            ForecastIdType
   (s/required-key :name)          s/Str
   (s/required-key :type)          ForecastTypes
   (s/required-key :n-inputs)      s/Int
   (s/required-key :n-outputs)     [s/Int]
   (s/required-key :owner)         s/Str
   (s/required-key :version)       s/Int
   (s/required-key :last-modified) s/Str
   (s/required-key :last-modifier) s/Str
   (s/optional-key :descendant-id) ForecastIdType
   ;; added internally
   (s/optional-key :db/id)         s/Int})
