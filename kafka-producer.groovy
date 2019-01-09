import org.apache.kafka.clients.producer.*
import org.apache.kafka.common.serialization.StringSerializer

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "2.0.0")
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

Properties producerProps = new Properties()
producerProps.put('zk.connect', 'localhost:2181')
String kafkaHost = System.env['KAFKA_HOSTS']
producerProps.put('bootstrap.servers', kafkaHost)
producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer)
producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer)
println producerProps

private String genTimeStamp() {
    LocalDateTime date = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-m-s-S");
    String timeStamp = date.format(formatter);
    timeStamp
}

def producer = new KafkaProducer(producerProps)

def sentMessagesFile = new File('sent.tsv')

def msg = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a, tellus. Phasellus viverra nulla ut metus varius laoreet. Quisque rutrum. Aenean imperdiet. Etiam ultricies nisi vel augue. Curabitur ullamcorper ultricies nisi. Nam eget dui. Etiam rhoncus. Maecenas tempus, tellus eget condimentum rhoncus, sem quam semper libero, sit amet adipiscing sem neque sed ipsum. Nam quam nunc, blandit vel, luctus pulvinar, hendrerit id, lorem. Maecenas nec odio et ante tincidunt tempus. Donec vitae sapien ut libero venenatis faucibus. Nullam quis ante. Etiam sit amet orci eget eros faucibus tincidunt. Duis leo. Sed fringilla mauris sit amet nibh. Donec sodales sagittis magna. Sed consequat, leo eget bibendum sodales, augue velit cursus nunc, quis gravida magna mi a libero. Fusce vulputate eleifend sapien. Vestibulum purus quam, scelerisque ut, mollis sed, nonummy id, metus. Nullam accumsan lorem in dui. Cras ultricies mi eu turpis hendrerit fringilla. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; In ac dui quis mi consectetuer lacinia. Nam pretium turpis et arcu. Duis arcu tortor, suscipit eget, imperdiet nec, imperdiet iaculis, ipsum. Sed aliquam ultrices mauris. Integer ante arcu, accumsan a, consectetuer eget, posuere ut, mauris. Praesent adipiscing. Phasellus ullamcorper ipsum rutrum nunc. Nunc nonummy metus. Vestib"

def sendMessage = { String topic, String message ->
    String key = new Random().nextLong()
    String timeStamp = genTimeStamp()

    producer.send(
            new ProducerRecord<String, String>(topic, key, message),
            { RecordMetadata metadata, Exception e ->
                sentMessagesFile << "${metadata.offset()}\t${timeStamp}\t${message.substring(0, 40)}\n"
            } as Callback
    )
}

while (true) {
    String randomMsg = new Random().nextLong() + msg
    sendMessage('test', randomMsg)
    Thread.sleep(50)
}

producer.close()