## Run consumer

dc run client kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic test --offset 4 --partition 0

dc run client connect-standalone.sh /connect/connect-standalone.properties  /connect/connect-file-source.properties