(defproject brave-ring "1.1.0"
  :description "Brave (zipkin) middleware for ring"
  :url "http://github.com/dbrenden/brave-ring"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [com.github.kristofa/brave-core "3.4.0"]
                 [prismatic/schema "0.4.4"]
                 [org.clojure/tools.logging "0.3.1"]])
