(defproject garamond "0.1.0-SNAPSHOT"
  :description      "A documentation generator framework with a focus on strong github support"
  :min-lein-version "2.0.0"
  :plugins          [[lein-cljsbuild "0.3.2"]]
  :profiles         
  {:dev
   {:dependencies [[contentjon/kit.cljs "0.2.0-SNAPSHOT"]
                   [org.clojure/clojure "1.5.1"]
                   [mocha-latte "0.1.0-SNAPSHOT"]]}}
  :cljsbuild
  {:builds
   [{:source-paths ["src/cli"],
     :id "cli",
     :compiler
     {:pretty-print  true,
      :output-to     "bin/garamond",
      :optimizations :simple
      :target        :nodejs}}]})
