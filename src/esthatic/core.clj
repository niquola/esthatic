(ns esthatic.core
  (:require [clojure.string :as str]
            [hiccup.page :as hp]
            [ring.middleware.resource :as rmr]
            [ring.middleware.defaults :as rmd]
            [ring.middleware.reload :as rml]
            [esthatic.hiccup :as hiccup]
            [org.httpkit.server :as srv]
            [esthatic.data :as data]
            [esthatic.bootstrap :as bootstrap]
            [esthatic.generator :as gen]
            [clojure.walk :as walk]
            [route-map.core :as rt]
            [clojure.java.io :as io]))

(defn build-stack [h mws]
  ((apply comp mws) h))

(defn hiccup-mw [h]
  (fn [req]
    (let [res (h req)
          macro (apply merge (map (fn [x] (if (var? x) (var-get x) x)) (get req :es/hiccup)))]
      {:body    (hiccup/html (update req :hiccup/macro merge macro) res)
       :headers {"Content-Type" "text/html; charset=utf-8"}
       :status  200})))

(defn dispatch [{params :params uri :uri
                 routes :es/routes :as req}]
  (if-let [rt (rt/match (str/replace uri #".html$" "") routes)]
    (let [mws (filterv identity (mapcat :es/mw (or (:parents rt) [])))
          req (merge (update req :params merge (:params rt)))
          handler (build-stack (:match rt) mws)]
      (println (:match rt) mws)
      (handler req))
    {:body (str uri " not found: " (str/replace uri #".html$" "")  " " (pr-str (if (var? routes) (var-get routes) routes))) :status 404}))

(defn mk-handler [opts]
  (-> (fn [req] (dispatch (merge req opts)))
      (rml/wrap-reload)
      (rmr/wrap-resource "assets")
      (rmd/wrap-defaults (merge rmd/site-defaults (merge opts {:security {:anti-forgery false}})))))



(defonce server (atom nil))

(defn restart [opts]
  (when-let [s @server] (s) (reset! server nil))
  (reset! server (srv/run-server (mk-handler opts) {:port (or (:port opts) 9090)})))

