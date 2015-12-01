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
             {:key "page-heading"}
             [:h1
              (get-string :new-forecast)]]
            [:div.pure-g#witan-new-forecast-container
             {:key "container"}
             [:div.pure-u-1-2
              {:key "left"}
              [:form.pure-form
               {:on-submit (fn [e]
                             (let [properties (-> cursor :selected-model :model/properties)
                                   {:keys [name version]} (parse-model-string (.-value (om/get-node owner "model-id")))
                                   fc {:name (.-value (om/get-node owner "model-name"))
                                       :description (.-value (om/get-node owner "model-desc"))
                                       :public? (.-checked (om/get-node owner "model-public"))
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
                 {:key "forecast-name"}
                 [:h3 {:key "title"}
                  (get-string :forecast-name)]
                 [:input.pure-input-1 {:type "text"
                                       :ref "model-name"
                                       :key "model-name"
                                       :required true
                                       :placeholder (get-string :new-forecast-name-placeholder)}]]
                [:div
                 {:key "forecast-description"}
                 [:h3 {:key "title"}
                  (get-string :forecast-desc)
                  [:em (str " " (get-string :optional))]]
                 [:input.pure-input-1 {:type "text"
                                       :ref "model-desc"
                                       :key "model-desc"
                                       :placeholder (get-string :new-forecast-desc-placeholder)}]]
                [:div
                 {:key "forecast-public"}
                 [:h3 {:key "title"}
                  (get-string :forecast-public?)]
                 [:input.pure-input {:type "checkbox"
                                     :ref "model-public"
                                     :key "model-public"}]
                 [:div {:key "explanation"} [:small (get-string :forecast-public?-explain)]]]
                [:div
                 {:key "forecast-model"}
                 [:h3 (get-string :model)]
                 [:select.pure-input-1
                  {:ref "model-id"
                   :key "model-id"
                   :on-change #(venue/raise! owner :event/select-model (parse-model-string (.-value (om/get-node owner "model-id"))))}
                  (for [model (:models cursor)]
                    (let [vstr (str (:model/name model) " - v" (:model/version model))]
                      [:option {:key (str vstr "-key") } vstr]))]]
                (if-let [properties (-> cursor :selected-model :model/properties)]
                  [:div
                   {:key "forecast-model-properties"}
                   [:h3
                    {:key "mod-props-header"}
                    (str (get-string :model) " " (get-string :properties))]
                   [:div.pure-form-aligned
                    {:key "mod-form-div"}
                    (for [{:keys [name type context enum_values]} properties]
                      (let [params {:ref (str "mod-prop-" name) :key (str "mod-prop-input-" name)}]
                        [:div.pure-control-group
                         {:key (str "mod-prop-" name)
                          :style {:vertical-align "top"}}
                         [:label {:for (str "mod-prop-" name)
                                  :key (str "mod-prop-label-" name)
                                  :style {:width "auto"
                                          :vertical-align "top"
                                          :margin-top "10px"}} name] ;; FIXME
                         (condp = type
                           "dropdown"
                           [:div.pure-input-1-2
                            {:style {:display "inline-block"}
                             :key (str "mod-prop-dropdown-" name)}
                            [:select
                             params
                             (for [opt enum_values]
                               [:option
                                {:key (str "mod-prop-opt-" opt)}
                                opt])]
                            [:div
                             {:key "context"}
                             [:small.text-gray {:style {:margin-left "1em"}} context]]]
                           "text"
                           [:input.pure-input-1-2
                            {:key (str "mod-prop-text-" name)}
                            (merge params {:placeholder context :required true})]
                           "number"
                           [:input.pure-input-1-2
                            {:key (str "mod-prop-number-" name)}
                            (merge params {:placeholder context :required true})])]))]]
                  [:div
                   {:key "no-model-props"}
                   [:p [:small [:i (get-string :no-model-properties)]]]])
                [:hr.medium
                 {:key "spacer"}]
                [:button.pure-button.button-success
                 {:type "submit"
                  :key "button"}
                 (if (:working? cursor)
                   [:span [:i.fa.fa-refresh.fa-spin]]
                   [:span [:i.fa.fa-line-chart] (str " " (get-string :create))])]]]]
             [:div.pure-u-1-2
              {:key "right"}
              [:div#model-information
               [:h2 {:key "title"}
                (get-string :about-model)]
               [:h3 {:key "name"}
                (-> cursor :selected-model :model/name)
                [:div
                 {:key "versions"}
                 [:em.text-gray
                  (get-string :forecast-version" " (-> cursor :selected-model :model/version))]]
                [:p
                 {:key "description"}
                 [:h4.text-gray {:key "model-desc-value"
                                 :dangerouslySetInnerHTML
                                 {:__html (-> cursor :selected-model :model/description)}}]]]]]]])))
