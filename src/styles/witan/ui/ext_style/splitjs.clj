(ns witan.ui.ext-style.splitjs
  (:require [witan.ui.style.util :as util]
            [witan.ui.style.colour :as colour]
            [garden.units :refer [percent]]))

(def style [[:body
             {:box-sizing :border-box}]
            [:.split
             {:overflow-y :auto
              :overflow-x :hidden}
             ^:prefix {:box-sizing :border-box}]
            [:.gutter
             {:background-color colour/gutter
              :background-repeat :no-repeat
              :background-position (percent 50)}
             [:&.gutter-vertical
              {:cursor :row-resize
               :background-image (util/url "../img/horizontal.png")}]]
            [:.split.split-horizontal :.gutter.gutter-horizontal]
            {:height (percent 100)
             :float :left}])
