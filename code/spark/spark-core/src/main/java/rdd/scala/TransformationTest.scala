package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{Before, Test}

class TransformationTest extends {

  var sc: SparkContext = _

  @Before
  def prepare(): Unit = {
    val conf = new SparkConf().setAppName("TransformationTest").setMaster("local[2]")
    sc = new SparkContext(conf)
  }

  @Test
  def map(): Unit = {
    val list = List(3, 6, 9, 10, 12, 21)
    sc.parallelize(list).map(_ * 10).foreach(println)
  }


}