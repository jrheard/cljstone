(defproject cljstone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.clojure/core.match "0.3.0-alpha4"]
                 [cheshire "5.5.0"]
                 [prismatic/plumbing "0.5.0"]
                 [prismatic/schema "1.0.1"]
                 [reagent "0.5.1-rc"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-doo "0.1.5-SNAPSHOT"]
            [lein-figwheel "0.4.0"]]
  :source-paths ["src/clj"]
  :cljsbuild
    {:builds
      {:dev {:compiler
               {:output-dir "resources/public/js/out"
                ;:optimizations :whitespace
                ;:source-map "resources/public/js/core.js.map"
                ;:pretty-print true
                :output-to "resources/public/js/core.js"}
             :source-paths ["src/cljs"]
             :figwheel {:on-jsload "cljstone.app/main"}}
       :test {:compiler
              {:main "cljstone.runner"
               :output-to "test_resources/test.js"
               :optimizations :whitespace
               :pretty-print true}
              :source-paths ["src/cljs" "test/cljs"]}}}
  :figwheel {:css-dirs ["resources/public/css"]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
