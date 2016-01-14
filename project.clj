(defproject fhirbase-static "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [markdown-clj "0.9.82"]
                 [hiccup "1.0.5"]
                 [http-kit "2.2.0-SNAPSHOT"]
                 [route-map "0.0.2"]
                 [garden "1.3.0"]
                 [ring "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [circleci/clj-yaml "0.5.5"]
                 [me.raynes/fs "1.4.6"]]

  :aliases {"generate" ["trampoline" "run" "-m" "site.generator"]}
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler site.server/app :auto-refresh? true}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]]}})
