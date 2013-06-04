(ns cli.core
  (:require-macros [kit.core :refer (?)]))

(def ^:private spawn (? (js/require "child_process") :spawn))

(def ^:private args
  (-> (js/require "optimist")
      (.usage "garamond -d <domain> -r <renderer> [-f <filter>]+")
      (.demand (array "d" "r"))
      (.describe "d" "The language domain to use for the source")
      (.describe "r" "The output generator to use")
      (.describe "f" "A filter that can transform the documentation in some way. You can have multiple of these")
      (.alias "r" "renderer")
      (.alias "f" "filter")
      (.-argv)))

(defn- wrap-seq
  "Returns a seq if the argument is seqable.
   If it is not seqable, wrap it in a result vector of size 1, 
   unless the argument is nil, in which case nil is returned."
  [x]
  (if (or (nil? x) (seqable? x))
    (seq x)
    (vector x)))

(defn- connect
  "Connect to processes a and b via a stdin/out pipe"
  [a b]
  (.pipe (? a :stdout) (? b :stdin))
  (.pipe (? a :stderr) (? js/process :stderr))
  b)

(defn- redirect-renderer
  "Write the renderer output to this processes output streams"
  [renderer]
  (.pipe (? renderer :stdout) (? js/process :stdout))
  (.pipe (? renderer :stderr) (? js/process :stderr)))

(defn- main []
  (let [domain        (? args :d)
        renderer      (? args :r)
        filters       (wrap-seq (? args :f))
        process-chain (concat [domain] filters [renderer])]
    (->> (map spawn process-chain)
         (reduce connect)
         (redirect-renderer))))

(set! *main-cli-fn* main)
