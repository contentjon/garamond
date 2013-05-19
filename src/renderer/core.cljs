(ns garamond.core)

(def fs (js/require "fs"))
(def mu (js/require "mu2"))

(aset mu "root" "templates")

(defn render [description]
  (let [out    (atom "")
        stream (.compileAndRender mu "api.html" description)]
    (.on stream "data"
      (fn [data]
        (swap! out str (.toString data))))
    (.on stream "end"
      (fn []
        (.writeFile fs "api.html" @out
          (fn [err]
            (when err
              (.log js/console err))))))))

(set! *main-cli-fn*
  (fn []
    (render
     (clj->js
      {:functions
       [{:name        "render"
         :description "Renders code descriptors into html files using mustache"
         :args        [{:name        "description"
                        :description "A description map of a unit of code"}]}
        {:name        "compute"
         :description "Do a very complex computation"
         :args        [{:name        "foo"
                        :description "Foo is really important"}
                       {:name        "bar"
                        :description "Bar determines the barness of the computation"}]}]}))))
