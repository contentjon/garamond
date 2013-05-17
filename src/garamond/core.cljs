(ns garamond.core
  "Core of the Garamond documentation generator"
  (:require [cljs.reader :as reader]))

(def ^:private fs
  "Require FS from NodeJS"
  (js/require "fs"))

(defn make-doc-entry [t doc {:keys [private] :or {private false}}]
  {:type   t
   :doc    doc
   :public (not private)})

(defmulti analyze-form
  (fn [form]
    (when (list? form)
      (first form))))

(defmethod analyze-form :default [_])

(defmethod analyze-form 'ns [[_ sym doc]]
  (when (string? doc)
    (make-doc-entry :ns doc (meta sym))))

(defmethod analyze-form 'def [[_ sym doc]]
  (when (string? doc)
    (make-doc-entry :def doc (meta sym))))

(defmethod analyze-form 'defn [[_ sym doc]]
  (when (string? doc)
    (make-doc-entry :defn doc (meta sym))))

(defmethod analyze-form 'defn- [[_ sym doc]]
  (when (string? doc)
    (make-doc-entry :defn doc (assoc (meta sym) :private true))))

(defn- form-seq
  "Create a seq from a string of ClojureScript forms"
  [s]
  (let [eof      (js/Object.)
        rdr      (reader/push-back-reader s)
        not-eof? (partial not= eof)]
    (->> (partial reader/read rdr false eof false)
         (repeatedly)
         (take-while not-eof?))))

(defn parse
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

(defn parse-file [path]
  (.readFile fs path "utf-8" (partial parse (fn []))))

(set! *main-cli-fn*
  (fn []
    (parse-file "src/garamond/core.cljs")))
