package com.heibaiying.kafka

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
       * 指定broker的地址清单，清单里不需要包含所有的broker地址，生产者会从给定的broker里查找其他broker的信息。
       * 不过建议至少提供两个broker的信息作为容错。
       */
      "bootstrap.servers" -> "hadoop001:9092",
      /*键的序列化器*/
      "key.deserializer" -> classOf[StringDeserializer],
      /*值的序列化器*/
      "value.deserializer" -> classOf[StringDeserializer],
      /*消费者所在分组的ID*/
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

    val topics = Array("spark-streaming-topic")
    val stream = KafkaUtils.createDirectStream[String, String](
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )

    /*打印输入流*/
    stream.map(record => (record.key, record.value)).print()

    streamingContext.start()
    streamingContext.awaitTermination()
  }

}
