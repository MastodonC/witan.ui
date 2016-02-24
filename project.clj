(defproject witan.ui "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [org.clojure/core.async "0.2.374" :exclusions [org.clojure/tools.reader]]
                 [devcards "0.2.1-5"]
                 [sablono "0.6.2"]
                 [org.omcljs/om "1.0.0-alpha31-SNAPSHOT"]
                 [cljsjs/react "0.14.3-0"]
                 [cljsjs/react-dom "0.14.3-1"]
                 [cljsjs/react-dom-server "0.14.3-0"]
                 [datascript "0.15.0"]
                 [cljs-log "0.2.2"]
                 [com.andrewmcveigh/cljs-time "0.3.14"]
                 [bidi "2.0.0"]
                 [venantius/accountant "0.1.7"]]

  :plugins [[lein-figwheel "0.5.0-6" :exclusions [org.clojure/tools.reader]]
            [lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]
            [lein-garden "0.2.6"]]

  :profiles {:uberjar {:auto-clean false}
             :dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.12"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}}

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "target"]

  :cljsbuild {:builds
              [{:id "devcards"
                :source-paths ["src/cljs"]
                :figwheel { :devcards true } ;; <- note this
                :compiler {:main       witan.ui.core
                           :asset-path "js/compiled/devcards_out"
                           :output-to  "resources/public/js/compiled/om_devcards_devcards.js"
                           :output-dir "resources/public/js/compiled/devcards_out"
                           :source-map-timestamp true }}
               {:id "dev"
                :source-paths ["src/cljs"]

                ;; If no code is to be run, set :figwheel true for continued automagical reloading
                :figwheel true ;{:on-jsload "witan.ui.core/on-js-reload"}

                :compiler {:main witan.ui.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/witan.ui.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :warnings {:single-segment-namespace false}}}
               {:id "prod"
                :source-paths ["src/cljs"]
                :compiler {:output-to "resources/public/js/compiled/witan.ui.js"
                           :main witan.ui.core
                           :externs ["../js/externs.js"]
                           :optimizations :advanced
                           :pretty-print false
                           :warnings {:single-segment-namespace false}}}]}

  :garden {:builds [{;; Optional name of the build:
                     :id "style"
                     ;; Source paths where the stylesheet source code is
                     :source-paths ["src/styles"]
                     ;; The var containing your stylesheet:
                     :stylesheet witan.ui.style/main
                     ;; Compiler flags passed to `garden.core/css`:
                     :compiler {;; Where to save the file:
                                :output-to "resources/public/css/style.css"
                                :vendors [:moz :webkit :o :ms]
                                ;; Compress the output?
                                :pretty-print? false}}]}

  :figwheel {:css-dirs ["resources/public/css"] ;; watch and update CSS
             :nrepl-port 7888}
  :jvm-opts ["-Xmx2g"]
  :release-tasks [["change" "version"
                   "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "release-v"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])

(comment
  (do (use 'figwheel-sidecar.repl-api)
      (cljs-repl)))
