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
            [witan.ui.style.components.topology :as topology]
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
    topology/style
    ;;
    splitjs/style]
   ;;
   (reduce concat)
   (vec)))
