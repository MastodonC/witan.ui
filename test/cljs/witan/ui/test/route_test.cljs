(ns witan.ui.test.route-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.route :as route]))

(deftest query-string->map-test
  (is (= {:foo "1" :bar "2"} (route/query-string->map "http://localhost?foo=1&bar=2"))))

(deftest dispatch-path-test
  (route/dispatch-path! "/" false)
  (is (= :app/data-dash (b/get-data :app/route :route/path)))  ;; default
  (route/dispatch-path! "/app" false)
  (is (= :app/data-dash (b/get-data :app/route :route/path)))
  (route/dispatch-path! "/app/data/dashboard" false)
  (is (= :app/data-dash (b/get-data :app/route :route/path)))
  (route/dispatch-path! "/app/data/create" false)
  (is (= :app/data-create (b/get-data :app/route :route/path)))
  (route/dispatch-path! "/reset" false)
  (is (= :app/data-dash (b/get-data :app/route :route/path)))
  (route/dispatch-path! "/invite" false)
  (is (= :app/data-dash (b/get-data :app/route :route/path)))
  (route/dispatch-path! "/app/workspace/12345?q=1" false)
  (is (= {:route/path :app/workspace
          :route/params {:id "12345"}
          :route/address "/app/workspace/12345"
          :route/query {}} ;; No query params because of a bug; `(.getToken accountant/history)` in witan.ui.route fixes this
         (b/get-data :app/route))))

#_(deftest navigate-test
    (route/navigate! :app/workspace {:id "54321"})
    (is (= {:route/path :app/workspace
            :route/params {:id "54321"}
            :route/address "/app/workspace/54321"
            :route/query {}} ;; No query params because of a bug; `(.getToken accountant/history)` in witan.ui.route fixes this
           (b/get-data :app/route))))
