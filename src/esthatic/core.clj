(ns esthatic.core
  (:require [clojure.string :as str]
            [hiccup.page :as hp]
            [ring.middleware.resource :as rmr]
            [ring.middleware.defaults :as rmd]
            [hiccup.core :refer [html]]
            [org.httpkit.server :as srv]
            [esthatic.data :as data]
            [esthatic.bootstrap :as bootstrap]
            [esthatic.generator :as gen]
            [garden.core :as garden]
            [garden.units :as units]
            [clojure.walk :as walk]
            [route-map.core :as rt]))

(defn svg [opts path]
  (slurp (str  "resources/assets/" (name path) ".svg")))

(defn $c [opts x]
  (let [styles (deref (:styles opts))]
    (get-in styles [:$c x])))

(defn $px [opts x] (units/px* x))

(defn $v [opts x]
  (let [styles (deref (:styles opts))]
    (units/px* x (or (get-in styles [:$v]) 18))))

(defn $border [opts & keys]
  (let [styles (deref (:styles opts))]
    (reduce
     (fn [acc k]
       (cond
         (number? k) (assoc acc :width ($px opts k))
         (keyword? k) (assoc acc :color ($c opts k))))
     {:style "solid"}
     keys)))

(def css-rules
  {:$v $v
   :$px $px
   :$border $border
   :$c $c})

(defn css-process [opts grdn]
  (println "css" opts)
  (walk/prewalk (fn [x]
                  (if-let [h (and (vector? x) (get css-rules (first x)))]
                    (apply h opts (rest x))
                    x))
                grdn))


(defn style [opts garden-rules]
  (println "Style")
  [:style {:type "text/css"}
   (garden/css (css-process opts garden-rules))])

(def css-s
  {:bootsrtrap "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"})

(defn css-link [opts href]
  [:link {:href href :rel "stylesheet" :type "text/css"}])

(defn cdn-css [opts k]
  (css-link opts (get css-s k)))

(defn google-font [opts nm]
  (css-link
   opts
   (str "https://fonts.googleapis.com/css?family="
        (str/replace (name nm) #"-" "+")
        ":400,100,100italic,200,200italic,300,300italic,400italic,500,900italic,500italic,600,600italic,700,700italic,800,800italic,900")))

(defn inspect-data
  ([opts data] [:pre (data/to-yaml data)])
  ([opts data & ks] (inspect-data opts (get-in data ks))))


(defn br [opts & [x]]
  [:div {:style (garden/style
                 {:height
                  (units/px*
                   (or (get-in opts [:styles :$v]) 18)
                   (or x 1))})}])

(def pre-rules
  {:$style style
   :$svg svg
   :$nav bootstrap/nav
   :$nav-item bootstrap/nav-item
   :$cdn-css cdn-css
   :$inspect inspect-data
   :$nbsp (fn [& _] "&nbsp;")
   :$br br
   :$google-font google-font})

(defn pre-process [opts hic]
  (walk/prewalk (fn [x]
                  (println x)
                  (if-let [h (and (vector? x) (get pre-rules (first x)))]
                    (apply h opts (conj (rest x)))
                    (if (and (vector? x) (.startsWith (name (first x)) "."))
                      (into [(keyword (str "div" (name (first x))))] (rest x))
                      x)
                    ))
                hic))

(defn http [opts hic]
  {:body    (html (pre-process opts hic))
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :status  200})



(defn mk-dispatch [{routes :routes layout :layout :as opts}]
  (fn [{params :params uri :uri meth :request-method :as req}]
    (if-let [mtch (rt/match [meth (str/replace uri #".html$" "")] routes)]
      (let [req (merge (update req :params merge (:params mtch))
                       {:data (data/load)})]
        (http opts (layout req ((:match mtch) req))))
      {:body "ups" :method 404})))

(defn mk-handler [{routes :routes port :port :as opts}]
  (-> (mk-dispatch opts)
      (rmr/wrap-resource "assets")
      (rmd/wrap-defaults rmd/site-defaults)))

(defn start [{routes :routes port :port :as opts}]
  (srv/run-server (mk-handler opts) {:port (or port 8080)}))

(defn generate [config]
  (gen/generate
   (assoc config :dispatch (mk-handler config))))


