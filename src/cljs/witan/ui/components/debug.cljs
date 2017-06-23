(ns witan.ui.components.debug
  (:require [reagent.core :as re]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [inflections.core :as i]
            [cljs.pprint :as p]
            [cljs.reader :as r]
            [cljsjs.parinfer])
  (:require-macros [cljs-log.core :as log]
                   [devcards.core :as dc :refer [defcard]]))

(defn- pp-str [s]
  (try
    (-> s
        r/read-string
        (p/write :right-margin        80
                 :stream              nil
                 :miser-width         120
                 :dispatch            p/simple-dispatch))
    (catch js/Error e
      (p/write {:error e
                :plain s}
               :right-margin        80
               :stream              nil
               :miser-width         120
               :dispatch            p/simple-dispatch))))

(def format-edn (comp pp-str
                      #(.-text %)
                      js/parinfer.indentMode))

(defn view
  []
  (let [selected-key (re/atom (str (first (keys data/app-state))))
        style {:width "100%" :height "100%"}]
    (fn []
      (let [edn-str (format-edn (pr-str (data/get-app-state (keyword (subs @selected-key 1)))))]
        (log/debug edn-str)
        [:div
         {:style style}
         [:select
          {:on-change #(reset! selected-key (.. % -target -value))}
          (doall (for [k (keys data/app-state)]
                   [:option
                    {:value (str k)
                     :key (str k)} (str k)]))]
         [:div
          {:style style}
          [:textarea
           {:readonly true
            :style style
            :value edn-str}]]]))))
