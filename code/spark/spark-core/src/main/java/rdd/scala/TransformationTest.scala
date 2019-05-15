package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{After, Test}

class TransformationTest {

  val conf: SparkConf = new SparkConf().setAppName("TransformationTest").setMaster("local[2]")
  val sc = new SparkContext(conf)


  @Test
  def map(): Unit = {
    val list = List(3, 6, 9, 10, 12, 21)
    sc.parallelize(list).map(_ * 10).foreach(println)
  }

  @After
  def destroy(): Unit = {
    sc.stop()
  }


}