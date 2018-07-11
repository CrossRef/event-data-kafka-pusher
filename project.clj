(defproject event-data-kafka-pusher "0.1.1"
  :description "Crossref Event Data Kafka Pusher"
  :url "http://eventdata.crossref.org"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.apache.kafka/kafka-clients "0.10.2.0"]
                 [yogthos/config "0.8"]
                 [event-data-common "0.1.57"]
                 [clj-time "0.14.4"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]]
  :main ^:skip-aot event-data-kafka-pusher.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
