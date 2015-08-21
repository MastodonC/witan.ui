(ns witan.schema.core
  (:require [schema.core :as s :include-macros true]))

(def Events
  "Any control events should be included here"
  #{:event/test-event            ;; this event is purely for testing
    :event/select-projection     ;; indicates that a projection is currently selected
    :event/toggle-tree-view      ;; indicates that the projection tree should expand at the specified branch
    :event/filter-projections    ;; filter the projections by name
    })

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
  {(s/required-key :id)            ProjectionIdType
   (s/required-key :name)          s/Str
   (s/required-key :type)          ProjectionTypes
   (s/required-key :owner)         s/Str
   (s/required-key :version)       s/Int
   (s/required-key :last-modified) s/Str
   (s/required-key :last-modifier) s/Str
   (s/optional-key :descendant-id) ProjectionIdType
   ;; added internally
   (s/optional-key :db/id)         s/Int})
