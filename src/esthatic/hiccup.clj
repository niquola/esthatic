(ns esthatic.hiccup
  (:require [hiccup.core :as hiccup]
            [gardner.core :as css]
            [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [clojure.walk :as walk]
            [clojure.java.io :as io]))


(defn garden-css [req gss]
  [:style {:type "text/css"} (css/css gss)])

(def default-rules
  {:css garden-css})

(defn pre-process [req hic]
  (walk/prewalk (fn [x]
                  (if-let [h (and (vector? x) (or (get (:hiccup/macro req) (first x))
                                                  (get default-rules (first x))))]
                    (apply h req (conj (rest x)))
                    (if (and (vector? x) (.startsWith (name (first x)) "."))
                      (into [(keyword (str "div" (name (first x))))] (rest x))
                      x)))
                hic))

(defn html [req hic]
  (println "macro" (:hiccup/macro req))
  (hiccup/html (pre-process req hic)))

(def yaml-hiccup
  {:yaml/debug (fn [r & keys]
                 [:pre
                  (when-let [d (get-in r (into [:data]  keys))]
                    (yaml/generate-string d :dumper-options {:flow-style :block}))])})


(defn get-data [r ks]
  (get-in r (into [:data]  keys)))

(def data-hiccup
  {:> (fn [r & keys]
        (when-let [d (get-data r keys)]
          (str d)))
   
   :data/table (fn [r & keys]
                 (if-let [d (get-data r keys)]
                   (let [hs (mapv first (first d))]
                     [:table.table
                      [:thead
                       [:tr (for [h hs] [:th (name h)])]]
                      [:tbody
                       (for [r d]
                         [:tr (for [h hs]
                                [:td (get r h "~")])])]])))})

(def markdown-hiccup
  {:md/doc (fn [r path]
             [:div.markdown
              (-> path
                  (io/resource)
                  (slurp)
                  (md/md-to-html-string))])})

(defn fa-icon [nm]
  [:i.fa {:class (str "fa-" (name nm))}])

(def fa-icon-hiccup
  {:fa/css (fn [r]
             [:link
              {:src "https://maxcdn.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css"
               :rel "stylesheet" :type "text/css"}])
   
   :fa/icon (fn [r nm] (fa-icon nm))})

(defn get-data [req pth]
  (get-in req (into [:data] pth)))

(def bootstrap-hiccup
  {:bs/css (fn [opts & _]
             [:link {:href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
                     :rel "stylesheet" :type "text/css"}])

   :bs/menu (fn [req opts]
              (let [data (get-data req (or (:source opts) [:menu]))]
                [:nav.navbar
                 [:.container
                  [:.navbar-header
                   [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse" :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
                    [:span.sr-only "Toggle navigation"]
                    [:span.icon-bar]
                    [:span.icon-bar]
                    [:span.icon-bar]]
                   [:a.navbar-brand {:href "/"} (:brand data)]]
                  [:div#navbar.navbar-collapse.collapsed
                   [:ul.nav.navbar-nav
                    (for [i (:items data)]
                      [:li.active [:a {:href (:href i)}
                                   (when-let  [ic (:icon i)] (fa-icon i))
                                   (:text i)]])]
                   #_[:ul.nav.navbar-nav.navbar-right right-items]]]])) 
   })
(comment
  (html {:hiccup/macro {:es/js (fn [req src]
                                 [:script {:src src :type "javascript"}])}}
        [:html
         [:head [:es/js "app.js"]
          [:css [:body {:padding "10px"}]]]
         [:body
          [:.container
           [:h1 "Hello"]
           ]]])
  )
