(ns example.core
  (:require
   [esthatic.core :as es]
   [esthatic.data :as esd]
   [esthatic.hiccup :as esh]))


(def blue "#4e83bf")

(defn *layout [h req]
  [:html
   [:head
    [:title "test"]
    [:bs/css]
    [:fa/css]]
   [:body
    [:css
     [:body
      {:font-size "16px"}
      [:img.logo {:left "20px" :height "50px" :position "absolute"}]
      [:nav.navbar
       {:border-bottom "1px solid #ddd"
        :margin 0
        }
       [:a {:color "#888"
            :font-size "18px"}]]]]

    [:img.logo {:src "ico-s.png"}]
    [:bs/menu {:source [:menu]}]

    (h req)]])

(defn layout [h]
  (fn [req] (*layout h req)))

(defn sublayout [h]
  (fn [req]
    [:div.wrap
     [:div.container [:a {:href "/index"} "home"]]
     (h req)]))

(defn index [req]
  [:div
   [:css
    [:body
     [:#moto {:padding "60px 0 80px"
              :background-color "#f1f1f1"
              :margin-bottom "40px"
              :border-bottom "1px solid #ddd"}
      [:img {:display "block"
             :width "auto"
             :height "160px"
             :margin "0 auto"}]
      [:h1 {:font-size "60px"
            :text-shadow "-1px -1px 1px white"
            :color blue
            :text-transform "lowercase"
            :text-align "center"}]
      [:h4 {:font-size "30px"
            :color "#666"
            :font-weight "300"
            :text-align "center"}]]
     [:.md {:font-size "20px"
            :color "#666"
            :text-shadow "-1px -1px 1px white"
            :font-weight "400"}]
     ]]
   [:div#moto
    [:h1 [:> :menu :brand]]
    [:img {:src "ico-b.png"}]
    [:h4 [:> :menu :moto]]]
   [:.container
    [:.row
     (for [f (get-in req [:data :menu :features])]
       [:.col-md-3
        [:h4 (:title f)]
        [:p (:text f)]])]]])

(defn getting-started [req]
  [:div.container
   [:md/doc "getting-started.md"]])

(defn docs [req]
  [:div.container
   [:md/doc "documentation.md"]])

(def routes
  {:es/mw [(esd/with-yaml :menu "menu.yaml")
           #'es/hiccup-mw
           #'layout]
   :. #'index
   "getting-started" {:. #'getting-started}
   "docs" {:. #'docs}
   "index" {:. #'index}})

(def config
  {:port 8888
   :es/hiccup [#'esh/bootstrap-hiccup
               #'esh/yaml-hiccup
               #'esh/data-hiccup
               #'esh/fa-icon-hiccup
               #'esh/markdown-hiccup]

   :es/routes #'routes })

(comment
  (es/restart config)
  )

