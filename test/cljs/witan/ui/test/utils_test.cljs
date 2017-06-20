(ns witan.ui.test.utils-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.utils :as utils]))

(deftest query-param-int-test
  (b/set-data! {:app/route {:route/query {:foo "1"}}})
  (is (= 1 (utils/query-param-int :foo)))
  (b/set-data! {:app/route {:route/query {:foo "x"}}})
  (is (not= 1 (utils/query-param-int :foo)))
  (b/set-data! {:app/route {:route/query {:foo "9"}}})
  (is (= 6 (utils/query-param-int :foo 3 6)))
  (is (= 12 (utils/query-param-int :foo 12 26))))

(deftest render-mustache-test
  (is (= "Hello, foobar!" (utils/render-mustache "Hello, {{world}}!" {:world "foobar"}))))

(deftest sanitize-filename-test
  (is (= "baz" (utils/sanitize-filename "/foo/bar/baz")))
  (is (= "baz" (utils/sanitize-filename "C:\\foo\\bar\\baz"))))

(deftest remove-nil-or-empty-vals-test
  (is (= {:baz 1} (utils/remove-nil-or-empty-vals {:foo [] :bar nil :baz 1})))
  (is (= {:baz 1 :foo ""} (utils/remove-nil-or-empty-vals {:foo "" :bar nil :baz 1}))))
