(ns ^:figwheel-always witan.ui.fixtures.new-forecast.view
    (:require [om.core :as om :include-macros true]
              [om-tools.dom :as dom :include-macros true]
              [om-tools.core :refer-macros [defcomponent]]
              [sablono.core :as html :refer-macros [html]]
              [inflections.core :as i]
              [schema.core :as s :include-macros true]
              ;;
              [witan.ui.widgets :as widgets]
              [witan.ui.strings :refer [get-string]]
              [venue.core :as venue]
              )
    (:require-macros [cljs-log.core :as log]))



;;;;

(defn parse-model-string
  [mod-str]
  (let [[_ name version] (re-find #"(.+)\s-\sv(\d)" mod-str)]
    {:name name :version version}))

(defcomponent view
  [cursor owner & opts]
  (render [_]
          (html
           [:div
            [:div.witan-page-heading
             [:h1
              (get-string :new-forecast)]]
            [:div.pure-g#witan-new-forecast-container
             [:div.pure-u-1-2
              [:form.pure-form
               {:on-submit (fn [e]
                             (let [properties (-> cursor :selected-model :model/properties)
                                   {:keys [name version]} (parse-model-string (.-value (om/get-node owner "model-id")))
                                   fc {:name (.-value (om/get-node owner "model-name"))
                                       :description (.-value (om/get-node owner "model-desc"))
                                       :model-name name
                                       :model-version version
                                       :model-props (when properties
                                                      (into {} (map #(->> (str "mod-prop-" (:name %))
                                                                          (om/get-node owner)
                                                                          .-value
                                                                          (hash-map (:name %))) properties)))}]
                               (venue/raise! owner :event/create-forecast fc))
                             (.preventDefault e))}
               [:fieldset
                [:div
                 [:h3 (get-string :forecast-name)]
                 [:input.pure-input-1 {:type "text"
                                       :ref "model-name"
                                       :required true
                                       :placeholder (get-string :new-forecast-name-placeholder)}]]
                [:div
                 [:h3 (get-string :forecast-desc)
                  [:em (str " " (get-string :optional))]]
                 [:input.pure-input-1 {:type "text"
                                       :ref "model-desc"
                                       :placeholder (get-string :new-forecast-desc-placeholder)}]]
                [:div
                 [:h3 (get-string :model)]
                 [:select.pure-input-1-2
                  {:ref "model-id"
                   :on-change #(venue/raise! owner :event/select-model (parse-model-string (.-value (om/get-node owner "model-id"))))}
                  (for [model (:models cursor)]
                    (let [vstr (str (:model/name model) " - v" (:model/version model))]
                      [:option {:key (str vstr "-key") } vstr]))]]
                (if-let [properties (-> cursor :selected-model :model/properties)]
                  [:div
                   [:h3 {:key "mod-props-header"} (str (get-string :model) " " (get-string :properties))]
                   [:div.pure-form-aligned {:key "mod-form-div"}
                    (for [{:keys [name type context enum_values]} properties]
                      (let [params {:ref (str "mod-prop-" name) :key (str "mod-prop-input-" name)}]
                        [:div.pure-control-group
                         {:key (str "mod-prop-" name)}
                         [:label {:for (str "mod-prop-" name) :key (str "mod-prop-label-" name) :style {:width "auto"}} name] ;; FIXME
                         (condp = type
                           "dropdown"
                           [:select.pure-input-1-2
                            params
                            (for [opt enum_values]
                              [:option {:key (str "mod-prop-opt-" opt)} opt])]
                           "text"
                           [:input.pure-input-1-2 (merge params {:placeholder context :required true})]
                           "number"
                           [:input.pure-input-1-2 params])]))]]
                  [:div
                   [:p [:small [:i (get-string :no-model-properties)]]]])
                [:hr.medium]
                [:button.pure-button.button-success {:type "submit"}
                 (if (:working? cursor)
                   [:span [:i.fa.fa-refresh.fa-spin]]
                   [:span [:i.fa.fa-line-chart] (str " " (get-string :create))])]]]]
             [:div.pure-u-1-2]]])))
