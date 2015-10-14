(ns witan.ui.strings
  (:require-macros
   [cljs-log.core :as log]))

(def strings
  {:witan                         "Witan"
   :witan-tagline                 "Make more sense of your city"
   :witan-title                   "Witan for London"
   :forecast                      "Forecast"
   :new-forecast                  "Create New Forecast"
   :new-forecast-name-placeholder "Enter a name for this forecast"
   :new-forecast-desc-placeholder "Enter a description for this forecast"
   :filter                        "Filter"
   :model                         "Model"
   :properties                    "Properties"
   :forecast-name                 "Name"
   :forecast-type                 "Type"
   :forecast-owner                "Owner"
   :forecast-version              "Version"
   :forecast-lastmodified         "Last Modified"
   :forecast-desc                 "Description"
   :optional                      "(optional)"
   :sign-in                       "Sign In"
   :email                         "Email"
   :password                      "Password"
   :forgotten-question            "forgotten your password?"
   :forgotten-password            "Forgotten Password"
   :forgotten-instruction         "Please enter your email address. If it matches one in our system we'll send you reset instructions."
   :reset-submitted               "Thanks. Your password reset request has been received."
   :reset-password                "Reset Password"
   :back                          "Back"
   :thanks                        "Thanks"
   :signing-in                    "Signing in..."
   :sign-in-failure               "There was a problem with your details. Please try again."
   :api-failure                   "There was a problem with the service. Please try again. If the problem persists, please contact us." ;; TODO add link?
   :create                        "Create"
   :logout                        "Log Out"
   :no-model-properties           "This model has no properties to configure"
   })

(defn get-string
  ""
  [keywd]
  (if (contains? strings keywd)
    (keywd strings)
    (do
      (log/severe "Failed to find string " (str keywd))
      "## ERROR ##")))
