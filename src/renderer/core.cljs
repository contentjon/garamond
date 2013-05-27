(ns garamond.core)

(def fs   (js/require "fs"))
(def mu   (js/require "mu2"))
(def json (js/require "JSONStream"))

(aset mu "root" "templates")

(defn render [description]
  (let [out    (atom "")
        stream (.compileAndRender mu "api.html" (clj->js description))]
    (.on stream "data"
      (fn [data]
        (swap! out str (.toString data))))
    (.on stream "end"
      (fn []
        (.writeFile fs "api.html" @out
          (fn [err]
            (when err
              (.log js/console err))))))))

(defn generate-fn-detail [fns]
  (map (fn [f]
         (reduce (fn [f parameter]
                   (if-not (= (:doc parameter) "")
                     (update-in f [:detail]
                                conj
                                (hash-map
                                  :name (:name parameter)
                                  :doc  (:doc parameter)))
                     f))
                 (assoc f :detail [])
                 (:parameters f)))
       fns))

(defn mustache-model [description]
  (-> description
      (update-in [:functions] (partial filter :public))
      (update-in [:functions] generate-fn-detail)))

(set! *main-cli-fn*
  (fn []
    (let [parser      (.parse json)
          description (atom {})]
      (.pipe (.-stdin js/process) parser)
      (.on parser "root"
           (fn [doc]
             (let [doc (js->clj doc :keywordize-keys true)]
               (when (= (:type doc) "function")
                 (swap! description
                   update-in
                   [:functions]
                   conj doc)))))
      (.on parser "end"
           (fn []
             (render (mustache-model @description)))))))
