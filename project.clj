(defproject cljstone "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [com.lucasbradstreet/cljs-uuid-utils "1.0.2"]
                 [enfocus "2.1.1"]
                 [prismatic/schema "0.4.4"]]
  :plugins [[lein-cljsbuild "1.0.6"]]
  :cljsbuild
    {:builds
      [{:compiler
        {:output-to "resources/public/core.js"
         :optimizations :whitespace
         :pretty-print true}
       :source-paths ["src/cljs"]}]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
