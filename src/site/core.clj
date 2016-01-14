(ns site.core
  (:require [esthatic.core :as es]))

(defn menu [{data :data}]
  [:div#menu
   [:$style
    [:#menu {:border-bottom "1px solid #ddd"
             :border-bottom-color [:$c :gray]}
     [:.item {:display "inline-block"
              :padding  [:$px 10]
              :font-size [:$v 2]
              :line-height [:$v 3]}
      [:.icon {:height [:$v 3]
               :width [:$v 3]
               :display "inline-block"}
       [:.logo {:fill "none"
                :stroke [:$c :red]
                :stroke-width 3}]]]]]
   [:a.item
    [:.icon [:$svg "icon"]]
    " "
    (get-in data [:texts :brand])]
   (for [it (:menu data)]
     [:a.item (:title it)])])

(defn layout [{data :data :as opts} cnt]
  [:html
   [:head
    [:$cdn-css :bootsrtrap]
    [:$google-font :Exo-2]
    [:$style
     [:body {:padding "50px"
             :font-family "'Exo 2'"}]]]
   [:body
    [:.container
     (menu opts)
     cnt]]])

(defn index [{data :data :as opts}]
  [:.wrap
   [:h3 "Hello"]
   [:$inspect data :menu]])

(def routes
  {:GET #'index})

(comment
  (def stop (es/start {:routes #'routes
                       :styles {:$v 18
                                :$c {:red "red"
                                     :gray "#777"
                                     :blue "blue"}}
                       :layout #'layout}))
  (stop)
  )

