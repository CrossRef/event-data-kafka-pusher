(ns event-data-kafka-pusher.core
  (require [event-data-common.event-bus :as event-bus]
           [clojure.data.json :as json]
           [clojure.tools.logging :as log]
           [config.core :refer [env]]
           [taoensso.timbre :as timbre])
  (:import [org.apache.kafka.clients.consumer KafkaConsumer Consumer ConsumerRecords])
  (:gen-class))



(defn -main
  "I don't do a whole lot ... yet."
  [& args]
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
             ; The artifact corresponds provides the config for this instances.
             ; So use the artifact name as the group name to ensure one group per instance.
             "group.id" "pusher"
             "key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
             "value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer"
             ; Always start from the beginning of time. 
             ; This means ingesting a few weeks of data on startup, but it's quick.
             "auto.offset.reset" "earliest"
             "fetch.max.bytes" "52428800"})]

     (log/info "Subscribing to" topic-name)
     (.subscribe consumer (list topic-name))
     (log/info "Subscribed to" topic-name)
     (loop []
       (let [^ConsumerRecords records (.poll consumer (int 1000))]
         (doseq [^ConsumerRecords record records]
           (event-bus/post-event (json/read-str (.value record) :key-fn keyword))))
        (recur)))
   
  (catch Exception e (log/error "Error in Topic listener " (.getMessage e))))
  (log/error "Stopped listening to Topic"))

