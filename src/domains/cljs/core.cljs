(ns garamond.domain.cljs.core
  "Contains the ClojureScript parser of the Garamond documentation generator"
  (:require [clojure.string :as str]
            [cljs.reader    :as reader]))

(def ^:private fs (js/require "fs"))

(defn make-doc-entry
  "Constructs a new doc entry map

   @param t      The type of the object. Possible values are :hierarchy,
                 :field or :function
   @param domain A domain tag for the entry, such as :fn or :ns
   @param n      The name of the new entry, usually some symbol name
   @param doc    A doc string, which will eventually go into the generated
                 output"
  [t domain n doc {:keys [private] :or {private false}}]
  {:type   t
   :domain domain
   :name   n
   :doc    doc
   :public (not private)})

(defn make-fn-doc-entry [domain n doc {:keys [parameters] :as opts}]
  (let [entry (make-doc-entry :function domain n doc opts)]
    (if parameters
      (assoc entry :parameters parameters)
      entry)))

(defn- maybe-doc [doc]
  (if (string? doc)
    doc
    ""))

(defn- get-defn-args [x]
  (let [syms (if (vector? (nth x 2))
               (nth x 2)
               (nth x 3))]
    (-> (map (fn [x]
               (let [n (cond
                         (symbol? x) (name x)
                         (map? x)    (or (-> x :as) "m")
                         (vector? x) "v")]
                 (hash-map :name n :doc "")))
             syms)
        (vec))))

(defmulti analyze-form
  (fn [form]
    (when (list? form)
      (first form))))

(defmethod analyze-form :default [_])

(defmethod analyze-form 'ns [[_ sym doc]]
  (make-doc-entry :hierachy
    :ns
    (name sym)
    (maybe-doc doc)
    (meta sym)))

(defmethod analyze-form 'def [[_ sym doc]]
  (make-doc-entry :field
    :def
    (name sym)
    (maybe-doc doc)
    (meta sym)))

(defmethod analyze-form 'defn [[_ sym doc  :as form]]
  (make-fn-doc-entry :fn
    (name sym)
    (maybe-doc doc)
    (assoc (meta sym)
      :parameters (get-defn-args form))))

(defmethod analyze-form 'defn- [[_ sym doc :as form]]
  (make-fn-doc-entry :fn
    (name sym)
    (maybe-doc doc)
    (assoc (meta sym)
      :private true
      :parameters (get-defn-args form))))

(defmethod analyze-form 'defmulti [[_ sym doc]]
  (make-doc-entry :function
    :method
    (name sym)
    (maybe-doc doc)
    (meta sym)))

(defmethod analyze-form 'defprotocol [[_ sym doc & forms]]
  (cons (make-doc-entry :hierachy
          :protocol
          (name sym)
          (maybe-doc doc))
        []))

(defn- form-seq
  "Create a seq from a string of ClojureScript forms"
  [s]
  (let [eof      (js/Object.)
        rdr      (reader/push-back-reader s)
        not-eof? (partial not= eof)]
    (->> (partial reader/read rdr false eof false)
         (repeatedly)
         (take-while not-eof?))))

(defn- parse
  "Parses a file with ClojureScript forms and prints
   documentation entries in JSON format, when doc strings
   are provided"
  [done err data]
  (if-not err
    (->> (form-seq data)
         (map analyze-form)
         (remove nil?)
         (map clj->js)
         (map JSON/stringify)
         (map println)
         (doall))
    (done nil))
  (done err))

(defn- parse-file [path]
  (.readFile fs path "utf-8" (partial parse (fn []))))

(defn walk-sources
  [path stat]
  (if (or (not stat) (.isDirectory stat))
    (mapcat
     (fn [entry]
       (let [subpath (str path "/" entry)
             stat    (.statSync fs subpath)]
         (walk-sources subpath stat)))
     (seq (.readdirSync fs path)))
    [path]))

(set! *main-cli-fn*
  (fn []
    (doseq [file
            (->> (walk-sources "src" nil)
                 (filter (fn [s] (.match s #".cljs$"))))]
      (parse-file file))))
