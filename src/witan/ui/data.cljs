(ns ^:figwheel-always witan.ui.data)

(defonce app-state (atom {}))

(defn set-app-state!
  [m]
  (if (map? m)
    (reset! app-state m)
    (throw (js/Error. "App state must be a map"))))
