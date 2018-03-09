(ns witan.ui.style.dashboard
  (:require [garden.units :refer [px em percent]]
            [witan.ui.style.colour :as colour]
            [witan.ui.style.values :as values]))

(def style [[:.dashboard
             {:height (percent 100)
              :min-width (px 800)}
             [:.content
              {:position :absolute
               :top values/app-peripheral-height
               :padding 0
               :bottom 0
               :left 0
               :right 0
               :overflow-y :auto
               :overflow-x :hidden}
              [:h1
               {:display :inline-block
                :font-weight 400}]
              [:#container
               {:width (percent 99)}]]

             [:.dash-buttons
              {:position :absolute
               :z-index 1
               :top 0
               :right 0
               :display :flex
               :justify-content :center
               :height (percent 100)
               :padding-right (px 10)}
              [:.workspace-create :.data-upload
               {:background-color colour/button-create
                :color colour/body-bg}]
              [:.workspace-view
               {:background-color colour/button-view}]]

             [:.dash-pagination
              {:margin-top (px 10)
               :margin-bottom (px 10)}]]

            ;;;;;;;;;;;;;;;;;;;;;
            [:#rts-no-requests
             {:text-align :center
              :padding (em 1)}]
            [:#rts-functions
             {:padding [[(px 0) (px 10)]]}
             [:div.buttons
              [:div
               [:span
                {:padding (em 1)}]
               [:.description
                {:font-size (em 1.1)}]]]]])
