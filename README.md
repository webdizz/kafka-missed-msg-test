How does Kafka miss a message
========================

## Run consumer

```sh
docker-compose run client kafka-console-consumer.sh --bootstrap-server kafka.dev:9092 --topic test --offset 4 --partition 0
```

## Create Kafka topic

```sh 
docker-compose exec kafka kafka-topics.sh --zookeeper zookeeper.dev:2181 --create  --replication-factor 3 --partitions 3 --topic test
```

## How to run

```sh
KAFKA_HOSTS=kafka.dev:9092 groovy kafka-producer.groovy
KAFKA_HOSTS=kafka.dev:9092 groovy kafka-producer.groovy 
```

## How to configure Burrow notifier

https://godoc.org/github.com/linkedin/Burrow/core/protocol#ConsumerOffset