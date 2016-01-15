(ns esthatic.bootstrap)

(defn nav [opts brand menu-items & [right-items]]
  [:nav.navbar
   [:.container
    [:.navbar-header
     [:button.navbar-toggle.collapsed {:type "button" :data-toggle "collapse" :data-target "#navbar", :aria-expanded "false", :aria-controls "navbar"}
      [:span.sr-only "Toggle navigation"]
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.navbar-brand {:href "/"} brand]]
    [:div#navbar.navbar-collapse.collapsed
     [:ul.nav.navbar-nav menu-items]
     [:ul.nav.navbar-nav.navbar-right right-items]]]])

(defn nav-item [opts attrs cnt]
  [:li.active [:a attrs cnt]])
