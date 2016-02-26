(ns witan.ui.dashboard.workspaces
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]
            ;;
            [witan.ui.shared  :as shared]
            [witan.ui.utils   :as utils]
            [witan.ui.icons   :as icons]
            [witan.ui.route   :as route]
            [witan.ui.strings :refer [get-string]])
  (:require-macros [cljs-log.core :as log]))

(defui Main
  static om/IQuery
  (query [this]
         [:wd/selected-id])
  Object
  (render [this]
          (let [{:keys [wd/selected-id]} (om/props this)
                icon-fn #(vector :div.text-center (icons/workspace :dark))]
            (sab/html [:div.dashboard
                       [:div.heading
                        [:h1 (get-string :string/workspace-dash-title)]
                        (shared/search-filter (get-string :string/workspace-dash-filter) nil)]
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
