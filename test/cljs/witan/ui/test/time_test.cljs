(ns witan.ui.test.time-test
  (:require  [cljs.test :refer-macros [deftest is testing]]
             [witan.ui.test.base :as b]
             [witan.ui.time :as time]
             [cljs-time.core :as t]
             [cljs-time.format :as tf]))

(deftest iso-time-as-moment-test
  (let [now (t/now)
        time-today (tf/unparse (tf/formatter "h:mm A") (t/plus (t/now) (t/hours (time/hours-offset))))
        time-yesterday (tf/unparse (tf/formatter "h:mm A") (t/minus
                                                            (t/plus (t/now) (t/hours (time/hours-offset)))
                                                            (t/days 1)))]
    (is (= (str "Today at " time-today) (time/iso-time-as-moment now)))
    (is (= (str "Yesterday at " time-yesterday) (time/iso-time-as-moment (t/minus now (t/days 1)))))))

(deftest iso-date-as-slash-date-test
  (= "2017/01/02" (time/iso-date-as-slash-date "20170102")))

(deftest jstime->str-test
  (let [now (t/now)]
    (is (re-matches #"[0-9]{8}T[0-9]{6}\.[0-9]{3}Z" (time/jstime->str now)))))

(deftest jstime->vstr-test
  (let [now (t/now)]
    (is (re-matches #"[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9-]{2}\:[0-9-]{2}\:[0-9-]{2}\.[0-9]{3}Z" (time/jstime->vstr now)))))

(deftest jstime->date-str-test
  (let [now (t/now)]
    (is (re-matches #"[0-9]{8}" (time/jstime->date-str now)))))
