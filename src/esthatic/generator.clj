(ns esthatic.generator
  (:require [clojure.string :as str]
            [me.raynes.fs :as fs]))

(defn dump [path res]
  (let [dir (butlast path)
        dir-path (str "dist/" (str/join "/" dir))
        fl  (or (last path) "index")
        fl-path (str dir-path "/" fl ".html")]
    (println "page: " fl-path)
    (fs/mkdir dir-path)
    (spit fl-path res)))

(defn safe-dispatch [& args]
  )

(defn *generate [{path :path params :params} {routes :routes :as config}]
  (doseq [[k v] routes]
    (println path k)
    (cond
      (= :GET k) (let [uri (str/join "/" path)
                       req {:uri uri :request-method k}
                       res (try
                             ((:dispatch config) req)
                             (catch Exception e
                               (println "ERROR: " req (.toString e))
                               {:body (str "<pre>" e "</pre>")}))]
                   (dump path (:body res)))
      (vector? k) (let [k (first k)
                        gen (get routes k)]
                    (println gen)
                    (doseq [pv (gen)]
                      (*generate {:path (conj path pv) :params (assoc params k pv)} v)))
      (keyword? k) "nop"
      :else (*generate {:path (conj path k) :params params} v))))


(defn generate [config]
  (println "Generating into dist:")
  (fs/delete-dir "dist")
  (fs/mkdir "dist")
  (fs/copy "CNAME" "dist/CNAME")
  (doseq [f (fs/glob "resources/assets/*")]
    (println "assets: " (fs/base-name f))
    ((if (fs/directory? f) fs/copy-dir fs/copy)
     (.getPath f) (str "dist/" (fs/base-name f))))

  (*generate {:path [] :params {}} config)
  (println "Done!"))
