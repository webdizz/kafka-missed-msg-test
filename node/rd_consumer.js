const NodeRdkafka = require('node-rdkafka');
const groupId = 'delta-calculator';
// const groupId = 'test-consumer-group';
const kafkaHost = 'localhost:9092';

var bunyan = require('bunyan');
var logger = bunyan.createLogger({
  name: "test-consumer",
  streams: [
    {
      level: 'info',
      stream: process.stdout
    },
    {
      level: 'debug',
      path: '/var/tmp/test-topic-consumer.log'
    }
  ]
});

async function wait(ms) {
  return new Promise(resolve => {
    setTimeout(resolve, ms);
  });
}

var topicName = 'test-topic';

const conf = {
  'metadata.broker.list': kafkaHost,
  'group.id': groupId,
  'enable.auto.commit': true,
  'auto.commit.interval.ms': 100,
  event_cb: true,
  rebalance_cb: true,
  'session.timeout.ms': 10000,
  'debug' : 'all',
  'statistics.interval.ms': 1000,
};

const topicConf = {
  'request.required.acks': 1,
  'auto.offset.reset': 'earliest',
}

consumer = new NodeRdkafka.KafkaConsumer(conf, topicConf);

consumer
  .on('data', async (message) => {
    logger.info(message.key.toString());
    await wait(100);
  })

  .on('ready', () => {
    consumer.subscribe([topicName]);
    consumer.consume();
  })

  .on('rebalance', () => {
    logger.info('Rebalancing finished, can consume messages...');
  })

  .on('subscribed', (data) => {
    logger.info({ topics: data }, 'Consumer connected to Kafka topics.');
  })

  .on('disconnected', (data) => {
    logger.warn({ topics: data }, 'Consumer disconnected from Kafka topics.');
  })

  .on('event', (data) => {
    logger.debug({type: 'rd_event'}, data);
  })

  .on('event.error', (err) => {
    logger.error({type: 'consumer_error'}, err);
  })

  // .on('event.stats', (stats) => {
  //   logger.debug('RD Kafka statistic');
  //   logger.debug(stats);
  // })

  .on('event.throttle', (throttle) => {
    logger.debug({type: 'rd_throttle'}, throttle);
  });

consumer.connect();
