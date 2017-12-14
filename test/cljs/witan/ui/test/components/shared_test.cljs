(ns witan.ui.test.components.shared-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.components.shared :as shared]))

(deftest pagination-test
  (let [pages (rand-nth (range 4 20))
        current-page (rand-nth (range 1 (inc pages)))
        m {:page-blocks (range 1 (inc pages))
           :current-page current-page}
        f identity
        output ((shared/pagination m f) m f)]
    (is (= :div.flex-start (first output)))
    (is (= 3 (dec (count output))))
    (is (= pages (count (nth output 2))))))
