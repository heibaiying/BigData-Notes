package com.heibaiying.flume

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.flume.FlumeUtils


/**
  * @author : heibaiying
  * 基于推的方法获取数据
  */
object PushBasedWordCount {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf()
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    // 1.获取输入流
    val flumeStream = FlumeUtils.createStream(ssc, "hadoop001", 8888)

    // 2.打印输入流的数据
    flumeStream.map(line => new String(line.event.getBody.array()).trim).print()

    ssc.start()
    ssc.awaitTermination()
  }
}
