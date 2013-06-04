(ns garamond.filter.jdoc
  "Contains the ClojureScript parser of the Garamond documentation generator"
  (:require [clojure.string :as str]
            [cljs.reader    :as reader]))

(def ^:private json (js/require "JSONStream"))

(defn words [s]
  (str/join " " s))

(defn- meta-info [s]
  (try
    (when (string? s)
      (when-let [match (.match s #"^@([a-z]+)")]
        (keyword (aget match 1))))
    (catch js/Error e)))

(defn until-next-meta [in]
  (split-with (complement meta-info) (drop 1 in)))

(defmulti handle-doc-info
  (fn [entry in]
    (meta-info (first in))))

(defmethod handle-doc-info :default
  [entry in]
  [entry (rest in)])

(defmethod handle-doc-info :param
  [entry in]
  (let [[[n & doc] rest] (until-next-meta in)
        doc              (words doc)
        index            (->> (:parameters entry)
                              (keep-indexed (fn [i v] (when (= (:name v) n) i)))
                              (first))]
    (if index
      [(assoc-in entry [:parameters index :doc] doc) rest]
      [entry rest])))

(defn- simplify [s]
  (-> s
      (str/trim)
      (str/replace #" +" " ")))

(defn- split-words [s]
  (str/split s #" "))

(defn- tokenize [s]
  (->> s
       (str/split-lines)
       (map simplify)
       (mapcat split-words)))

(defn- apply-doc-info [x]
  (apply handle-doc-info x))

(defn- scan-doc-string [entry]
  (let [[doc tokens] (->> entry
                          :doc
                          tokenize
                          (split-with (complement meta-info)))]
    (assoc
      (->> [entry tokens]
           (iterate apply-doc-info)
           (drop-while (comp seq second))
           (first)
           (first))
      :doc (str/join " " doc))))

(set! *main-cli-fn*
  (fn []
    (let [parser (.parse json)]
      (.pipe (.-stdin js/process) parser)
      (.on parser "root"
           (fn [doc]
             (let [clj (js->clj doc :keywordize-keys true)]
               (println
                (if (= (:type clj) "function")
                  (-> clj
                      scan-doc-string
                      clj->js
                      JSON/stringify)
                  (JSON/stringify doc)))))))))
