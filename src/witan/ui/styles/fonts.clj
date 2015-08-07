(ns witan.ui.styles.fonts
  (:require [garden.stylesheet :as gs]))

(def font-face-definitions
  [(gs/at-font-face
    {:font-family "Asap"
     :font-weight 400
     ;;:src "url (\"../fonts/asap/Asap-regular.eot\")"
     ;;:src "url (\"../fonts/asap/Asap-regular.eot?\") format (\"eot\")"
     :src "url (\"../fonts/asap/Asap-regular.woff\") format (\"woff\")"})
   (gs/at-font-face
    {:font-family "Asap"
     :font-weight 700
     ;;:src "url (\"../fonts/asap/Asap-700.eot\")"
     ;;:src "url (\"../fonts/asap/Asap-700.eot?\") format (\"eot\")"
     :src "url (\"../fonts/asap/Asap-700.woff\") format (\"woff\")"})])

(def base-fonts ["Asap" "Helvetica Neue" "Helvetica" "Arial" "sans-serif"])
