(ns witan.ui.components.dashboard.workspaces
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.components.shared  :as shared]
            [witan.ui.components.dashboard.shared  :as shared-dash]
            [witan.ui.utils   :as utils]
            [witan.ui.components.icons   :as icons]
            [witan.ui.route   :as route]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [cljs-log.core :as log]))

(defmulti button-press
  (fn [_ event] event))

(defmethod button-press :view
  [selected-id _]
  (route/navigate! :app/workspace {:id selected-id}))

(defmethod button-press :create
  [_ _]
  (route/navigate! :app/create-workspace))

(defui Main
  static om/IQuery
  (query [this]
         [:wd/selected-id])
  Object
  (render [this]
          (let [{:keys [wd/selected-id]} (om/props this)
                icon-fn #(vector :div.text-center (icons/workspace :dark))
                buttons (concat (when selected-id [{:id :view :icon icons/open :txt :string/view :class "workspace-view"}])
                                [{:id :create :icon icons/plus :txt :string/create :class "workspace-create"}])]
            (sab/html [:div.dashboard
                       (shared-dash/header {:title :string/workspace-dash-title
                                            :filter-txt :string/workspace-dash-filter
                                            :filter-fn nil
                                            :buttons buttons
                                            :on-button-click (partial button-press selected-id)})
                       [:div.content
                        (shared/table {:headers [{:content-fn icon-fn   :title ""              :weight 0.03}
                                                 {:content-fn :name     :title "Name"          :weight 0.57}
                                                 {:content-fn :owner    :title "Owner"         :weight 0.2}
                                                 {:content-fn :modified :title "Last Modified" :weight 0.2}]
                                       :content [{:name "Workspace for Camden Population"   :id 1 :owner "Bob"     :modified "Yesterday, 2pm"}
                                                 {:name "Workspace for Hounslow Population" :id 2 :owner "Alice"   :modified "4th Jan, 4.15pm"}
                                                 {:name "Workspace for Barnet Population"   :id 3 :owner "Charles" :modified "12th Jan, 10.24am"}]
                                       :selected?-fn #(= (:id %) selected-id)
                                       :on-select #(om/transact! this `[(wd/select-row! {:id ~(:id %)})])
                                       :on-double-click #(route/navigate! :app/workspace {:id (:id %)})})]]))))
