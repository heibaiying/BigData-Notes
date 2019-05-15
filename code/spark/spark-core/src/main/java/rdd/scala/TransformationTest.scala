package rdd.scala

import org.apache.spark.{SparkConf, SparkContext}
import org.junit.{After, Test}

import scala.collection.mutable.ListBuffer

class TransformationTest {

  val conf: SparkConf = new SparkConf().setAppName("TransformationTest").setMaster("local[2]")
  val sc = new SparkContext(conf)


  @Test
  def map(): Unit = {
    val list = List(1, 2, 3)
    sc.parallelize(list).map(_ * 10).foreach(println)
  }


  @Test
  def filter(): Unit = {
    val list = List(3, 6, 9, 10, 12, 21)
    sc.parallelize(list).filter(_ >= 10).foreach(println)
  }


  @Test
  def flatMap(): Unit = {
    val list = List(List(1, 2), List(3), List(), List(4, 5))
    sc.parallelize(list).flatMap(_.toList).map(_ * 10).foreach(println)

    val lines = List("spark flume spark",
      "hadoop flume hive")
    sc.parallelize(lines).flatMap(line => line.split(" ")).
      map(word => (word, 1)).reduceByKey(_ + _).foreach(println)

  }


  @Test
  def mapPartitions(): Unit = {
    val list = List(1, 2, 3, 4, 5, 6)
    sc.parallelize(list, 3).mapPartitions(iterator => {
      val buffer = new ListBuffer[Int]
      while (iterator.hasNext) {
        buffer.append(iterator.next() * 100)
      }
      buffer.toIterator
    }).foreach(println)
  }


  @Test
  def mapPartitionsWithIndex(): Unit = {
    val list = List(1, 2, 3, 4, 5, 6)
    sc.parallelize(list, 3).mapPartitionsWithIndex((index, iterator) => {
      val buffer = new ListBuffer[String]
      while (iterator.hasNext) {
        buffer.append(index + "分区:" + iterator.next() * 100)
      }
      buffer.toIterator
    }).foreach(println)
  }


  @Test
  def sample(): Unit = {
    val list = List(1, 2, 3, 4, 5, 6)
    sc.parallelize(list).sample(withReplacement = false, 0.5).foreach(println)
  }


  @Test
  def union(): Unit = {
    val list1 = List(1, 2, 3)
    val list2 = List(4, 5, 6)
    sc.parallelize(list1).union(sc.parallelize(list2)).foreach(println)
  }


  @Test
  def intersection(): Unit = {
    val list1 = List(1, 2, 3, 4, 5)
    val list2 = List(4, 5, 6)
    sc.parallelize(list1).intersection(sc.parallelize(list2)).foreach(println)
  }

  @Test
  def distinct(): Unit = {
    val list = List(1, 2, 2, 4, 4)
    sc.parallelize(list).distinct().foreach(println)
  }


  @Test
  def groupByKey(): Unit = {
    val list = List(("hadoop", 2), ("spark", 3), ("spark", 5), ("storm", 6), ("hadoop", 2))
    sc.parallelize(list).groupByKey().map(x => (x._1, x._2.toList)).foreach(println)
  }


  @Test
  def reduceByKey(): Unit = {
    val list = List(("hadoop", 2), ("spark", 3), ("spark", 5), ("storm", 6), ("hadoop", 2))
    sc.parallelize(list).reduceByKey(_ + _).foreach(println)
  }

  @Test
  def aggregateByKey(): Unit = {
    val list = List(("hadoop", 3), ("hadoop", 2), ("spark", 4), ("spark", 3), ("storm", 6), ("storm", 8))
    sc.parallelize(list, numSlices = 6).aggregateByKey(zeroValue = 0, numPartitions = 5)(
      seqOp = math.max(_, _),
      combOp = _ + _
    ).getNumPartitions
  }


  @Test
  def sortBy(): Unit = {
    val list01 = List((100, "hadoop"), (90, "spark"), (120, "storm"))
    sc.parallelize(list01).sortByKey(ascending = false).foreach(println)

    val list02 = List(("hadoop", 100), ("spark", 90), ("storm", 120))
    sc.parallelize(list02).sortBy(x => x._2, ascending = false).foreach(println)
  }


  @Test
  def join(): Unit = {
    val list01 = List((1, "student01"), (2, "student02"), (3, "student03"))
    val list02 = List((1, "teacher01"), (2, "teacher02"), (3, "teacher03"))
    sc.parallelize(list01).join(sc.parallelize(list02)).foreach(println)
  }


  @Test
  def cogroup(): Unit = {
    val list01 = List((1, "a"), (1, "a"), (2, "b"), (3, "e"))
    val list02 = List((1, "A"), (2, "B"), (3, "E"))
    val list03 = List((1, "[ab]"), (2, "[bB]"), (3, "eE"), (3, "eE"))
    sc.parallelize(list01).cogroup(sc.parallelize(list02), sc.parallelize(list03)).foreach(println)
  }


  @Test
  def cartesian(): Unit = {
    val list1 = List("A", "B", "C")
    val list2 = List(1, 2, 3)
    sc.parallelize(list1).cartesian(sc.parallelize(list2)).foreach(println)
  }


  @Test
  def reduce(): Unit = {
    val list = List(1, 2, 3, 4, 5)
    sc.parallelize(list).reduce((x, y) => x + y)
    sc.parallelize(list).reduce(_ + _)
  }

  // 继承Ordering[T],实现自定义比较器
  class CustomOrdering extends Ordering[(Int, String)] {
    override def compare(x: (Int, String), y: (Int, String)): Int
    = if (x._2.length > y._2.length) 1 else -1
  }

  @Test
  def takeOrdered(): Unit = {
    val list = List((1, "hadoop"), (1, "storm"), (1, "azkaban"), (1, "hive"))
    //  定义隐式默认值
    implicit val implicitOrdering = new CustomOrdering
    sc.parallelize(list).takeOrdered(5)
  }


  @Test
  def countByKey(): Unit = {
    val list = List(("hadoop", 10), ("hadoop", 10), ("storm", 3), ("storm", 3), ("azkaban", 1))
    sc.parallelize(list).countByKey()
  }

  @Test
  def saveAsTextFile(): Unit = {
    val list = List(("hadoop", 10), ("hadoop", 10), ("storm", 3), ("storm", 3), ("azkaban", 1))
    sc.parallelize(list).saveAsTextFile("/usr/file/temp")
  }

  @Test
  def saveAsSequenceFile(): Unit = {
    val list = List(("hadoop", 10), ("hadoop", 10), ("storm", 3), ("storm", 3), ("azkaban", 1))
    sc.parallelize(list).saveAsSequenceFile("/usr/file/sequence")
  }


  @After
  def destroy(): Unit = {
    sc.stop()
  }


}