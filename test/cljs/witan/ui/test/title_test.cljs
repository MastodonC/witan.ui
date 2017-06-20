(ns witan.ui.test.title-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.title :as title]))

(deftest set-title-test
  (testing "Not logged in..."
    (title/set-title! "Foobar")
    (is (= "Witan" (.. js/window -document -title))))
  (testing "Logged in"
    (b/set-data! {:app/user {:kixi.user/id "12345"}})
    (title/set-title! "Foobar")
    (is (= "Witan - Foobar" (.. js/window -document -title)))
    (title/set-title! "This" "is" "a" "thing")
    (is (= "Witan - This is a thing" (.. js/window -document -title)))))
