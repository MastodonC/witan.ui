(ns witan.ui.test.controllers.datastore-test
  (:require  [cljs.test :refer-macros [deftest is testing async]]
             [reagent.core :as r]
             [witan.ui.test.base :as b]
             [witan.ui.controllers.datastore :as data]
             [cljs.core.async :refer [chan <! >! timeout pub sub unsub unsub-all put! close!]])
  (:require-macros [cljs-log.core :as log]
                   [cljs.core.async.macros :refer [go go-loop]]
                   [witan.ui.env :as env :refer [cljs-env]]))

(deftest select-current-test
  (let [u (random-uuid)]
    (data/select-current! u)
    (is (= (b/get-data :app/datastore :ds/current)))))

(deftest reset-properties-test
  (let [u (random-uuid)]
    (b/set-data! {:app/datastore {:ds/file-properties {u 123}}})
    (is (b/get-data :app/datastore :ds/file-properties u))
    (data/reset-properties! u)
    (is (not (b/get-data :app/datastore :ds/file-properties u)))))

(deftest reset-file-edit-metadata-test
  (let [md {:a (random-uuid)}]
    (data/reset-file-edit-metadata! md)
    (is (= md (b/get-data :app/datastore :ds/file-metadata-editing)))
    (data/reset-file-edit-metadata!)
    (is (nil? (b/get-data :app/datastore :ds/file-metadata-editing)))))

(deftest reset-file-edit-metadata-command-test
  (b/set-data! {:app/datastore {:ds/file-metadata-editing-command 123}})
  (is (= 123 (b/get-data :app/datastore :ds/file-metadata-editing-command)))
  (data/reset-file-edit-metadata-command!)
  (is (nil? (b/get-data :app/datastore :ds/file-metadata-editing-command))))

(deftest get-local-file-test
  (let [u (random-uuid)]
    (b/set-data! {:app/datastore {:ds/file-metadata {u 123}}})
    (is (data/get-local-file u))))

(deftest remove-update-from-key-test
  (is (= :foo (data/remove-update-from-key :foo)))
  (is (= :foo/bar (data/remove-update-from-key :foo/bar)))
  (is (= :foo/bar (data/remove-update-from-key :foo.update/bar)))
  (is (= :foo.baz/bar (data/remove-update-from-key :foo.update.baz/bar))))

(deftest md-key->update-command-key-test
  (is (= :foo (data/md-key->update-command-key :foo)))
  (is (= :foo.update/bar (data/md-key->update-command-key :foo/bar)))
  (is (= :foo.baz.update/bar (data/md-key->update-command-key :foo.baz/bar))))

(deftest kw-op->op-fn-test
  (is (= {:foo {:bar 123}} ((data/kw-op->op-fn :assoc [:foo :bar] 123) {})))
  (is (= {:foo {}} ((data/kw-op->op-fn :dissoc [:foo :bar] nil) {:foo {:bar 123}})))
  (is (= {:foo #{:a}} ((data/kw-op->op-fn :update-conj [:foo] :a) {})))
  (is (= {:foo #{}} ((data/kw-op->op-fn :update-disj [:foo] :a) {:foo #{:a}}))))

(deftest kw-op->command-op-fn-test
  (is (= :rm ((data/kw-op->command-op-fn :dissoc [] 123) {})))
  (is (= {:set 123} ((data/kw-op->command-op-fn :assoc [] 123) {})))
  (is (= {:set 123} ((data/kw-op->command-op-fn :assoc [] 123) {:foo 456})))
  (is (= {:conj #{123}} ((data/kw-op->command-op-fn :update-conj [] 123) {})))
  (is (= {} ((data/kw-op->command-op-fn :update-conj [] 123) {:disj #{123}})))
  (is (= {:disj #{123}} ((data/kw-op->command-op-fn :update-disj [] 123) {})))
  (is (= {} ((data/kw-op->command-op-fn :update-disj [] 123) {:conj #{123}}))))

(deftest conform-op-test
  (is (= [:assoc [:a :b] 123] (data/conform-op :assoc [:a :b] 123)))
  (is (= [:dissoc [:a :b] nil] (data/conform-op :assoc [:a :b] ""))))
