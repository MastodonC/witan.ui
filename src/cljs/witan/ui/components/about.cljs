(ns witan.ui.components.about
  (:require [reagent.core :as re]
            [sablono.core :as sab :include-macros true]
            [witan.ui.data :as data]
            [witan.ui.route :as route]
            [witan.ui.components.shared :as shared]
            [witan.ui.components.icons :as icons]
            [witan.ui.strings :refer [get-string]]
            [witan.ui.controller :as controller]
            [witan.ui.utils :as utils]
            [witan.ui.time :as time]
            [goog.string :as gstring]
            [inflections.core :as i])
  (:require-macros [cljs-log.core :as log]
                   [witan.ui.env :as env :refer [cljs-env]]))

(defn view
  []
  (let []
    (fn []
      [:div.padded-content.text-center
       [:h1 (get-string :string/witan)]
       [:h2 (get-string :string/witan-tagline)]
       [:p "by "
        [:a {:href "http://mastodonc.com"} "Mastodon C"]]
       [:div
        [:div [:i (or (cljs-env :build-sha) "SHA unknown")]]
        [:div [:i (or (cljs-env :build-dt) "Build time unknown")]]]
       [:hr]
       [:h2 (get-string :string/contact-us)]
       [:p (get-string :string.about/contact-us-1)]
       [:a {:href (str "mailto:" (get-string :string/support-email))}
        (get-string :string/support-email)]
       [:p (get-string :string.about/contact-us-2)]
       [:a {:href (str "mailto:" (get-string :string/enquiries-email))}
        (get-string :string/enquiries-email)]
       [:hr]
       [:h2 (get-string :string.about/attributions)]
       [:p (get-string :string.about/attributions-text-1)]
       [:p (get-string :string.about/attributions-text-2)]])))
