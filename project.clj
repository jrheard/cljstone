(defproject cljstone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [prismatic/schema "1.0.1"]
                 [reagent "0.5.1-rc"]
                 [devcards "0.2.0-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-doo "0.1.5-SNAPSHOT"]
            [lein-figwheel "0.4.0"]]
  :cljsbuild
    {:builds
      {:dev {:compiler
               {:output-dir "resources/public/js/compiled/out"
                :asset-path "js/compiled/out"
                ;:optimizations :whitespace
                ;:source-map "resources/public/js/core.js.map"
                ;:pretty-print true
                :output-to "resources/public/js/compiled/core.js"}
             :source-paths ["src/cljs"]
             :figwheel {:on-jsload "cljstone.app/main"}}
       :devcards {:compiler
                  {:output-dir "resources/public/js/compiled/devcards_out"
                   :output-to "resources/public/js/compiled/core_devcards.js"}
                  :source-paths ["src/cljs"]
                  :figwheel {:devcards true}}
       :test {:compiler
              {:main "cljstone.runner"
               :output-to "test_resources/test.js"
               :optimizations :whitespace
               :pretty-print true}
              :source-paths ["src/cljs" "test/cljs"]}}}
  :figwheel { :css-dirs ["resources/public/css"] }
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
