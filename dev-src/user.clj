(ns user
  (:require [figwheel-sidecar.repl-api :as figwheel]))

;; Let Clojure warn you when it needs to reflect on types, or when it does math
;; on unboxed numbers. In both cases you should add type annotations to prevent
;; degraded performance.
(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn go []
  (figwheel/start-figwheel! :dev))

(defn go-devcards []
  (figwheel/start-figwheel! :dev :devcards))

(defn stop []
  (figwheel/stop-figwheel!))

(def browser-repl
  figwheel/cljs-repl)
