(ns witan.ui.strings
  (:require [clojure.string])
  (:require-macros
   [cljs-log.core :as log]))

(def strings
  {:string/name                             "Name"
   :string/full-name                        "Full Name"
   :string/file-name                        "Dataset Name"
   :string/file-type                        "Dataset Type"
   :string/file-provenance-source           "Source"
   :string/sign-in-failure                  "There was a problem with your details. Please try again."
   :string/upload-new-data                  "Upload new dataset"
   :string/upload-new-data-desc             "Upload to securely store, share or visualise your data"
   :string/forgotten-instruction            "Please enter your email address. If it matches one in our system we'll send you reset instructions."
   :string/confirm-email                    "Confirm email"
   :string/witan                            "Witan"
   :string/reset-submitted                  "Thanks. Your password reset request has been received."
   :string/back                             "Back"
   :string/please-wait                      "Please Wait..."
   :string/error                            "Error"
   :string/edit                             "Edit"
   :string/choose-file                      "Choose Dataset"
   :string/optional                         "(optional)"
   :string/forecast-name                    "Name"
   :string/forgotten-password               "Forgotten Password"
   :string/forecast-lastmodified            "Last Modified"
   :string/create-account-header            "Need an account?"
   :string/view                             "View"
   :string/if-persists                      "If the problem persists, please contact us at witan@mastodonc.com"
   :string/api-failure                      ["Sorry, we're having a problem with the service. Please try again." :string/if-persists]
   :string/thanks                           "Thanks"
   :string/upload                           "Upload"
   :string/create-account                   "Create Account"
   :string/email                            "Email"
   :string/sign-up-token                    "Invite code"
   :string/forecast-owner                   "Owner"
   :string/progress                         "Progress"
   :string/forgotten-question               "forgotten your password?"
   :string/witan-tagline                    "Make more sense of your city"
   :string/sign-in                          "Sign In"
   :string/create                           "Create"
   :string/password                         "Password"
   :string/created-at                       "Created at"
   :string/confirm-password                 "Confirm password"
   :string/browser-upload-error             "An error occurred whilst trying to upload the dataset. Please try again and if this problem persists, contact us."
   :string/reset-password                   "Reset Password"
   :string/create-account-info              "If you have an invite code you can create your account below:"
   :string/tooltip-workspace                "Browse your workspaces"
   :string/tooltip-data                     "Browse your datasets"
   :string/tooltip-logout                   "Log out from your account"
   :string/tooltip-help                     "Get help"
   :string/tooltip-request-to-share         "Request that datasets be shared with you or a group you belong to"
   :string/data                             "Datasets"
   :string/workspace-noun-plural            "Workspaces"
   :string/workspace-dash-title             :string/workspace-noun-plural
   :string/go-to                            "Go to"
   :string/go-to-data                       [:string/go-to :string/data]
   :string/data-dash-title                  "Datasets"
   :string/workspace-dash-filter            ["Filter your" :string/workspace-noun-plural]
   :string/data-dash-filter                 "Filter your datasets"
   :string/workspace-data-view              "Data"
   :string/workspace-config-view            "Configuration"
   :string/workspace-history-view           "History"
   :string/create-workspace-title           "Create a new workspace"
   :string/create-workspace-subtitle        "A workspace contains models and visualisations, configured how you want them"
   :string/create-workspace-name            "Workspace name"
   :string/create-workspace-name-ph         "Enter a name for this workspace"
   :string/create-workspace-desc            "Description"
   :string/create-workspace-desc-ph         "What will this workspace be used for?"
   :string/workspace-404-error              "Unable to find a Workspace at this address."
   :string/workspace-empty                  "This workspace is empty!"
   :string/workspace-select-a-model         "Please start by selecting a model:"
   :string/data-empty-catalog               "No datasets required."
   :string/config-empty-catalog             "No configuration required."
   :string/run                              "Run"
   :string/running                          "Running"
   :string/no-viz-selected                  "Please select the datasets you'd like to visualise!"
   :string/no-viz-selected-desc             "If you don't have any output datasets yet, try running the workspace first."
   :string/workspace-result-history         "Result History"
   :string/no-results                       "You haven't generated any results yet."
   :string/compare                          "Compare"
   :string/request-to-share-noun            "Data Request"
   :string/request-to-share-dash-title      "Data Requests"
   :string/request-to-share-dash-desc       "Manage data requests to and from other groups and users"
   :string/create-request-to-share          ["Create a new" :string/request-to-share-noun]
   :string/create-request-to-share-desc     "Request a dataset, of a given format, from one or more users or organisations. Ensure the data you receive is valid and track the progress of your request by being able to see who has submit their data and who hasn't."
   :string/get-started                      "Get Started"
   :string/rts-no-requests                  ["It looks like you haven't created any" :string/request-to-share-dash-title
                                             "yet. Use the button above to get started!"]
   :string/create-rts-subtitle              "Send a request, to a user or organisation, for data of a specific type"
   :string/create-rts-user                  "Recipient Users/Groups"
   :string/create-rts-user-ph               "Search for the users and/or organisations you'd like to send the request to."
   :string/search-results                   "Search Results"
   :string/create-rts-will-be-sent-to       "This request will be sent to:"
   :string/schema                           "Schema"
   :string/create-rts-schema-ph             "Search for the schema you'd like to associate with the uploaded data."
   :string/create-rts-selected-schema       "The selected schema for the requested data:"
   :string/create-rts-destination           "Destination Users/Groups"
   :string/create-rts-destination-ph        "Search for the users and/or organisations you'd like the requested data to be shared with."
   :string/create-rts-will-be-shared-with   "The requested data will be shared with:"
   :string/create-rts-recipients-invalid    "Please specify at least one recipient group."
   :string/create-rts-schema-invalid        "Please specify a schema."
   :string/create-rts-destinations-invalid  "Please specify at least one destination group."
   :string/message                          "Message"
   :string/create-rts-message-ph            "Add an optional message which the recipients will see when they receive the request."
   :string/create-rts-message-failed        "There was an error creating this Request. Please contact us."
   :string/create-rts-message-created       "Your Request was created successfully! The next step is to email your request to the relevant groups. Click the individual 'Mail' buttons below to do this now:"
   :string/send-mail                        "Send Mail"
   :string/return-to-dash                   "Return to Dashboard"
   :string/rts-404-error                    "Unable to find a Request at this address."
   :string/status                           "Status"
   :string/rts-status-incomplete            "Pending"
   :string/rts-status-complete              "Complete"
   :string/new-data-request-created         ["New" :string/request-to-share-noun "created!"]
   :string/new-data-request-created-desc    ["Congratulations! You've successfully created a new" :string/request-to-share-noun "- please remember to email each of the recipients. This will also give you an opportunity to tailor the message and add any attachments that might help them fulfil the request for data."]
   :string/date                             "Date"
   :string/rts-info-paragraph-1             "Your request for"
   :string/rts-info-paragraph-2             "data, dated"
   :string/rts-info-paragraph-3             "has been responded to by"
   :string/rts-info-paragraph-4             "out of"
   :string/rts-info-paragraph-5             "group(s)."
   :string/rts-info-paragraph-6             "You wrote them the following message:"
   :string/groups                           "Groups"
   :string/outbound-requests                ["Outbound" :string/request-to-share-dash-title]
   :string/rts-email-subject                ["New" :string/request-to-share-noun "from "]
   :string/rts-email-header-line            "To whom it may concern,\n\n"
   :string/rts-email-footer-line            "Here is the link you should use to submit your data:\n\n"
   :string/rts-email-default-body           (str
                                             "You are receiving this email because you belong to the '%s' City Datastore group.\n"
                                             "This is a formal request for submission of '%s' data.\n"
                                             "Please submit the data directly into the City Datastore using the link provided below.\n\n"
                                             "If you feel you've received this requesst in error, please reply directly or liase with other members of the group.")
   :string/author                           "Author"
   :string/datastore-name                   "City Data Store"
   :string/data-upload-intro                ["This step-by-step process will guide you through uploading a dataset into the" :string/datastore-name "- if you're unsure about any of the steps, please don't hesistate to use our interactive support system and we can offer additional guidance."]
   :string/data-upload-selected-file        "Selected dataset"
   :string/data-upload-step-1               "Select the dataset you'd like to upload..."
   :string/data-upload-step-2               "Please fill in some information about this data."
   :string/data-upload-step-2-input-1-title "Name of the data"
   :string/data-upload-step-2-input-1-ph    "e.g. 'Housing Data for London'"
   :string/data-upload-step-2-input-2-title "Description of the data"
   :string/data-upload-step-2-input-2-ph    "e.g. 'This dataset shows housing data across all 33 London boroughs.'"
   :string/data-upload-step-2-input-3-title "Author"
   :string/data-upload-step-2-input-3-ph    ""
   :string/data-upload-step-2-input-4-title "Maintainer"
   :string/data-upload-step-2-input-4-ph    ""
   :string/data-upload-step-2-input-5-title "Source"
   :string/data-upload-step-2-input-5-ph    ""
   :string/data-upload-step-2-input-6-title "Smallest Geography"
   :string/data-upload-step-2-input-6-ph    ""
   :string/data-upload-step-2-input-7-title "Temporal Coverage From"
   :string/data-upload-step-2-input-7-ph    ""
   :string/data-upload-step-2-input-8-title "Temporal Coverage To"
   :string/data-upload-step-2-input-8-ph    ""
   :string/data-upload-step-2-input-9-title "Tags"
   :string/data-upload-step-2-input-9-ph    "e.g. demography, send, schools, ons, economy"
   :string/data-upload-step-2-input-10-title "License Type"
   :string/data-upload-step-2-input-10-ph    ""
   :string/data-upload-step-2-input-11-title "License Usage"
   :string/data-upload-step-2-input-11-ph    ""
   :string/data-upload-step-3               "Would you like to share this dataset with others users or groups?"
   :string/data-upload-step-3-radio-1       "Yes, I'd like to share this dataset."
   :string/data-upload-step-3-radio-2       "No, this dataset should be private and only accessible by me."
   :string/data-upload-search-groups        "Search for users and/or groups with whom you'd like to share this data."
   :string/data-upload-step-4               "Confirm"
   :string/data-upload-step-4-decl-unsure   "Please take a moment to check that the steps are filled out correctly and then press the 'Upload' button to begin uploading the data."
   :string/file-description                 "Description"
   :string/try-again                        "Try Again"
   :string/file-size                        "Size"
   :string/file-uploader                    "Uploader"
   :string/sharing                          "Sharing"
   :string/file-sharing-meta-read           "Read Metadata"
   :string/file-sharing-meta-update         "Update Metadata"
   :string/file-sharing-file-read           "Download Dataset"
   :string/file-actions-download-file       "Download this dataset"
   :string/sharing-matrix-group-name        "Group Name"
   :string/sharing-matrix-group-search-ph   "Search for users and/or groups..."
   :string/file-inaccessible                "The dataset could not be accessed. Either it does not exist or you do not have permission to view it."
   :string/title-data-dashboard             "Your Datasets"
   :string/title-data-create                "Upload Dataset"
   :string/title-data-loading               "Loading..."
   :string/this-is-you                      "This is you!"
   :string/upload-finalizing                "Confirming the upload succeeded"
   :string/uploading                        "Uploading"
   :string/preparing-upload                 "Preparing to upload"
   :string/sign-up-error-usernames-match    "Email addresses do not match"
   :string/sign-up-error-passwords-match    "Passwords do not match"
   :string/sign-up-failure                  ["There was an issue signing up. Please check your invite code and email address, and try again." :string/if-persists]
   :string/reset-your-password              "Reset Your Password"
   :string/reset-your-password-instructions "Please provide the necessary details, including a new password"
   :string/reset-code                       "Reset Code"
   :string/reset-password-completion        "Your password was changed. Press 'Back' to log in to your account."
   :string/file-upload-unknown-error        "An unknown error occurred whilst uploading your file. Please contact support."
   :string/file-upload-metadata-invalid     "The name and/or description field provided was rejected by the system. Please ensure that only standard characters are used (A-Z, a-z, 0-9, spaces, tabs, punctuation). If the problem persists, please contact support."
   :string/license                          "License"
   :string/license-type                     "License Type"
   :string/license-usage                    "License Usage"
   :string/source                           "Source"
   :string/maintainer                       "Maintainer"
   :string/smallest-geography               "Smallest Geography"
   :string/temporal-coverage                "Temporal Coverage"
   :string/tags                             "Tags"
   :string/sharing-summary                  "This file is currently visible to %d other users or organisations."
   :string/sharing-summary-single           "This file is currently visible to just 1 other user or organisation."} )


(defn resolve-string
  ([r]
   (let [s (get strings r)]
     (if (string? s) s
         (resolve-string s nil))))
  ([r a]
   (cond (keyword? r) (if a (clojure.string/join " " [a (get strings r)]) (get strings r))
         (string? r)  (if a (clojure.string/join " " [a r]) r)
         (vector? r)  (reduce #(resolve-string %2 %1) a r)
         :else nil)))

(defn get-string
  ""
  [keywd & add]
  (if (contains? strings keywd)
    (let [r (resolve-string keywd)]
      (if add
        (str r (first add) (clojure.string/join " " (concat " " (rest add))))
        r))
    (do
      (log/severe "Failed to find string " (str keywd))
      "## ERROR ##")))
