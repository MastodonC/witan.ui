(ns witan.ui.primary.topology
  (:require [om.next :as om :refer-macros [defui]]
            [sablono.core :as sab]))

(defui Main
  static om/IQuery
  (query [this]
         [{:app/counter [:db/id :app/title :app/count]}])
  Object
  (render [this]
          (sab/html
           (let [{:keys [app/title app/count] :as entity}
                 (get-in (om/props this) [:app/counter 0])]
             [:div
              [:span (str "Count: " count)]
              [:button {:on-click
                        (fn [e]
                          (om/transact! this `[(app/increment ~entity)]))}
               "Click me pls foo"]]))))
