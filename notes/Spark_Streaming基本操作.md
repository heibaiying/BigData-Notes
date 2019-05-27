# Spark Streaming 基本操作

## 一、案例引入

这里先引入一个基本的案例来演示流的创建：监听指定端口9999上的数据并进行词频统计。项目依赖和代码实现如下：

```xml
<dependency>
    <groupId>org.apache.spark</groupId>
    <artifactId>spark-streaming_2.12</artifactId>
    <version>2.4.3</version>
</dependency>
```

```scala
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

object NetworkWordCount {

  def main(args: Array[String]) {

    /*指定时间间隔为5s*/
    val sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    /*创建文本输入流,并进行词频统计*/
    val lines = ssc.socketTextStream("hadoop001", 9999)
    lines.flatMap(_.split(" ")).map(x => (x, 1)).reduceByKey(_ + _).print()

    /*启动服务*/
    ssc.start()
    /*等待服务结束*/
    ssc.awaitTermination()
  }
}
```

二、常用算子

使用本地模式启动Spark程序，然后使用`nc -lk 9999`打开端口并输入测试数据：

```shell
[root@hadoop001 ~]#  nc -lk 9999
hello world hello spark hive hive hadoop
storm storm flink azkaban
```

此时IDEA中控制台输出如下，可以看到已经接收到每一行数据并且进行了词频统计。

![spark-streaming-word-count-v1](D:\BigData-Notes\pictures\spark-streaming-word-count-v1.png)

下面我们针对示例代码进行讲解：

### 3.1 StreamingContext

Spark Streaming编程的入口类是StreamingContext，在创建时候需要指明`sparkConf`和`batchDuration`(批次时间)，Spark流处理本质是将流数据拆分为一个个批次，然后进行微批处理，batchDuration就是用于指定流数据将被分成批次的时间间隔。这个时间可以根据业务需求和服务器性能进行指定，如果业务要求低延迟，则这个时间可以指定得很短。

这里需要注意的是：示例代码使用的是本地模式，配置为`local[2]`，这里不能配置为`local[1]`。这是因为对于流数据的处理，Spark必须有一个独立的Executor来接收数据，然后由其他的Executors来处理数据，所以为了保证数据能够被处理，至少要有2个Executors。这里我们的程序只有一个数据流，在并行读取多个数据流的时候，也要注意必须保证有足够的Executors来接收和处理数据。

### 3.2 数据源

在示例代码中使用的是`socketTextStream`来创建基于socket的数据流，实际上Spark还支持多种数据源，分为以下两类：

+ 基本数据源：文件系统、socket的连接；
+ 高级数据源：Kafka，Flume，Kinesis。

在基本数据源中，Spark支持对HDFS上指定目录进行监听，当有新文件加入时，会获取其文件内容作为输入流。创建方式如下：

```scala
// 对于文本文件，指明监听目录即可
streamingContext.textFileStream(dataDirectory)
// 对于其他文件，需要指明目录，以及键的类型、值的类型、和输入格式
streamingContext.fileStream[KeyClass, ValueClass, InputFormatClass](dataDirectory)
```

指定的目录时，可以是具体的目录，如`hdfs://namenode:8040/logs/`；也可以使用通配符，如`hdfs://namenode:8040/logs/2017/*`。

