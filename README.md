# JMX Source Connector

The JmxSourceConnector is a [Source Connector](https://docs.confluent.io/current/connect/javadocs/index.html?org/apache/kafka/connect/source/SourceConnector.html) that collects JMX metrics and push them to Kafka.

Build status: [![CircleCI](https://circleci.com/gh/zigarn/kafka-connect-jmx.svg?style=svg)](https://circleci.com/gh/zigarn/kafka-connect-jmx)

## Configuration

| Name                    | Type   | Importance | Default Value | Validator | Documentation                                            |
| ----------------------- | ------ | ---------- | ------------- | --------- | ---------------------------------------------------------|
| `topic`                 | String | High       |               |           | The topic to publish data to                             |
| `jmx.url`               | String | High       |               |           | The JMX URL to fetch data from                           |
| `jmx.username`          | String | Medium     | `""`          |           | The username to connect to JMX                           |
| `jmx.password`          | String | Medium     | `[hidden]`    |           | The password to connect to JMX                           |
| `connection.attempts`   | Int    | Low        | `3`           | `[0,...]` | Maximum number of attempts to retrieve a JMX connection  |
| `connection.backoff.ms` | Long   | Low        | `10 000`      | `[0,...]` | Backoff time in milliseconds between connection attempts |


### Connect-standalone example

This configuration is used typically along with [standalone mode](https://docs.confluent.io/current/connect/concepts.html#standalone-workers):

```properties
name=jmx-connector
tasks.max=1

connector.class=com.zigarn.kafka.connect.jmx.JmxSourceConnector
topic=connect-jmx
jmx.url=service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi
# Set the following configuration if JMX authentication is necessary
jmx.username=jmx_user
jmx.password=jmx_password
```

### Connect-distributed

This configuration is used typically along with [distributed mode](http://docs.confluent.io/current/connect/concepts.html#distributed-workers):

```json
{
    "name": "jmx-connector",
    "config": {
        "connector.class": "com.zigarn.kafka.connect.jmx.JmxSourceConnector",
        "topic": "connect-jmx",
        "jmx.url": "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi",
        "connection.attempts": 5,
        "connection.backoff.ms": 1000
    }
}
```

Put this configuration in a `connector.json` file, then post it to one of the Kafka-connect workers:

```shell
curl -s -X POST -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors
```

## Output

For each MBean available on the JMX server, a record will be sent to the configured topic with the following structure:

 - key: [`Struct`](https://docs.confluent.io/current/connect/javadocs/org/apache/kafka/connect/data/Struct.html) with fields:
   - `jmx.url`: JMX service URL the MBean was retrieved from
   - `bean`: canonical name of the MBean
   - `timestamp`: timestamp when the MBean was retrieved
 - value: a map of MBean attributes with attribute name as key and String representation of attribute value as value

For example:

```json
// Key
{
  "jmx.url":"/jndi/rmi://localhost:9999/jmxrmi",
  "bean":"kafka.producer:client-id=confluent-control-center-heartbeat-sender-1-producer,type=producer-metrics",
  "timestamp":1516713540685
}
// Value
{
  "connection-creation-total":"4.0",
  "bufferpool-wait-time-total":"0.0",
  "batch-split-total":"0.0",
  "produce-throttle-time-max":"0.0",
  "select-rate":"0.3663979748184665",
  "connection-close-total":"0.0",
  "outgoing-byte-rate":"113.48899714429699",
  "record-send-total":"300.0",
  "batch-size-max":"137.0",
  "produce-throttle-time-avg":"0.0",
  "iotime-total":"6.6670434E7",
  "successful-authentication-total":"4.0",
  "batch-split-rate":"0.0",
  "io-waittime-total":"2.5733506471E11",
  "request-rate":"0.06719075455217363",
  "buffer-available-bytes":"3.3554432E7",
  ...
  "io-time-ns-avg":"90059.09090909091",
  "compression-rate-avg":"0.9496202588081359",
  "record-retry-rate":"0.0",
  "request-latency-max":"104.0",
  "record-size-max":"157.0",
  "select-total":"2.5733506471E11",
  "buffer-total-bytes":"3.3554432E7",
  "batch-size-avg":"136.4"
}
```

## Further documentation

Please see [documentation](docs).

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
