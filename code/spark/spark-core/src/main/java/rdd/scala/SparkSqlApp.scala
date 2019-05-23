package rdd.scala

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


object SparkSqlApp {

  // 测试方法
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("aggregations").master("local[2]").getOrCreate()

    val df = spark.read.json("/usr/file/json/emp.json")

    import spark.implicits._

    df.select($"ename").limit(5).show()
    df.sort("sal").limit(3).show()

    df.orderBy(desc("sal")).limit(3).show()

    df.select("deptno").distinct().show()

    df.orderBy(desc("deptno"), asc("sal")).show(2)
  }
}
