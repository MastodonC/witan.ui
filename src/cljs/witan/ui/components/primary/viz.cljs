(ns witan.ui.components.primary.viz
  (:require [reagent.core :as r]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.data :as data]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [sablono.core :as sab :include-macros true]
            [clojure.string :as str]
            [goog.string :as gstr]
            [inflections.core :as i]
            cljsjs.dialog-polyfill
            cljsjs.clipboard)
  (:require-macros [devcards.core :as dc :refer [defcard]]
                   [cljs-log.core :as log])
  (:import goog.Uri))

;; color presets used by viz
;; https://github.com/thi-ng/color/blob/master/src/presets.org
(def cat10
  ["#1f77b4"
   "#ff7f0e"
   "#2ca02c"
   "#d62728"
   "#9467bd"
   "#8c564b"
   "#e377c2"
   "#7f7f7f"
   "#bcbd22"
   "#17becf"])

(def id "viz")
(defonce ready? (r/atom false))
(defonce settings-open? (r/atom false))
(defonce last-location (r/atom ""))
(defonce pymo (atom nil))
(defonce last-opts (atom nil))
(def default-viz-options
  {:spinner false
   :settings false})

(def default-style :table)
(def viz-lookup
  {"table"    {:style :table}
   "lineplot" {:style :lineplot
               :params {:filter (gstr/urlEncode "age=10,sex=M")
                        "args[x]" "year"
                        "args[y]" "popn"}}})

(defn on-resize
  [obj]
  (when @pymo
    (.sendMessage @pymo "resize" 0)))

(defonce add-hooks
  (do
    (log/debug "Adding resize event listener...")
    (.addEventListener js/window "resize" on-resize)))

(defn m->query-params
  [m]
  (let [qd (goog.Uri.QueryData.)]
    (run! (fn [[k v]] (.add qd (name k) v)) m)
    (gstr/urlDecode (.toString qd))))

(defn location->path
  [locations style & [opts]]
  (str "http://" (get data/config :viz/address) "/?"
       (m->query-params (merge {:data (str/join "," locations) :style (name style)} opts))))

(defn make-iframe
  [locations style & [opts]]
  (let [p (location->path locations style (merge default-viz-options opts))]
    (log/debug "Creating iFrame for " p)
    (reset! pymo (.Parent js/pym id p #js {}))
    (reset! last-location p)
    (.onMessage @pymo "ready" (fn [_]
                                (log/debug "Viz is ready")
                                (reset! ready? true)))
    (.onMessage @pymo "location" (fn [location]
                                   (reset! last-location (-> location
                                                             (str/replace #"spinner=false" "spinner=true")
                                                             (str/replace #"settings=false" "settings=true")))))))

(defn reset-iframe
  [locations style params]
  (reset! ready? false)
  (let [args (m->query-params (merge {:data (str/join "," locations) :style (name style)}
                                     (merge default-viz-options params)))]
    (log/debug "Resetting iframe to " args)
    (.sendMessage @pymo "arguments" args)
    (reset! settings-open? false)))

(defn clipboard-button [content text]
  (let [clipboard-atom (atom nil)]
    (r/create-class
     {:display-name "clipboard-button"
      :component-did-mount
      #(let [clipboard (new js/Clipboard (r/dom-node %))]
         (reset! clipboard-atom clipboard))
      :component-will-unmount
      #(when-not (nil? @clipboard-atom)
         (.destroy @clipboard-atom)
         (reset! clipboard-atom nil))
      :reagent-render
      (fn []
        [:button.pure-button
         {:data-clipboard-text @last-location
          :key "clipboard-btn"}
         (content)])})))

(defn settings-button
  []
  [:button.pure-button
   {:key "settings-btn"
    :on-click #(do
                 (swap! settings-open? not)
                 (.sendMessage @pymo "toggle-settings" 0))}
   (if @settings-open?
     (icons/close :small :dark)
     (icons/cog :small :dark))])

(defn view
  []
  (r/create-class
   {:component-will-unmount
    (fn [this])
    :component-did-mount
    (fn [this]
      (when @last-opts
        (let [{:keys [result/location viz/style viz/params]} @last-opts
              params (if params params (when style (:params (get viz-lookup (name style)))))
              locations (when location (if (coll? location) location [location]))]
          (make-iframe locations (or style default-style) params))))
    :reagent-render
    (fn []
      (let [{:keys [workspace/current-viz]} (data/get-app-state :app/workspace)
            {:keys [result/location viz/style viz/params] :as opts} current-viz
            params (if params params (when style (:params (get viz-lookup (name style)))))
            locations (when location (if (coll? location) location [location]))]
        [:div#viz-container
         (when (not= @last-opts opts)
           (reset! last-opts opts)
           (log/debug "Changing visualisation data...")
           (if (not @pymo)
             (do (log/debug "Making iframe")
                 (make-iframe locations (or style default-style) params))
             (reset-iframe locations (or style default-style) params)))
         (if (and (not-empty locations) @ready?)
           [:div.buttons.pure-form
            (for [location-idx (range (count locations))]
              (let [location (nth locations location-idx)]
                [:span
                 {:style {:border-bottom (str "2px " (nth cat10 location-idx) " solid")}
                  :key (str "location" location-idx)}
                 (str
                  (inc location-idx) ": "
                  (-> location
                      (as-> l (subs l (inc (.lastIndexOf l "/"))))
                      (as-> l (subs l 0 (.lastIndexOf l ".")))
                      (i/capitalize)))]))
            [:select.pure-input
             {:key "select-a-style"
              :on-change #(controller/raise! :workspace/change-visualisation-style
                                             (get viz-lookup (.. % -target -value)))
              :value (when style (name style))}
             [:option {:value "table"
                       :key   "table"}    "Table"]
             [:option {:value "lineplot"
                       :key   "lineplot"} "Line Chart"]]
            [clipboard-button
             #(icons/link :small :dark)
             (location->path locations (or style default-style) params)]
            [settings-button]]
           [:div.buttons])
         (when (empty? locations)
           [:div#viz-placeholder.text-center
            (icons/pie-chart :large :dark)
            [:h2 (get-string :string/no-viz-selected)]
            [:h3 (get-string :string/no-viz-selected-desc)]])
         [:div#loading
          {:style {:background-color "transparent"
                   :z-index "1"
                   :pointer-events "none"
                   :display (if (or (empty? locations) @ready?) "none" "inherit")
                   }}
          (icons/loading :large)]
         [:div {:id id
                :style {:display (if (or (empty? locations) @ready?) "inherit" "none")}
                }]]))}))
