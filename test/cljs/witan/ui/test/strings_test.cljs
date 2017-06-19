(ns witan.ui.test.strings-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.strings :as strings]))

(deftest resolve-string-test
  (is (= "Name" (strings/resolve-string :string/name)))
  (is (= "Name" (strings/resolve-string :string/name nil)))
  (is (= "Foo Name" (strings/resolve-string :string/name "Foo")))
  (is (= "Foo Name Type" (strings/resolve-string [:string/name :string/type] "Foo")))
  (is (= "Foo Name Bar Type" (strings/resolve-string [:string/name "Bar" :string/type] "Foo")))
  (is (not (strings/resolve-string :string/foobar))))

(deftest get-string-test
  (is (= "Name" (strings/get-string :string/name)))
  (is (= "## ERROR ##" (strings/get-string :string/foo)))
  (is (= "Name!" (strings/get-string :string/name "!")))
  (is (= "Name! foo" (strings/get-string :string/name "!" "foo"))))
