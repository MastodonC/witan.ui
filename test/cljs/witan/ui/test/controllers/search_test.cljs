(ns witan.ui.test.controllers.search-test
  (:require  [cljs.test :refer-macros [deftest is testing async]]
             [reagent.core :as r]
             [witan.ui.test.base :as b]
             [witan.ui.controllers.search :as search]
             [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(deftest extract-tags-from-search-term-test
  (is (= [nil nil] (search/extract-tags-from-search-term nil)))
  (is (= ["foo" nil] (search/extract-tags-from-search-term "foo")))
  (is (= ["foo" #{"bar"}] (search/extract-tags-from-search-term "foo tag(bar)")))
  (is (= ["foo baz" #{"bar" "qux"}] (search/extract-tags-from-search-term "foo tag(bar) baz tag(qux)")))
  (is (= ["foo" #{"bar baz"}] (search/extract-tags-from-search-term "foo tag(bar baz)"))))
