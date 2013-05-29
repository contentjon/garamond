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

(defn update-function-model [events]
  (->> events
       (filter #(= (:type %) "function"))
       (filter :public)
       (generate-fn-detail)))

(defn mustache-model [events]
  (let [namespaces (partition-by #(= (:domain %) "ns") events)]
    (reduce (fn [description [ns functions]]
              (let [functions (update-function-model functions)]
                (update-in description
                           [:namespaces]
                           conj
                           (assoc (first ns) :functions functions))))
            {:namespaces []}
            (partition 2 namespaces))))

(set! *main-cli-fn*
  (fn []
    (let [parser (.parse json)
          events (atom [])]
      (.pipe (.-stdin js/process) parser)
      (.on parser "root"
           (fn [doc]
             (let [doc (js->clj doc :keywordize-keys true)]
               (swap! events conj doc))))
      (.on parser "end"
           (fn []
             (render (mustache-model @events)))))))
