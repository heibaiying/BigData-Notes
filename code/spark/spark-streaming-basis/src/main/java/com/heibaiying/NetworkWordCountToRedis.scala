package com.heibaiying

import com.heibaiying.utils.JedisPoolUtil
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

/**
  * 词频统计
  */
object NetworkWordCountToRedis {


  def main(args: Array[String]) {

    /*指定时间间隔为5s*/
    val sparkConf = new SparkConf().setAppName("NetworkWordCountToRedis").setMaster("local[2]")
    val ssc = new StreamingContext(sparkConf, Seconds(5))

    /*创建文本输入流,并进行词频统计*/
    val lines = ssc.socketTextStream("hadoop001", 9999)
    val pairs: DStream[(String, Int)] = lines.flatMap(_.split(" ")).map(x => (x, 1)).reduceByKey(_ + _)

    pairs.foreachRDD { rdd =>
      rdd.foreachPartition { partitionOfRecords =>
        var jedis: Jedis = null
        try {
          jedis = JedisPoolUtil.getConnection
          partitionOfRecords.foreach(record => jedis.hincrBy("wordCount", record._1, record._2))
        } catch {
          case ex: Exception =>
            ex.printStackTrace()
        } finally {
          if (jedis != null) jedis.close()
        }
      }
    }

    /*启动服务*/
    ssc.start()
    /*等待服务结束*/
    ssc.awaitTermination()
  }
}
