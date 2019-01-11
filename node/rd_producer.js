const NodeRdkafka = require('node-rdkafka');
const clientId = 'test-producer-client';
const kafkaHost = 'localhost:9092';

var bunyan = require('bunyan');
var logger = bunyan.createLogger({
  name: "test-producer",
  streams: [
    {
      level: 'info',
      stream: process.stdout
    },
    {
      level: 'debug',
      path: '/tmp/test-topic-producer.log'
    }
  ]
});

async function wait(ms) {
  return new Promise(resolve => {
    setTimeout(resolve, ms);
  });
}

//counter to stop this sample after maxMessages are sent
var counter = 0;
var maxMessages = 1000;
var topicName = 'test-topic';

var conf = {
  'metadata.broker.list': kafkaHost,
  'client.id': clientId,
  event_cb: true,
  'compression.codec': 'snappy',
  'retry.backoff.ms': 200,
  'message.send.max.retries': 10,
  'socket.keepalive.enable': true,
  'queue.buffering.max.messages': 1000,
  'queue.buffering.max.ms': 100,
  'batch.num.messages': 1000,
  dr_cb: true,
  'debug' : 'all',
};

const topicConf = {
  'request.required.acks': 1,
  'auto.offset.reset': 'earliest',
}

var producer = new NodeRdkafka.Producer(conf, topicConf);
producer.setPollInterval(100);

const test_message = require('./message');

producer
  .on('ready', async (arg) =>  {
    logger.info(`Producer ready. ${JSON.stringify(arg)}`);

    for (var i = 0; i < maxMessages; i++) {
      var value = Buffer.from(JSON.stringify(test_message));
      var key = "key-"+i;
      // if partition is set to -1, librdkafka will use the default partitioner
      var partition = 0;
      logger.info(`Sending a record '${i}'`);
      // producer.produce(topicName, partition, value);
      producer.produce(topicName, partition, value, key);
      producer.poll();
      await wait(10);
    }

    //need to keep polling for a while to ensure the delivery reports are received
    var pollLoop = setInterval(function() {
        producer.poll();
        if (counter === maxMessages) {
          clearInterval(pollLoop);
          producer.disconnect();
        }
      }, 1000);
  })

  .on('delivery-report', (err, report) => {
    logger.debug(`DeliveryReport: ${JSON.stringify(report)}`);
    counter++;
    if (err) {
      logger.error(err);
    }
  })

  .on('disconnected', (data) => {
    logger.warn({ topics: data }, 'Consumer disconnected from Kafka topics');
  })

  .on('event', (data) => {
    logger.debug({type: 'rd_event'}, data);
  })

  .on('event.log', function(log) {
    logger.debug(log);
  })

  .on('event.error', function(err) {
    logger.error({type: 'producer_error'}, err);
  })

  // .on('event.stats', (stats) => {
  //   logger.debug('RD Kafka statistic');
  //   logger.debug(stats);
  // })

  .on('event.throttle', (throttle) => {
    logger.error({type: 'rd_throttle'}, throttle);
  });

//starting the producer
producer.connect();
