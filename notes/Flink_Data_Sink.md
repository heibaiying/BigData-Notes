# Flink Sink

## 一、Data Sinks

在使用 Flink 进行数据处理时，数据经 Data Source 流入，然后通过系列 Transformations 的转化，最终可以通过 Sink 将计算结果进行输出，Flink Data Sinks 就是用于定义数据流最终的输出位置。Flink 提供了几个较为简单的 Sink API 用于日常的开发，具体如下：

### 1.1 writeAsText

`writeAsText` 用于将计算结果以文本的方式并行地写入到指定文件夹下，除了路径参数是必选外，该方法还可以通过指定第二个参数来定义输出模式，它有以下两个可选值：

+ **WriteMode.NO_OVERWRITE**：当指定路径上不存在任何文件时，才执行写出操作；
+ **WriteMode.OVERWRITE**：不论指定路径上是否存在文件，都执行写出操作；如果原来已有文件，则进行覆盖。

使用示例如下：

```java
 streamSource.writeAsText("D:\\out", FileSystem.WriteMode.OVERWRITE);
```

以上写出是以并行的方式写出到多个文件，如果想要将输出结果全部写出到一个文件，需要设置其并行度为 1：

```java
streamSource.writeAsText("D:\\out", FileSystem.WriteMode.OVERWRITE).setParallelism(1);
```

### 1.2 writeAsCsv

`writeAsCsv` 用于将计算结果以 CSV 的文件格式写出到指定目录，除了路径参数是必选外，该方法还支持传入输出模式，行分隔符，和字段分隔符三个额外的参数，其方法定义如下：

```java
writeAsCsv(String path, WriteMode writeMode, String rowDelimiter, String fieldDelimiter) 
```

### 1.3 print \ printToErr

`print \ printToErr` 是测试当中最常用的方式，用于将计算结果以标准输出流或错误输出流的方式打印到控制台上。

### 1.4 writeUsingOutputFormat

采用自定义的输出格式将计算结果写出，上面介绍的 `writeAsText` 和 `writeAsCsv` 其底层调用的都是该方法，源码如下：

```java
public DataStreamSink<T> writeAsText(String path, WriteMode writeMode) {
    TextOutputFormat<T> tof = new TextOutputFormat<>(new Path(path));
    tof.setWriteMode(writeMode);
    return writeUsingOutputFormat(tof);
}
```

### 1.5 writeToSocket

`writeToSocket` 用于将计算结果以指定的格式写出到 Socket 中，使用示例如下：

```shell
streamSource.writeToSocket("192.168.0.226", 9999, new SimpleStringSchema());
```

## 二、Streaming Connectors

除了上述 API 外，Flink 中还内置了系列的 Connectors 连接器，用于将计算结果输入到常用的存储系统或者消息中间件中，具体如下：

- Apache Kafka (支持 source 和 sink)
- Apache Cassandra (sink)
- Amazon Kinesis Streams (source/sink)
- Elasticsearch (sink)
- Hadoop FileSystem (sink)
- RabbitMQ (source/sink)
- Apache NiFi (source/sink)
- Google PubSub (source/sink)

除了内置的连接器外，你还可以通过 Apache Bahir 的连接器扩展 Flink。Apache Bahir 旨在为分布式数据分析系统 (如 Spark，Flink) 等提供功能上的扩展，当前其支持的与 Flink Sink 相关的连接器如下：

- Apache ActiveMQ (source/sink)
- Apache Flume (sink)
- Redis (sink)
- Akka (sink)

这里接着在 Data Sources 章节介绍的整合 Kafka Source 的基础上，将 Kafka Sink 也一并进行整合，具体步骤如下。

## 三、整合 Kafka Sink

### 3.1 addSink

### 3.2 创建输出主题

```shell
# 创建用于测试的输出主题
bin/kafka-topics.sh --create \
                    --bootstrap-server hadoop001:9092 \
                    --replication-factor 1 \
                    --partitions 1  \
                    --topic flink-stream-out-topic

# 查看所有主题
 bin/kafka-topics.sh --list --bootstrap-server hadoop001:9092
```

### 3.3 启动消费者

```java
bin/kafka-console-consumer.sh --bootstrap-server hadoop001:9092 --topic flink-stream-out-topic
```

### 3.4 测试结果



## 四、自定义 Sink

### 4.1 导入依赖

### 4.2 自定义 Sink

### 4.3 测试结果





