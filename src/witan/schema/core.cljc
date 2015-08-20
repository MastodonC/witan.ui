(ns witan.schema.core
  (:require [schema.core :as s :include-macros true]))

(def Events
  "Any control events should be included here"
  #{:event/test-event            ;; this event is purely for testing
    :event/select-projection}    ;; indicates that a projection is currently selected
  )

(def ProjectionTypes
  "Valid Projection types"
  (s/enum
   :population
   :employment))

(def ProjectionIdType
  "The type of an ID"
  s/Str)

(def Projection
  "A schema for a schema"
  {:id ProjectionIdType
   :name s/Str
   :type ProjectionTypes
   :owner s/Str
   :version s/Int
   :last-modified s/Str
   :last-modifier s/Str
   :previous-version (s/maybe (s/recursive #'Projection))})
