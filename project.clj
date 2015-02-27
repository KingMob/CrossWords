(defproject CrossWords "0.2.0"
  :description "A wonderful little program to turn sentences into crosswords"
  :url "https://www.github.com/KingMob/CrossWords"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/math.numeric-tower "0.0.4"]
                 [org.clojure/math.combinatorics "0.0.8"]]
  :profiles {
            :dev {
                  :dependencies [[midje "1.6.3" :exclusions [org.clojure/clojure]]]
                  :plugins [[lein-midje "3.1.3"]]}
            :uberjar {:aot :all}}

  :main CrossWords.core)
