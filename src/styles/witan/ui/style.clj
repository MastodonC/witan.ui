(ns witan.ui.style
  (:require [garden.def :refer [defstyles]]
            ;;
            [witan.ui.style.fonts      :as fonts]
            [witan.ui.style.icons      :as icons]
            [witan.ui.style.animations :as anims]
            [witan.ui.style.layout     :as layout]
            [witan.ui.style.login      :as login]
            [witan.ui.style.side       :as side]
            [witan.ui.style.app        :as app]
            [witan.ui.style.dashboard  :as dashboard]
            [witan.ui.style.shared     :as shared]
            [witan.ui.style.panic      :as panic]
            [witan.ui.style.components.topology :as topology]
            [witan.ui.style.components.viz :as viz]
            [witan.ui.style.components.data-select :as data-select]
            [witan.ui.style.components.configuration :as configuration]
            [witan.ui.style.components.results :as results]
            [witan.ui.style.components.rts :as rts]
            [witan.ui.style.components.data :as data]
            [witan.ui.style.components.activities :as activities]


            ;;
            [witan.ui.ext-style.splitjs :as splitjs]))

(defstyles main
  (->>
   [fonts/style
    icons/style
    anims/style
    layout/style
    login/style
    side/style
    app/style
    dashboard/style
    shared/style
    panic/style
    viz/style
    topology/style
    data-select/style
    configuration/style
    results/style
    rts/style
    data/style
    activities/style
    ;;
    splitjs/style]
   ;;
   (reduce concat)
   (vec)))
