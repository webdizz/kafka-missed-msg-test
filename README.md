How does Kafka miss a message
========================

## Run consumer

```sh
docker-compose run client kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --offset 4 --partition 0
```

## Create Kafka topic

```sh 
docker-compose exec kafka kafka-topics.sh --zookeeper zookeeper:2181 --create  --replication-factor 1 --partitions 3 --topic test
```


## How to run

```sh
KAFKA_HOST=306837fc86da groovy kafka-producer.groovy 
KAFKA_HOST=306837fc86da groovy kafka-consumer.groovy 
```