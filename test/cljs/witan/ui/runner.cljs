(ns witan.ui.runner
  (:require[doo.runner :refer-macros [doo-all-tests doo-tests]]
           [witan.ui.test.utils-test]
           [witan.ui.test.title-test]
           [witan.ui.test.time-test]
           [witan.ui.test.strings-test]
           [witan.ui.test.route-test]
           [witan.ui.test.data-test]
           ;;
           [witan.ui.route :as route]
           [accountant.core :as accountant]))

(defonce init
  (do (accountant/configure-navigation! {:nav-handler route/dispatch-path!
                                         :path-exists? route/path-exists?})
      (.setUseFragment accountant/history true)))

(doo-tests 'witan.ui.test.utils-test
           'witan.ui.test.title-test
           'witan.ui.test.time-test
           'witan.ui.test.strings-test
           'witan.ui.test.route-test
           'witan.ui.test.data-test)
