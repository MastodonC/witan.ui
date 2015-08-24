(ns ^:figwheel-always witan.ui.util)

(defn contains-str
  "Performs a case-insensitive substring match"
  [source match]
  (not= -1 (.indexOf (.toLowerCase source) (.toLowerCase match))))

(defn contains-str-regex
  "Performs a case-insensitive regex match"
  [source pattern]
  (boolean (re-find (js/RegExp. pattern "i") source)))

(defn goto-window-location!
  [location]
  (set! (.. js/document -location -href) location))
