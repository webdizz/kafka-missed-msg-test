How does Kafka miss a message
========================

## Run consumer

dc run client kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --offset 4 --partition 0

## Create Kafka topic
dc exec kafka kafka-topics.sh --zookeeper zookeeper:2181 --create  --replication-factor 1 --partitions 3 --topic test