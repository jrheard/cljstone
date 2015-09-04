(defproject cljstone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [prismatic/schema "0.4.4"]
                 [reagent "0.5.1-rc"]]
  :plugins [[lein-cljsbuild "1.0.6"]
            [lein-doo "0.1.5-SNAPSHOT"]]
  :cljsbuild
    {:builds
      {:dev {:compiler
               {:output-dir "resources/public"
                ; XXXXXX i don't think i actually understand what :output-dir does. look into it.
                :output-to "resources/public/core.js"
                :optimizations :whitespace
                :pretty-print true
                :source-map "resources/public/core.js.map"}
             :source-paths ["src/cljs"]}
       :test {:compiler
              {:main 'cljstone.runner
               :output-to "test_resources/test.js"
               :optimizations :whitespace
               :pretty-print true}
              :source-paths ["src/cljs" "test/cljs"]}}
     :test-commands {"test" ["doo" "phantom" "test" "once"]}}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
