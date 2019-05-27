package com.heibaiying

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 词频统计
  */
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
