(ns esthatic.data
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [markdown.core :as md]
            [clj-yaml.core :as yaml]))

(defn markdown [str]
  (md/md-to-html-string str))

(defn load-text [f]
  (markdown (slurp (str "resources/" f))))

(defn load-yaml [f]
  (yaml/parse-string (slurp (str "resources/" f))))

(defn from-yaml [str] (yaml/parse-string str))

(defn to-yaml [data]
  (yaml/generate-string data))

(defn fs-join [& parts] (.getPath (apply io/file parts)))

(defn load []
  (reduce
   (fn [acc fl] (assoc acc (keyword (fs/base-name fl ".yaml"))
                       (from-yaml (slurp fl))))
   {}
   (fs/glob (fs-join "resources/data" "*.yaml"))))


(defn data [& ks] (get-in (load) ks))

(data [:texts :brand])
