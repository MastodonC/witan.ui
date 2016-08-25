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
            cljsjs.dialog-polyfill
            cljsjs.clipboard)
  (:require-macros [devcards.core :as dc :refer [defcard]]
                   [cljs-log.core :as log])
  (:import goog.Uri))

(def id "viz")
(defonce ready? (r/atom false))
(defonce pymo (atom nil))
(defonce last-opts (atom nil))
(def default-viz-options
  {:spinner false})

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
    (log/info "Adding resize event listener...")
    (.addEventListener js/window "resize" on-resize)))

(defn m->query-params
  [m]
  (let [qd (goog.Uri.QueryData.)]
    (run! (fn [[k v]] (.add qd (name k) v)) m)
    (gstr/urlDecode (.toString qd))))

(defn location->path
  [location style & [opts]]
  (let [ss (name style)]
    (str "http://localhost:3448/?" (m->query-params (merge {:data location :style (name style)} opts)))))

(defn make-iframe
  [location style & [opts]]
  (let [p (location->path location style (merge default-viz-options opts))]
    (log/info "Creating iFrame for " p)
    (reset! pymo (.Parent js/pym id p #js {}))
    (.onMessage @pymo "ready" (fn [_]
                                (log/debug "Viz is ready")
                                (reset! ready? true)))))

(defn reset-iframe
  [location style params]
  (reset! ready? false)
  (let [args (m->query-params (merge {:data location :style (name style)} params))]
    (log/debug "Resetting iframe to " args)
    (.sendMessage @pymo "arguments" args)))

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
         {:data-clipboard-text text}
         (content)])})))

(defn view
  []
  (r/create-class
   {:component-will-unmount
    (fn [this])
    :component-did-mount
    (fn [this]
      (when @last-opts
        (let [{:keys [result/location viz/style viz/params]} @last-opts]
          (make-iframe location (or style default-style) params))))
    :reagent-render
    (fn []
      (let [{:keys [workspace/current-viz]} (data/get-app-state :app/workspace)
            {:keys [result/location viz/style viz/params] :as opts} current-viz
            location' (when location
                        (subs location (+ 1 (.lastIndexOf location "/"))))
            select (fn [{:keys [value] :as opts}]
                     (if (and style (= value (name style)))
                       (assoc opts :selected true)
                       opts))]
        [:div#viz-container
         (when (not= @last-opts opts)
           (reset! last-opts opts)
           (log/debug "Changing visualisation data...")
           (if (not @pymo)
             (do (log/debug "Making iframe")
                 (make-iframe location (or style default-style)))
             (reset-iframe location (or style default-style) params)))
         (if (and location @ready?)
           [:div.buttons.pure-form
            [:span location']
            [:select.pure-input
             {:on-change #(controller/raise! :workspace/change-visualisation-style
                                             (get viz-lookup (.. % -target -value)))}
             [:option (select {:value "table"})    "Table"]
             [:option (select {:value "lineplot"}) "Line Chart"]]
            [clipboard-button
             #(icons/link :small :dark)
             (location->path location (or style default-style) params)]]
           [:div.buttons])
         (if-not location
           [:div#viz-placeholder.text-center
            (icons/pie-chart :large :dark)
            [:h2 (get-string :string/no-viz-selected)]
            [:h3 (get-string :string/no-viz-selected-desc)]])
         [:div#loading
          {:style {:background-color "transparent"
                   :height "10%"
                   :display (if (or (not location) @ready?) "none" "inherit")}}
          (icons/loading :large)]
         [:div {:id id
                :style {:display (if (or (not location) @ready?) "inherit" "none")}
                }]]))}))
