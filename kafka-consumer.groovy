import org.apache.kafka.clients.producer.Callback
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "2.0.0")
//@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

Properties consumerProps = new Properties()
consumerProps.put('zk.connect', 'localhost:2181')
String kafkaHost = System.env['KAFKA_HOST']
consumerProps.put('bootstrap.servers', kafkaHost + ':9092')
consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "my-group")
consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer)
consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer)

final int MSG_THRESHOLD = 5

def consumer = new KafkaConsumer<String, String>(consumerProps)
consumer.subscribe(['test'])

def consumedMessages = new File('consumed.tsv')

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
            consumedMessages << "${record.offset()}\t${record.value.substring(0, 40)}\n"
        }
        // commits the offset of record to broker.
        consumer.commitAsync()
    }

}

//while (true) {
consumeMessage()
//}

consumer.close()


