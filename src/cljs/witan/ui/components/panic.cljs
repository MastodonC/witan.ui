(ns witan.ui.components.panic
  (:require [witan.ui.components.icons :as icons]
            [reagent.core :as r])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn panic-screen
  [message]
  (log/info "Panic screen is showing." message)
  (let [countdown (r/atom 20)]
    (fn [_]
      (js/setTimeout #(swap! countdown dec) 1000)
      (if (zero? @countdown)
        (set! (.-location js/window) "/")
        [:div.panic-screen
         [:h1 "Sorry!"]
         [:div [:strong "It's not you, it's us. We've messed something up."]]
         [:div [:strong "Have another go and if it persists, please get in touch."]]
         [:br]
         [:br]
         (icons/bug :x-large :dark)
         [:h2 (str "Restarting app in " @countdown " seconds...")]
         [:p
          [:span.error message]]]))))
