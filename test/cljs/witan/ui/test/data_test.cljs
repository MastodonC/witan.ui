(ns witan.ui.test.data-test
  (:require  [cljs.test :refer-macros [deftest is testing async]]
             [reagent.core :as r]
             [witan.ui.test.base :as b]
             [witan.ui.data :as data]
             [witan.ui.time :as time]
             [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(deftest atomize-map-test
  (let [foo {:a 1 :b "two" :c {:x :y}}
        bar {:a (r/atom 1) :b (r/atom "two") :c (r/atom {:x :y})}
        atomized (data/atomize-map foo)
        deatomized (data/deatomize-map bar)]
    (is (= (every? #(= (type %) reagent.ratom/RAtom) (vals atomized))))
    (is (= (every? (fn [[k v]] (= (get foo k) (deref v))) atomized)))
    (is (= foo deatomized))))

(deftest app-state-tests
  (testing "get-app-state"
    (b/set-data! {:app/user {:bar 1}})
    (is (= {:bar 1} (data/get-app-state :app/user)))
    (is (= 1 (data/get-in-app-state :app/user :bar))))
  (testing "swap-app-state"
    (data/swap-app-state! :app/user update :bar inc)
    (is (= 2 (data/get-in-app-state :app/user :bar))))
  (testing "reset-app-state"
    (data/reset-app-state! :app/user {:bar 3})
    (is (= 3 (data/get-in-app-state :app/user :bar)))))

;; TODO need to fix async testing!??!?!?!
;; https://github.com/clojure/clojurescript/wiki/Testing#async-testing
#_(deftest pubsub-test
    (let [result (atom nil)]
      (async done
             (data/subscribe-topic :foo #(reset! result %))
             (go
               (data/publish-topic :foo 123)
               (loop [tries 5]
                 (if (pos? tries)
                   (if-not (= 123 @result)
                     (recur (dec tries))
                     (done))
                   (done)))))
      (is (= 123 @result))))

(deftest async-test
  (let [result (atom nil)
        foo (chan)]
    (async done
           (go
             (>! foo 123))
           (go
             (reset! result (<! foo))
             (is (= 123 @result))
             (done)))))

(deftest async-test2
  (let [result (atom nil)
        subscriber (chan)
        payload {:topic :foo :args 123}
        cb #(reset! result %)]
    (async done
           (sub data/publication :foo subscriber)
           (go
             (cb (<! subscriber))
             (is (= payload @result))
             (done))
           (go
             (>! data/publisher payload)))))