> 关于高级数据源的整合单独整理至：[Spark Streaming 整合 Flume](https://github.com/heibaiying/BigData-Notes/blob/master/notes/Spark_Streaming整合Flume.md) 和 [Spark Streaming 整合 Kafka](https://github.com/heibaiying/BigData-Notes/blob/master/notes/Spark_Streaming整合Kafka.md)

### 3.3 服务的启动与停止

在示例代码中，使用`streamingContext.start()`代表启动服务，此时还要使`streamingContext.awaitTermination()`使服务处于等待和可用的状态，直到发生异常或者手动使用`streamingContext.stop()`进行终止。



## 二、updateStateByKey

### 2.1 DStream与RDDs

DStream是Spark Streaming提供的基本抽象。它表示连续的数据流。在内部，DStream由一系列连续的RDD表示。所以从本质上而言，应用于DStream的任何操作都会转换为底层RDD上的操作。例如，在示例代码中的flatMap算子操作实际上是作用在每个RDDs上(如下图)。因为这个原因，所以Spark Streaming能够支持RDD大多数的transformation算子。

![spark-streaming-dstream-ops](D:\BigData-Notes\pictures\spark-streaming-dstream-ops.png)

### 2.2 updateStateByKey

除了支持RDD上大多数的transformation算子外，DStream还有部分独有的算子，这当中比较重要的是`updateStateByKey`。文章开头的词频统计程序，只能统计每一次输入文本中单词出现的数量，想要统计所有历史输入中单词出现的数量，就需要依赖`updateStateByKey`算子。代码如下：

```scala
object NetworkWordCountV2 {


  def main(args: Array[String]) {

    /*
     * 本地测试时最好指定hadoop用户名,否则会默认使用本地电脑的用户名,
     * 此时在HDFS上创建目录时可能会抛出权限不足的异常
     */
    System.setProperty("HADOOP_USER_NAME", "root")
      
    val sparkConf = new SparkConf().setAppName("NetworkWordCountV2").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))
    /*必须要设置检查点*/
    ssc.checkpoint("hdfs://hadoop001:8020/spark-streaming")
    val lines = ssc.socketTextStream("hadoop001", 9999)
    lines.flatMap(_.split(" ")).map(x => (x, 1))
      .updateStateByKey[Int](updateFunction _)   //updateStateByKey算子
      .print()

    ssc.start()
    ssc.awaitTermination()
  }

  /**
    * 累计求和
    *
    * @param currentValues 当前的数据
    * @param preValues     之前的数据
    * @return 相加后的数据
    */
  def updateFunction(currentValues: Seq[Int], preValues: Option[Int]): Option[Int] = {
    val current = currentValues.sum
    val pre = preValues.getOrElse(0)
    Some(current + pre)
  }
}
```

使用`updateStateByKey`算子，你必须使用` ssc.checkpoint()`设置检查点，这样当使用`updateStateByKey`算子时，它会去检查点中取出上一次保存的信息，并使用自定义的`updateFunction`函数将上一次的数据和本次数据进行相加，然后返回。

### 2.3 启动测试

```shell
[root@hadoop001 ~]#  nc -lk 9999
hello world hello spark hive hive hadoop
storm storm flink azkaban
hello world hello spark hive hive hadoop
storm storm flink azkaban
```

控制台输出如下，可以看到每一次输入的结果都被进行了累计求值。

![spark-streaming-word-count-v2](D:\BigData-Notes\pictures\spark-streaming-word-count-v2.png)

同时查看在输出日志中还可以看到检查点操作的相关信息：

```shell
# 保存检查点信息
19/05/27 16:21:05 INFO CheckpointWriter: Saving checkpoint for time 1558945265000 ms 
to file 'hdfs://hadoop001:8020/spark-streaming/checkpoint-1558945265000'

# 删除已经无用的检查点信息
19/05/27 16:21:30 INFO CheckpointWriter: 
Deleting hdfs://hadoop001:8020/spark-streaming/checkpoint-1558945265000
```

## 三、输出操作

| Output Operation                            | Meaning                                                      |
| :------------------------------------------ | :----------------------------------------------------------- |
| **print**()                                 | 在运行流应用程序的driver节点上打印DStream中每个批次的前十个元素。用于开发调试。 |
| **saveAsTextFiles**(*prefix*, [*suffix*])   | 将DStream的内容保存为文本文件。每个批处理间隔的文件名基于前缀和后缀生成：“prefix-TIME_IN_MS [.suffix]”。 |
| **saveAsObjectFiles**(*prefix*, [*suffix*]) | 将此DStream的内容序列化为Java对象，并保存到SequenceFiles。每个批处理间隔的文件名基于前缀和后缀生成：“prefix-TIME_IN_MS [.suffix]”。 |
| **saveAsHadoopFiles**(*prefix*, [*suffix*]) | 将此DStream的内容保存为Hadoop文件。每个批处理间隔的文件名基于前缀和后缀生成：“prefix-TIME_IN_MS [.suffix]”。 |
| **foreachRDD**(*func*)                      | 最通用的输出方式，它将函数func应用于从流生成的每个RDD。此函数应将每个RDD中的数据推送到外部系统，例如将RDD保存到文件，或通过网络将其写入数据库。 |







