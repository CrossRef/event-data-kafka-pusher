(ns event-data-kafka-pusher.core
  (require [event-data-common.event-bus :as event-bus]
           [clojure.data.json :as json]
           [clojure.tools.logging :as log]
           [config.core :refer [env]]
           [taoensso.timbre :as timbre])
  (:import [org.apache.kafka.clients.consumer KafkaConsumer Consumer ConsumerRecords])
  (:gen-class))

(defn run
  []
  (timbre/merge-config!
    {:ns-blacklist [
       ; Kafka's DEBUG is overly chatty.
       "org.apache.kafka.clients.consumer.internals.*"
       "org.apache.kafka.clients.NetworkClient"]
     :level :info})
  (try
    (let [topic-name (:global-event-input-topic env)
          consumer 
          (KafkaConsumer.
            {"bootstrap.servers" (:global-kafka-bootstrap-servers env)     
             "group.id" "kafka-pusher"
             "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
             "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
             ; Always start from the beginning of time. 
             ; This means ingesting a few weeks of data on startup, and possibly
             ; ingesting the same Event twice. event-bus/post-event handles duplicates
             ; so this won't cause an issue.
             "auto.offset.reset" "earliest"
             "fetch.max.bytes" "52428800"})]

     (log/info "Subscribing to" topic-name)
     (.subscribe consumer (list topic-name))
     (log/info "Subscribed to" topic-name)
     (loop []
       (let [^ConsumerRecords records (.poll consumer (int 1000))]
         (doseq [^ConsumerRecords record records]
           (let [event (json/read-str (.value record) :key-fn keyword)
                 ; Percolator addds a JWT field. Remove this, as it's a secret.
                 ; The event-bus/post-event will generate the required JWT when it sends.
                 event (dissoc event :jwt)]
             (log/info "Send" (:id event))
             (event-bus/post-event event))))
        (recur)))
   
  (catch Exception e (log/error "Error in Topic listener" e)))
  (log/error "Stopped listening to Topic"))


(defn -main [& args]
  (run))
