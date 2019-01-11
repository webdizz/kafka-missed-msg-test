import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "2.0.0")
//@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

String groupId = System.env['KAFKA_GROUP_ID'] ?:"groovy_2_earliest"

Properties consumerProps = new Properties()
consumerProps.put('zk.connect', 'localhost:2181')
String kafkaHost = System.env['KAFKA_HOSTS']
consumerProps.put('bootstrap.servers', kafkaHost)
// consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer")
// consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "groovy_1")
consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer)
consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer)
consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
consumerProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "100")
consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000")
consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

private String genTimeStamp() {
    LocalDateTime date = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-m-s-S");
    String timeStamp = date.format(formatter);
    timeStamp
}

final int MSG_THRESHOLD = 5

def consumer = new KafkaConsumer<String, String>(consumerProps)
consumer.subscribe(['test'])

def consumedMessages = new File(groupId+'_consumed.tsv')

def consumeMessage = {
    int noMessageFound = 0
    while (true) {
        ConsumerRecords<String, String> consumerRecords = consumer.poll(1000)
        // 1000 is the time in milliseconds consumer will wait if no record is found at broker.
        if (consumerRecords.isEmpty()) {
            noMessageFound++
            if (noMessageFound > MSG_THRESHOLD) {
                // If no message found count is reached to threshold exit loop.
                println "giving up"
                break
            } else {
                continue
            }
        }
        //print each record.
        for (ConsumerRecord<String, String> record in consumerRecords) {
            String timeStamp = genTimeStamp()
            //consumedMessages << "${record.offset()}\t${timeStamp}\t${record.value.substring(0, 40)}\n"
            println record.offset()
            Thread.sleep(1000)
        }
        // commits the offset of record to broker.
        //consumer.commitAsync()
    }
}

consumeMessage()
consumer.close()
