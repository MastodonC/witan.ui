(ns witan.ui.utils
  (:require [witan.ui.data :as data]
            [cljs.reader :as reader]
            [cljsjs.mustache])
  (:require-macros [cljs-log.core :as log]))

(defn query-param
  [k]
  (data/get-in-app-state :app/route :route/query k))

(defn query-param-int
  ([k]
   (reader/parse-int (query-param k)))
  ([k mn mx]
   (-> (query-param k)
       (reader/parse-int)
       (min mx)
       (max mn))))

(defn render-mustache
  [s m]
  (.render js/Mustache s (clj->js m)))

(defn sanitize-filename
  "Removed slashes from a filename"
  [filename]
  (.replace filename #".*[\\\/]" ""))

(defn keys*
  [m]
  (let [r (map #(condp = (type %)
                  cljs.core/Keyword %
                  schema.core.OptionalKey (:k %)
                  (throw (js/Error. (str "Unknown key type in: " (keys m) " - " (type %)))))
               (keys m))]
    r))

(defn add-file-flag!
  [id flag]
  (data/swap-app-state! :app/datastore update-in [:ds/file-properties id :flags] #(conj (set %) flag)))

(defn remove-file-flag!
  [id flag]
  (data/swap-app-state! :app/datastore update-in [:ds/file-properties id :flags] #(disj (set %) flag)))

(defn check-file-flag?
  [id flag]
  (contains? (data/get-in-app-state :app/datastore [:ds/file-properties id :flags]) flag))

(defn remove-nil-or-empty-vals
  [m]
  (if (map? m)
    (reduce
     (fn [a [k v]]
       (if (or (nil? v)
               (and (coll? v)
                    (empty? v)))
         (dissoc a k)
         a)) m m)
    m))

(defn user-has-permission?
  [permission user file-metadata]
  (let [ug (set (conj (:kixi.user/groups user) (:kixi.user/self-group user)))
        vg (set (map :kixi.group/id (get-in file-metadata [:kixi.datastore.metadatastore/sharing permission])))]
    (some vg ug)))

(def user-has-edit?
  (partial user-has-permission? :kixi.datastore.metadatastore/meta-update))

(def user-has-download?
  (partial user-has-permission? :kixi.datastore.metadatastore/file-read))
