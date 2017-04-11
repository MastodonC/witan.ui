(ns witan.ui.upload-data
  (:require [clojure.string :as str]
            [amazonica.aws.s3 :as aws]
            [amazonica.aws.s3transfer :as aws-s3]
            [clojure.java.io :as io]
            [environ.core :refer [env]])
  (:import java.util.zip.GZIPOutputStream)
  (:gen-class))

(def dir "target/build")
(def blacklist #{"cards.html"})

(defn -main
  "Uploads the data to S3"
  [bucket region]
  (println "Uploading to...\nbucket:" bucket "\nregion:" region "\ndir:" dir)
  (time
   (let [aak (env :aws-access-key)
         ask (env :aws-secret-key)
         files (file-seq (clojure.java.io/file dir))
         upload-fn (fn [f]
                     (when-not (.isDirectory f)
                       (let [fname (subs (str/replace (str f) dir "") 1)
                             fobj  (io/file (str f))]
                         (when-not (contains? blacklist fname)
                           (println "-" fname)
                           (aws/put-object (merge {:endpoint region}
                                                  (when (and aak ask)
                                                    {:access-key aak
                                                     :secret-key ask}))
                                           :bucket-name bucket
                                           :key fname
                                           :file fobj)))))]
     (run! upload-fn files))))
