(ns witan.ui.test.base
  (:require [witan.ui.data :as data]))

(defn set-data!
  [new-data]
  (doall (for [[k v] new-data]
           (data/reset-app-state! k v)))

  (defn get-data
    [& path]
    (apply data/get-in-app-state path)))
