package rdd.scala

import org.apache.spark.sql.{Dataset, SparkSession}


object SparkSqlTest extends App {



  val spark = SparkSession.builder().appName("Spark SQL basic example").config("spark.some.config.option", "some-value").getOrCreate()

  val dataFrames = spark.read.json("/usr/file/people.json")

  df.select("name").show()

  df.printSchema()




  import spark.implicits._

  val primitiveDS = Seq(1, 2, 3).toDS()
  primitiveDS.printSchema()
  primitiveDS.map(_ + 1).collect()

  peopleDS.select("name").show()  //失败
  peopleDS.dtypes
  peopleDS.printSchema()
  peopleDS.toDF()
  // Encoders are created for case classes

  /* 1.此时把selected写成为selected ,编译器没有任何提示 */
  spark.sql("selected name from emp")

  /* 2.此时把selected写成为selected ,编译器有提示; 但是把字段名称name写成了nameEd ,编译器没有任何提示*/
  val dataFrames = spark.read.json("people.json")
  dataFrames.selected("nameEd").show()
  dataFrames.map(line=>line.name)

  case class Person(name: String, age: Long)

  /* 3.此时最为严格,语法和字段名称错误都被检测出来*/
  val dataSet: Dataset[Person] = spark.read.json("people.json").as[Person]
  dataSet.selected("name")
  dataSet.map(line=>line.name)
  dataSet.map(line=>line.nameEd)

  /* 4.即使在由RDD转换为dataFrame时候指定了类型Person,依然无法提示字段名称*/
  val peopleDF = spark.sparkContext
    .textFile("people.json")
    .map(_.split(","))
    .map(attributes => Person(attributes(0), attributes(1).trim.toInt))
    .toDF()
  peopleDF.map(line=>line.name)

}
