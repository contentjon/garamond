(defproject garamond "0.1.0-SNAPSHOT"
  :description      "A collection of tools to document your github projects"
  :url              "https://github.com/contentjon/garamond"
  :license          {:name         "GPL"
                     :url          "http://www.gnu.org/licenses/gpl.html"
                     :distribution :repo}
  :min-lein-version "2.0.0"
  :plugins          [[lein-cljsbuild "0.3.1"]]
  :dependencies     [[contentjon/kit.cljs "0.1.0"]]
  :profiles         {:dev {:dependencies [[org.clojure/clojure "1.5.1"]
                                          [mocha-latte "0.1.0"]]}}
  :cljsbuild
  {:builds
   [{:source-paths ["src/domains/cljs"],
     :id "parser",
     :compiler
     {:pretty-print  true,
      :output-to     "domains/cljs",
      :optimizations :simple
      :target        :nodejs}}
    {:source-paths ["src/renderer/"],
     :id "renderer",
     :compiler
     {:pretty-print  true,
      :output-to     "generators/renderer",
      :optimizations :simple
      :target        :nodejs}}
    {:source-paths ["src/filters/jdoc"],
     :id "jdoc",
     :compiler
     {:pretty-print  true,
      :output-to     "filters/jdoc",
      :optimizations :simple
      :target        :nodejs}}
    {:source-paths ["src/garamond/"],
     :id "garamond",
     :compiler
     {:pretty-print  true,
      :output-to     "garamond",
      :optimizations :simple
      :target        :nodejs}}]})
