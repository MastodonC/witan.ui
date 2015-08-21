(ns ^:figwheel-always witan.ui.util
  (:require [witan.ui.data :refer [app-state]]))

(defn get-string
  "Assumes that strings are always in the :strings keyword"
  [keyword]
  (-> @app-state :strings keyword))

(defn prependtial
  "Works like `partial` except the additional args are prepended, rather than appended.
   i.e. ((partial str 'hello') 'world') => 'helloworld'
        ((prependtial str 'hello') 'world) => 'worldhello'"
  [f & args1]
  (fn [& args2]
    (apply f (concat args2 args1))))

(defn contains-str
  "Performs a case-insensitive substring match"
  [source match]
  (not= -1 (.indexOf (.toLowerCase source) (.toLowerCase match))))

(defn contains-str-regex
  "Performs a case-insensitive regex match"
  [source pattern]
  (boolean (re-find (js/RegExp. pattern "i") source)))
