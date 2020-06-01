# Spark Streaming 整合 Kafka

<nav>
<a href="#一版本说明">一、版本说明</a><br/>
<a href="#二项目依赖">二、项目依赖</a><br/>
<a href="#三整合Kafka">三、整合Kafka</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-ConsumerRecord">3.1 ConsumerRecord</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-生产者属性">3.2 生产者属性</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-位置策略">3.3 位置策略</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-订阅方式">3.4 订阅方式</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#35-提交偏移量">3.5 提交偏移量</a><br/>
<a href="#四启动测试">四、启动测试</a><br/>
</nav>


## 一、版本说明

Spark 针对 Kafka 的不同版本，提供了两套整合方案：`spark-streaming-kafka-0-8` 和 `spark-streaming-kafka-0-10`，其主要区别如下：

|                                               | [spark-streaming-kafka-0-8](https://spark.apache.org/docs/latest/streaming-kafka-0-8-integration.html) | [spark-streaming-kafka-0-10](https://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html) |
| :-------------------------------------------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| Kafka 版本                                     | 0.8.2.1 or higher                                            | 0.10.0 or higher                                             |
| AP 状态                                        | Deprecated<br/>从 Spark 2.3.0 版本开始，Kafka 0.8 支持已被弃用  | Stable(稳定版)                                               |
| 语言支持                                      | Scala, Java, Python                                          | Scala, Java                                                  |
| Receiver DStream                              | Yes                                                          | No                                                           |
| Direct DStream                                | Yes                                                          | Yes                                                          |
| SSL / TLS Support                             | No                                                           | Yes                                                          |
| Offset Commit API(偏移量提交)                 | No                                                           | Yes                                                          |
| Dynamic Topic Subscription<br/>(动态主题订阅) | No                                                           | Yes                                                          |

本文使用的 Kafka 版本为 `kafka_2.12-2.2.0`，故采用第二种方式进行整合。

## 二、项目依赖

项目采用 Maven 进行构建，主要依赖如下：

```xml
<properties>
    <scala.version>2.12</scala.version>
</properties>

<dependencies>
    <!-- Spark Streaming-->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-streaming_${scala.version}</artifactId>
        <version>${spark.version}</version>
    </dependency>
    <!-- Spark Streaming 整合 Kafka 依赖-->
    <dependency>
        <groupId>org.apache.spark</groupId>
        <artifactId>spark-streaming-kafka-0-10_${scala.version}</artifactId>
        <version>2.4.3</version>
    </dependency>
</dependencies>
```

> 完整源码见本仓库：[spark-streaming-kafka](https://github.com/heibaiying/BigData-Notes/tree/master/code/spark/spark-streaming-kafka)

## 三、整合Kafka

通过调用 `KafkaUtils` 对象的 `createDirectStream` 方法来创建输入流，完整代码如下：

```scala
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * spark streaming 整合 kafka
  */
object KafkaDirectStream {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("KafkaDirectStream").setMaster("local[2]")
    val streamingContext = new StreamingContext(sparkConf, Seconds(5))

    val kafkaParams = Map[String, Object](
      /*
       * 指定 broker 的地址清单，清单里不需要包含所有的 broker 地址，生产者会从给定的 broker 里查找其他 broker 的信息。
       * 不过建议至少提供两个 broker 的信息作为容错。
       */
      "bootstrap.servers" -> "hadoop001:9092",
      /*键的序列化器*/
      "key.deserializer" -> classOf[StringDeserializer],
      /*值的序列化器*/
      "value.deserializer" -> classOf[StringDeserializer],
      /*消费者所在分组的 ID*/
      "group.id" -> "spark-streaming-group",
      /*
       * 该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理:
       * latest: 在偏移量无效的情况下，消费者将从最新的记录开始读取数据（在消费者启动之后生成的记录）
       * earliest: 在偏移量无效的情况下，消费者将从起始位置读取分区的记录
       */
      "auto.offset.reset" -> "latest",
      /*是否自动提交*/
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )
    
    /*可以同时订阅多个主题*/
    val topics = Array("spark-streaming-topic")
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      /*位置策略*/
      PreferConsistent,
      /*订阅主题*/
      Subscribe[String, String](topics, kafkaParams)
    )

    /*打印输入流*/
    stream.map(record => (record.key, record.value)).print()

    streamingContext.start()
    streamingContext.awaitTermination()
  }
}
```

### 3.1 ConsumerRecord

这里获得的输入流中每一个 Record 实际上是 `ConsumerRecord<K, V> ` 的实例，其包含了 Record 的所有可用信息，源码如下：

```scala
public class ConsumerRecord<K, V> {
    
    public static final long NO_TIMESTAMP = RecordBatch.NO_TIMESTAMP;
    public static final int NULL_SIZE = -1;
    public static final int NULL_CHECKSUM = -1;
    
    /*主题名称*/
    private final String topic;
    /*分区编号*/
    private final int partition;
    /*偏移量*/
    private final long offset;
    /*时间戳*/
    private final long timestamp;
    /*时间戳代表的含义*/
    private final TimestampType timestampType;
    /*键序列化器*/
    private final int serializedKeySize;
    /*值序列化器*/
    private final int serializedValueSize;
    /*值序列化器*/
    private final Headers headers;
    /*键*/
    private final K key;
    /*值*/
    private final V value;
    .....   
}
```

### 3.2 生产者属性

在示例代码中 `kafkaParams` 封装了 Kafka 消费者的属性，这些属性和 Spark Streaming 无关，是 Kafka 原生 API 中就有定义的。其中服务器地址、键序列化器和值序列化器是必选的，其他配置是可选的。其余可选的配置项如下：

#### 1. fetch.min.byte

消费者从服务器获取记录的最小字节数。如果可用的数据量小于设置值，broker 会等待有足够的可用数据时才会把它返回给消费者。

#### 2. fetch.max.wait.ms

broker 返回给消费者数据的等待时间。

#### 3. max.partition.fetch.bytes

分区返回给消费者的最大字节数。

#### 4. session.timeout.ms

消费者在被认为死亡之前可以与服务器断开连接的时间。

#### 5. auto.offset.reset

该属性指定了消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理：

- latest(默认值) ：在偏移量无效的情况下，消费者将从其启动之后生成的最新的记录开始读取数据；
- earliest ：在偏移量无效的情况下，消费者将从起始位置读取分区的记录。

#### 6. enable.auto.commit

是否自动提交偏移量，默认值是 true,为了避免出现重复数据和数据丢失，可以把它设置为 false。

#### 7. client.id

客户端 id，服务器用来识别消息的来源。

#### 8. max.poll.records

单次调用 `poll()` 方法能够返回的记录数量。

#### 9. receive.buffer.bytes 和 send.buffer.byte

这两个参数分别指定 TCP socket 接收和发送数据包缓冲区的大小，-1 代表使用操作系统的默认值。



### 3.3 位置策略

Spark Streaming 中提供了如下三种位置策略，用于指定 Kafka 主题分区与 Spark 执行程序 Executors 之间的分配关系：

+ **PreferConsistent** : 它将在所有的 Executors 上均匀分配分区；

+ **PreferBrokers** : 当 Spark 的 Executor 与 Kafka Broker 在同一机器上时可以选择该选项，它优先将该 Broker 上的首领分区分配给该机器上的 Executor；
+ **PreferFixed** : 可以指定主题分区与特定主机的映射关系，显示地将分区分配到特定的主机，其构造器如下：

```scala
@Experimental
def PreferFixed(hostMap: collection.Map[TopicPartition, String]): LocationStrategy =
  new PreferFixed(new ju.HashMap[TopicPartition, String](hostMap.asJava))

@Experimental
def PreferFixed(hostMap: ju.Map[TopicPartition, String]): LocationStrategy =
  new PreferFixed(hostMap)
```



### 3.4 订阅方式

Spark Streaming 提供了两种主题订阅方式，分别为 `Subscribe` 和 `SubscribePattern`。后者可以使用正则匹配订阅主题的名称。其构造器分别如下：

```scala
/**
  * @param 需要订阅的主题的集合
  * @param Kafka 消费者参数
  * @param offsets(可选): 在初始启动时开始的偏移量。如果没有，则将使用保存的偏移量或 auto.offset.reset 属性的值
  */
def Subscribe[K, V](
    topics: ju.Collection[jl.String],
    kafkaParams: ju.Map[String, Object],
    offsets: ju.Map[TopicPartition, jl.Long]): ConsumerStrategy[K, V] = { ... }

/**
  * @param 需要订阅的正则
  * @param Kafka 消费者参数
  * @param offsets(可选): 在初始启动时开始的偏移量。如果没有，则将使用保存的偏移量或 auto.offset.reset 属性的值
  */
def SubscribePattern[K, V](
    pattern: ju.regex.Pattern,
    kafkaParams: collection.Map[String, Object],
    offsets: collection.Map[TopicPartition, Long]): ConsumerStrategy[K, V] = { ... }
```

在示例代码中，我们实际上并没有指定第三个参数 `offsets`，所以程序默认采用的是配置的 `auto.offset.reset` 属性的值 latest，即在偏移量无效的情况下，消费者将从其启动之后生成的最新的记录开始读取数据。

### 3.5 提交偏移量

在示例代码中，我们将 `enable.auto.commit` 设置为 true，代表自动提交。在某些情况下，你可能需要更高的可靠性，如在业务完全处理完成后再提交偏移量，这时候可以使用手动提交。想要进行手动提交，需要调用 Kafka 原生的 API :

+ `commitSync`:  用于异步提交；
+ `commitAsync`：用于同步提交。

具体提交方式可以参见：[Kafka 消费者详解](https://github.com/heibaiying/BigData-Notes/blob/master/notes/Kafka 消费者详解.md)



## 四、启动测试

### 4.1 创建主题

#### 1. 启动Kakfa

Kafka 的运行依赖于 zookeeper，需要预先启动，可以启动 Kafka 内置的 zookeeper，也可以启动自己安装的：

```shell
# zookeeper启动命令
bin/zkServer.sh start

# 内置zookeeper启动命令
bin/zookeeper-server-start.sh config/zookeeper.properties
```

启动单节点 kafka 用于测试：

```shell
# bin/kafka-server-start.sh config/server.properties
```

#### 2. 创建topic

```shell
# 创建用于测试主题
bin/kafka-topics.sh --create \
                    --bootstrap-server hadoop001:9092 \
                    --replication-factor 1 \
                    --partitions 1  \
                    --topic spark-streaming-topic

# 查看所有主题
 bin/kafka-topics.sh --list --bootstrap-server hadoop001:9092
```

#### 3. 创建生产者

这里创建一个 Kafka 生产者，用于发送测试数据：

```shell
bin/kafka-console-producer.sh --broker-list hadoop001:9092 --topic spark-streaming-topic
```

### 4.2 本地模式测试

这里我直接使用本地模式启动 Spark Streaming 程序。启动后使用生产者发送数据，从控制台查看结果。

从控制台输出中可以看到数据流已经被成功接收，由于采用 `kafka-console-producer.sh` 发送的数据默认是没有 key 的，所以 key 值为 null。同时从输出中也可以看到在程序中指定的 `groupId` 和程序自动分配的 `clientId`。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/spark-straming-kafka-console.png"/> </div>





## 参考资料

1. https://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>