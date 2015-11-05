(ns witan.ui.strings
  (:require-macros
   [cljs-log.core :as log]))

(def strings
  {:witan                          "Witan"
   :witan-tagline                  "Make more sense of your city"
   :witan-title                    "Witan for London"
   :forecast                       "Forecast"
   :new-forecast                   "Create New Forecast"
   :new-forecast-name-placeholder  "Enter a name for this forecast"
   :new-forecast-desc-placeholder  "Enter a description for this forecast"
   :filter                         "Filter"
   :search                         "Search"
   :model                          "Model"
   :properties                     "Properties"
   :forecast-name                  "Name"
   :forecast-type                  "Type"
   :forecast-owner                 "Owner"
   :forecast-version               "Version"
   :forecast-lastmodified          "Last Modified"
   :forecast-desc                  "Description"
   :model-publisher                "Publisher"
   :optional                       "(optional)"
   :sign-in                        "Sign In"
   :email                          "Email"
   :password                       "Password"
   :forgotten-question             "forgotten your password?"
   :forgotten-password             "Forgotten Password"
   :forgotten-instruction          "Please enter your email address. If it matches one in our system we'll send you reset instructions."
   :reset-submitted                "Thanks. Your password reset request has been received."
   :reset-password                 "Reset Password"
   :back                           "Back"
   :thanks                         "Thanks"
   :signing-in                     "Signing in..."
   :sign-in-failure                "There was a problem with your details. Please try again."
   :api-failure                    "There was a problem with the service. Please try again. If the problem persists, please contact us." ;; TODO add link?
   :create                         "Create"
   :created                        "Created"
   :logout                         "Log Out"
   :no-model-properties            "This model has no properties to configure"
   :please-wait                    "Please Wait..."
   :create-new-forecast            "Create a New Version"
   :revert-forecast                "Revert changes"
   :in-progress                    "In-Progress"
   :changed                        "Changed"
   :refresh-now                    "Refresh"
   :new                            "New"
   :upload                         "Upload"
   :upload-new-data                "Upload new data"
   :forecast-changes-text          "There are changes to this forecast. Would you like to save them and create a new version?"
   :forecast-in-progress-text      "This version is currently being generated. During this time you will be unable to make changes or download results. This can take several minutes."
   :input                          "Input"
   :input-intro                    "The fields below show the inputs that currently configured for this forecast. If there are required inputs, these need to be specified before the model can be run. You can adjust existing inputs in the same way and this will cause the model to be re-run."
   :model-intro                    "These are the model details that have been configured for this forecast."
   :output                         "Output"
   :output-intro                   "Download the latest results of this forecast."
   :no-input-specified             "No data input specified."
   :please-select-data-input       "Please select a data input for this category."
   :default-brackets               "(default)"
   :browser-choose-data            "Choose from existing data or upload your own new data"
   :browser-upload-completes       "Please wait whilst your upload completes..."
   :browser-no-file-selected       "No file selected..."
   :browser-upload-option-existing "This is an updated version of an existing data item"
   :browser-upload-option-new      "This is a brand new data item"
   :browser-upload-select-existing "Please select the existing data item that you want to update"
   :browser-upload-select-new      "Please enter a name for the new data item"
   :browser-upload-error           "An error occurred whilst trying to upload the file. Please try again and if this problem persists, contact us." ;; TODO wants email
   :downloads                      "Downloads"
   :new-version-no-downloads       "Downloads are not yet available. After configuring and running the forecast for the first time your downloads will appear in this section."
   :in-progress-no-downloads       "Downloads are not yet available because the model is still running. This can take several minutes so please check again shortly."
   :data-items                     "data items"
   :upload-success                 "Upload successful"
   :creating-forecast              "Please wait whilst we update this forecast..."
   :today                          "Today"
   :yesterday                      "Yesterday"
   })

(defn get-string
  ""
  [keywd & add]
  (if (contains? strings keywd)
    (if add
      (str (keywd strings) (first add) (clojure.string/join " " (concat " " (rest add))))
      (keywd strings))
    (do
      (log/severe "Failed to find string " (str keywd))
      "## ERROR ##")))
