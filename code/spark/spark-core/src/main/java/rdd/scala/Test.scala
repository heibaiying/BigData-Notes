package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}

object Test extends App {


  val conf = new SparkConf().setAppName("TransformationTest123").setMaster("local[2]")
  val sc = new SparkContext(conf)

  val list = List(3, 6, 9, 10, 12, 21)
  sc.parallelize(list).map(_ * 10).foreach(println)

}
