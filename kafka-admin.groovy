import org.apache.kafka.clients.admin.*
import org.apache.kafka.common.config.*
import groovy.json.JsonOutput
import groovy.json.JsonBuilder

@Grab(group = "org.apache.kafka", module = "kafka-clients", version = "2.0.0")
//@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.1.2')

Properties props = new Properties()
props.put('zk.connect', 'localhost:2181')
String kafkaHost = System.env['KAFKA_HOSTS']
props.put('bootstrap.servers', kafkaHost)

String topic = System.env['KAFKA_TOPIC'] ?: 'test'

AdminClient adminClient = AdminClient.create(props)

DescribeConfigsResult configs = adminClient.describeConfigs([new ConfigResource(ConfigResource.Type.TOPIC, topic)])

def obtainedConfig = configs.all().get()
obtainedConfig.each {key, val->
    println "Topic: ${key.name}"
    val.entries.each { cfg ->
        println "  $cfg.key=$cfg.value.value"
    }
}
