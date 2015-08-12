(ns ^:figwheel-always witan.ui.core
    (:require[om.core :as om :include-macros true]
             [om-tools.dom :as dom :include-macros true]
             [om-tools.core :refer-macros [defcomponent]]))

(enable-console-print!)

(defonce app-state (atom {}))

(defcomponent
  menu
  [cursor owner]
  (render [_]
          (dom/div {:class "pure-menu"}
                   (dom/a {:class "pure-menu-heading" :href "#"} "Witan")
                   (dom/hr)
                   (dom/ul {:class "pure-menu-list"}
                           (dom/li {:class "witan-menu-item pure-menu-item"}
                                   (dom/a {:href "#headings"} "Headings"))
                           (dom/li {:class "witan-menu-item pure-menu-item"}
                                   (dom/a {:href "#buttons"} "Buttons"))))))

(defcomponent
  title
  [cursor owner]
  (render [_]
          (dom/div {:id "witan-page-title" :class "pure-g"}
                   (dom/div {:class "pure-u-1"}
                            (dom/h1 "Witan")
                            (dom/h2 "Open City Planning")
                            (dom/h2 "Pattern Library")))))

(defcomponent
  main
  [cursor owner]
  (render [_]
          (dom/div
           (om/build title cursor)
           (dom/div {:id "witan-main-content"}
                    ;; headers
                    (dom/a {:name "headings"})
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/h1 "Heading One"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/h1 \"Heading One\")")))
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/h2 "Heading Two"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/h2 \"Heading Two\")")))
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/h3 "Heading Three"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/h3 \"Heading Three\")")))
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/h4 "Heading Four"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/h4 \"Heading Four\")")))
                    ;; buttons
                    (dom/hr)
                    (dom/a {:name "buttons"})
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/button {:class "pure-button"} "Normal Button"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/button {:class \"pure-button\"} \"Normal Button\")")))
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/button {:class "pure-button pure-button-primary"} "Primary Button"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/button {:class \"pure-button pure-button-primary\"} \"Primary Button\")"))
                             )
                    (dom/div {:class "pure-g witan-pattern-example"}
                             (dom/div {:class "pure-u-1-2"}
                                      (dom/button {:class "pure-button pure-button-disabled"} "Disabled Button"))
                             (dom/div {:class "pure-u-1-2 witan-pattern-example-code"}
                                      (dom/pre "(dom/button {:class \"pure-button pure-button-disabled\"} \"Disabled Button\")"))
                             )))))

(if-let [menu-element (. js/document (getElementById "witan-menu"))]
  (om/root
   menu
   app-state
   {:target menu-element}))

(if-let [main-element (. js/document (getElementById "witan-main"))]
  (om/root
   main
   app-state
   {:target main-element}))


(defn on-js-reload [])
