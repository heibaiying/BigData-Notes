package com.heibaiying

import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 词频统计
  */
object NetworkWordCountV2 {


  def main(args: Array[String]) {

    /*
     * 本地测试时最好指定hadoop用户名,否则会默认使用本地电脑的用户名,
     * 此时在HDFS上创建目录时可能会抛出权限不足的异常
     */
    System.setProperty("HADOOP_USER_NAME", "root")

    /*指定时间间隔为5s*/
    val sparkConf = new SparkConf().setAppName("NetworkWordCount").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    /*必须要设置检查点*/
    ssc.checkpoint("hdfs://192.168.200.229:8020/spark-streaming")

    /*创建文本输入流,并进行词频统计*/
    val lines = ssc.socketTextStream("192.168.200.229", 9999)
    lines.flatMap(_.split(" ")).map(x => (x, 1))
      .updateStateByKey((values: Seq[Int], state: Option[Int]) => {
        val currentCount: Int = values.sum
        val lastCount: Int = state.getOrElse(0)
        Some(currentCount + lastCount)
      })
      .print()

    /*启动服务*/
    ssc.start()
    /*等待服务结束*/
    ssc.awaitTermination()

  }
}
