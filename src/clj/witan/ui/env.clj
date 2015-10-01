(ns witan.ui.env
  (:require [environ.core :refer [env]]))

;; https://groups.google.com/forum/#!topic/clojurescript/YV-051DmdFE

(defmacro cljs-env [kw]
  (env kw))
