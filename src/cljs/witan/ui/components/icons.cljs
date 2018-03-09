(ns witan.ui.components.icons
  (:require [sablono.core :as sab :include-macros true])
  (:require-macros
   [devcards.core :as dc :refer [defcard]]
   [cljs-log.core :as log]))

(defn decode-keys [& args]
  (let [lookup {:error "md-error"
                :success "md-success"
                :warning "md-warning"
                :dark "md-dark"
                :light "md-light"
                :disabled "md-inactive"
                :tiny "md-t"
                :small "md-s"
                :medium "md-m"
                :large "md-l"
                :x-large "md-xl"
                :spin "anim-spin"
                :silver "md-silver"
                :info   "md-info"}]
    (reduce (fn [a r]
              (apply str a
                     (if-let [value (get lookup r)]
                       (str "." value)))) "" args)))

(defn create-key
  [icon-name & args]
  (let [key-str (->>
                 (apply decode-keys args)
                 (str "i.material-icons")
                 (keyword))]
    [key-str icon-name]))


(defn add-react-args
  [icon args]
  (let [[head & body] icon]
    (vec (concat [head] [args] body))))

;;

(defn cog
  [& args]
  (apply create-key "settings" args))

(defn upload
  [& args]
  (apply create-key "file_upload" args))

(defn padlock
  [& args]
  (apply create-key "lock" args))

(defn star
  [& args]
  (apply create-key "star" args))

(defn workspace
  [& args]
  (apply create-key "assessment" args))

(defn data
  [& args]
  (apply create-key "subject" args))

(defn help
  [& args]
  (apply create-key "live_help" args))

(defn info
  [& args]
  (apply create-key "info" args))

(defn filter-list
  [& args]
  (apply create-key "filter_list" args))

(defn logout
  [& args]
  (apply create-key "power_settings_new" args))

(defn search
  [& args]
  (apply create-key "search" args))

(defn person
  [& args]
  (apply create-key "person" args))

(defn visualisation
  [& args]
  (apply create-key "timeline" args))

(defn topology
  [& args]
  (apply create-key "device_hub" args))

(defn plus
  [& args]
  (apply create-key "add" args))

(defn open
  [& args]
  (apply create-key "open_in_browser" args))

(defn view
  [& args]
  (apply create-key "visibility" args))

(defn error
  [& args]
  (apply create-key "error" args))

(defn clipboard
  [& args]
  (apply create-key "content_paste" args))

(defn close
  [& args]
  (apply create-key "close" args))

(defn grain
  [& args]
  (apply create-key "grain" args))

(defn cake
  [& args]
  (apply create-key "cake" args))

(defn download
  [& args]
  (apply create-key "file_download" args))

(defn pie-chart
  [& args]
  (apply create-key "pie_chart" args))

(defn link
  [& args]
  (apply create-key "link" args))

(defn tree-arrow-down
  [& args]
  (apply create-key "arrow_drop_down" args))

(defn unchecked
  [& args]
  (apply create-key "check_box_outline_blank" args))

(defn checked
  [& args]
  (apply create-key "check_box" args))

(defn play
  [& args]
  (apply create-key "play_arrow" args))

(defn bug
  [& args]
  (apply create-key "bug_report" args))

(defn request-to-share
  [& args]
  (apply create-key "assignment_return" args))

(defn user
  [& args]
  (apply create-key "person" args))

(defn organisation
  [& args]
  (apply create-key "location_city" args))

(defn delete
  [& args]
  (apply create-key "delete" args))

(defn tick
  [& args]
  (apply create-key "done" args))

(defn schema
  [& args]
  (apply create-key "assignment" args))

(defn email
  [& args]
  (apply create-key "email" args))

(defn tick-circle
  [& args]
  (apply create-key "check_circle" args))

(defn warning
  [& args]
  (apply create-key "warning" args))

(defn file
  [& args]
  (apply create-key "insert_drive_file" args))

(defn retry
  [& args]
  (apply create-key "undo" args))

(defn activity
  [& args]
  (apply create-key "history" args))

(defn datapack
  [& args]
  (apply create-key "library_books" args))

;;

