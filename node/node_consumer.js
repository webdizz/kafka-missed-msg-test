const groupId = 'delta-calculator';
const kafkaHost = 'localhost:9092';

var bunyan = require('bunyan');
var logger = bunyan.createLogger({
  name: "test-consumer",
  streams: [
    {
      level: 'debug',
      stream: process.stdout
    },
    {
      level: 'debug',
      path: '/var/tmp/test-topic-consumer.log'
    }
  ]
});

const KafkaNodeLogging = require('kafka-node/logging');
KafkaNodeLogging.setLoggerProvider(() => {
  return {
    trace: logger.trace.bind(logger, {}),
    debug: logger.debug.bind(logger, {}),
    info : logger.info.bind(logger, {}),
    warn : logger.warn.bind(logger, {}),
    error: logger.error.bind(logger, {}),
  };
});
const Kafka = require('kafka-node');

async function wait(ms) {
  return new Promise(resolve => {
    setTimeout(resolve, ms);
  });
}

var topicName = 'test-topic';

const options = {
    groupId,
    autoCommit: true,
    autoCommitIntervalMs: 100,
    event_cb: true,
    rebalance_cb: true,
    sessionTimeout: 10000,
    fromOffset: 'earliest',
    outOfRangeOffset: 'earliest', // trying out
}

const payloads = [{
  topic: topicName
}];

consumer = new Kafka.ConsumerGroup(options, topicName);

consumer
  .on('message', async (message) => {
    logger.info(message.key.toString()); // ?
    await wait(100);
  })

  .on('ready', () => {
    consumer.subscribe([topicName]);
    consumer.consume();
  })

  // .on('rebalance', () => {
  //   logger.info('Rebalancing finished, can consume messages...');
  // })

  .on('connect', (data) => {
    logger.info({ topics: data }, 'Consumer connected to Kafka topics.');
  })
  //
  // .on('disconnected', (data) => {
  //   logger.warn({ topics: data }, 'Consumer disconnected from Kafka topics.');
  // })

  .on('event', (data) => {
    logger.debug({type: 'node_event'}, data);
  })

  .on('error', (err) => {
    logger.error({type: 'consumer_error'}, err);
  })

  .on('offsetOutOfRange', (err) => {
    logger.debug({type: 'node_outOfRange'}, err);
  });

consumer.connect();
