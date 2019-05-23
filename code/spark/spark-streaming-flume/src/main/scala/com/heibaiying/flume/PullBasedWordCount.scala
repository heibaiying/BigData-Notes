package com.heibaiying.flume

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.flume.FlumeUtils

/**
  * @author : heibaiying
  * 使用自定义接收器的基于拉的方法获取数据
  */
object PullBasedWordCount {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // 1.获取输入流
    val flumeStream = FlumeUtils.createPollingStream(ssc, "hadoop001", 8888)

    // 2.打印输入流中的数据
    flumeStream.map(line => new String(line.event.getBody.array()).trim).print()

    ssc.start()
    ssc.awaitTermination()
  }

}