(defn ext->file-type
  [ext]
  (case ext
    "ai"   "ai"
    "avi"  "avi"
    "css"  "css"
    "csv"  "csv"
    "dbf"  "dbf"
    "doc"  "doc"
    "docx" "doc"
    "odt"  "doc"
    "dwg"  "dwg"
    "exe"  "exe"
    "file" "file"
    "fla"  "fla"
    "html" "html"
    "iso"  "iso"
    "js"   "js"
    "jpg"  "jpg"
    "json" "json"
    "mp3"  "mp3"
    "mp4"  "mp4"
    "pdf"  "pdf"
    "png"  "png"
    "ppt"  "ppt"
    "psd"  "psd"
    "rtf"  "rtf"
    "svg"  "svg"
    "txt"  "txt"
    "xls"  "xls"
    "xlsx" "xls"
    "xml"  "xml"
    "zip"  "zip"
    ;;
    "file"))

(defn file-type
  [extension & [size]]
  (let [filename (ext->file-type extension)
        attr (str filename " icon (designed by Madebyoliver from Flaticon)")]
    [:img {:src (str "/img/file-types/" filename ".svg")
           :alt attr
           :title attr
           :class (str "icon--file-type " (when size (name size)))}]))

(defn bundle-type
  [type & [size]]
  (case type
    "datapack" (let [attr (str "zip-1 icon (designed by Madebyoliver from Flaticon)")]
                 [:img {:src (str "/img/file-types/pack.svg")
                        :alt attr
                        :title attr
                        :class (str "icon--file-type " (when size (name size)))}])
    ;;
    (help)))

;;

(defn loading
  [& args]
  (apply cog :spin :dark args))

;;

(defcard file-type-size-card
  (sab/html [:div
             (file-type "csv" :tiny)
             (file-type "csv" :small)
             (file-type "csv" :medium)
             (file-type "csv" :large)
             (file-type "csv" :x-large)]))

(defcard file-type-types-card
  (let [types ["ai"
               "avi"
               "css"
               "csv"
               "dbf"
               "doc"
               "dwg"
               "exe"
               "fla"
               "html"
               "iso"
               "js"
               "jpg"
               "json"
               "mp3"
               "mp4"
               "pdf"
               "png"
               "ppt"
               "psd"
               "rtf"
               "svg"
               "txt"
               "xls"
               "xml"
               "zip"]]
    (sab/html [:div
               (doall (for [t types]
                        (file-type t :small)))])))

(defcard loading-card
  (sab/html [:div
             (loading :small)
             (loading :large )
             (loading :x-large)]))

(defcard icons-card
  (sab/html
   (let [variations '([]
                      [:dark]
                      [:dark :disabled]
                      [:light]
                      [:light :disabled]
                      [:small]
                      [:medium]
                      [:large]
                      [:x-large]
                      [:error]
                      [:success]
                      [:warning]
                      [:info]
                      [:silver])
         all-icons [["cog"           cog]
                    ["upload"        upload]
                    ["padlock"       padlock]
                    ["star"          star]
                    ["workspace"     workspace]
                    ["data"          data]
                    ["help"          help]
                    ["logout"        logout]
                    ["search"        search]
                    ["visualisation" visualisation]
                    ["person"        person]
                    ["plus"          plus]
                    ["open"          open]
                    ["view"          view]
                    ["error"         error]
                    ["clipboard"     clipboard]
                    ["close"         close]
                    ["grain"         grain ]
                    ["cake"          cake]
                    ["download"      download]
                    ["pie-chart"     pie-chart]
                    ["link"          link]
                    ["tree-arrow-down" tree-arrow-down]
                    ["unchecked"     unchecked]
                    ["checked"       checked]
                    ["play"          play]
                    ["bug"           bug]
                    ["request-to-share" request-to-share]
                    ["user"          user]
                    ["organisation"  organisation]
                    ["delete"        delete]
                    ["tick"          tick]
                    ["tick-circle"   tick-circle]
                    ["schema"        schema]
                    ["email"         email]
                    ["warning"       warning]
                    ["file"          file]
                    ["retry"         retry]
                    ["activity"      activity]
                    ["datapack"      datapack]]]
     [:div {:style {:background-color "#fff"}}
      (for [[title icon-fn] all-icons]
        [:div
         {:key title}
         [:h2 title]
         (for [v variations]
           (let [icon (apply icon-fn v)]
             [:div
              {:key (str title icon)}
              icon
              [:span {:style {:float "right"}} (dc/edn icon)]]))])])))
