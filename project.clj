(defproject witan.ui "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "0.0-3297"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [sablono "0.3.4"]
                 [garden "1.2.5"]
                 [org.omcljs/om "0.8.8"]
                 [prismatic/om-tools "0.3.11"]
                 [inflections "0.9.14"]
                 [prismatic/schema "0.4.3"]
                 [secretary "1.2.3"]
                 [datascript "0.11.6"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [lein-figwheel "0.3.5"]
            [lein-garden "0.2.6"]
            [lein-cljfmt "0.3.0"]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {
              :builds [{:id "ui"
                        :source-paths ["src"]
                        :figwheel {:on-jsload "witan.ui.core/on-js-reload" }
                        :compiler {:main witan.ui.core
                                   :asset-path "js/compiled/out-ui"
                                   :output-to "resources/public/js/compiled/witan-ui.js"
                                   :output-dir "resources/public/js/compiled/out-ui"
                                   :source-map-timestamp true
                                   :warnings {:single-segment-namespace false}}}
                       {:id "login"
                        :source-paths ["src"]
                        :figwheel true
                        :compiler {:main witan.login.core
                                   :asset-path "js/compiled/out-login"
                                   :output-to "resources/public/js/compiled/witan-login.js"
                                   :output-dir "resources/public/js/compiled/out-login"
                                   :source-map-timestamp true }}
                       {:id "prod-ui"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/witan-ui.js"
                                   :main witan.ui.core
                                   :optimizations :advanced
                                   :pretty-print false}}
                       {:id "prod-login"
                        :source-paths ["src"]
                        :compiler {:output-to "resources/public/js/compiled/witan-login.js"
                                   :main witan.login.core
                                   :optimizations :advanced
                                   :pretty-print false}}]}

  :figwheel {
             ;; :http-server-root "public" ;; default and assumes "resources"
             ;; :server-port 3449 ;; default
             ;; :server-ip "127.0.0.1"

             :css-dirs ["resources/public/css"] ;; watch and update CSS

             ;; Start an nREPL server into the running figwheel process
             :nrepl-port 7888

             ;; Server Ring Handler (optional)
             ;; if you want to embed a ring handler into the figwheel http-kit
             ;; server, this is for simple ring servers, if this
             ;; doesn't work for you just run your own server :)
             ;; :ring-handler hello_world.server/handler
             }

  :garden {:builds [{;; Optional name of the build:
                     :id "ui"
                     ;; Source paths where the stylesheet source code is
                     :source-paths ["src/styles"]
                     ;; The var containing your stylesheet:
                     :stylesheet witan.styles.base/base
                     ;; Compiler flags passed to `garden.core/css`:
                     :compiler {;; Where to save the file:
                                :vendors [:moz :webkit :o]
                                :output-to "resources/public/css/app.css"
                                ;; Compress the output?
                                :pretty-print? false}}
                    {;; Optional name of the build:
                     :id "login"
                     ;; Source paths where the stylesheet source code is
                     :source-paths ["src/styles"]
                     ;; The var containing your stylesheet:
                     :stylesheet witan.styles.login/login
                     ;; Compiler flags passed to `garden.core/css`:
                     :compiler {;; Where to save the file:
                                :vendors [:moz :webkit :o]
                                :output-to "resources/public/css/login.css"
                                ;; Compress the output?
                                :pretty-print? false}}]})

(comment
  (do (use 'figwheel-sidecar.repl-api)
      (cljs-repl)))
