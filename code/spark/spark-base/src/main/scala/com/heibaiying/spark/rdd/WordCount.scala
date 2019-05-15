package com.heibaiying.spark.rdd

import org.apache.spark.{SparkConf, SparkContext}


object WordCount extends App {

  val conf = new SparkConf().setAppName("sparkBase").setMaster("local[2]")
  val sc = new SparkContext(conf)
  val rdd = sc.textFile("input/wc.txt").flatMap(_.split(",")).map((_, 1)).reduceByKey(_ + _)
  rdd.foreach(println)
  rdd.saveAsTextFile("output/")

}