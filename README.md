# Event Data Kafka Pusher

Simple glue component. Listens on a Kafka Topic with Events and pushes to the Event Bus via HTTP. This is used to bridge our two separate cloud installations.

# Configuration

 - `GLOBAL_EVENT_INPUT_TOPIC` - Topic that we're pushing. Percolator writes to this.
 - `GLOBAL_KAFKA_BOOTSTRAP_SERVERS` - Kafka connection string
 - `GLOBAL_EVENT_BUS_BASE` - Public URL of Event Bus. e.g. https://bus.eventdata.crossref.org

