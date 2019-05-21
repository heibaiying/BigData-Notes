package rdd.scala

import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, Encoders, SparkSession, functions}

// 1.定义员工类,对于可能存在null值的字段需要使用Option进行包装
case class Emp(ename: String, comm: scala.Option[Double], deptno: Long, empno: Long,
               hiredate: String, job: String, mgr: scala.Option[Long], sal: Double)

// 2.定义聚合操作的中间输出类型
case class SumAndCount(var sum: Double, var count: Long)

/* 3.自定义聚合函数
 * @IN  聚合操作的输入类型
 * @BUF reduction操作输出值的类型
 * @OUT 聚合操作的输出类型
 */
object MyAverage extends Aggregator[Emp, SumAndCount, Double] {


  // 4.用于聚合操作的的初始零值
  override def zero: SumAndCount = SumAndCount(0, 0)


  // 5.同一分区中的reduce操作
  override def reduce(avg: SumAndCount, emp: Emp): SumAndCount = {
    avg.sum += emp.sal
    avg.count += 1
    avg
  }

  // 6.不同分区中的merge操作
  override def merge(avg1: SumAndCount, avg2: SumAndCount): SumAndCount = {
    avg1.sum += avg2.sum
    avg1.count += avg2.count
    avg1
  }

  // 7.定义最终的输出类型
  override def finish(reduction: SumAndCount): Double = reduction.sum / reduction.count

  // 8.中间类型的编码转换
  override def bufferEncoder: Encoder[SumAndCount] = Encoders.product

  // 9.输出类型的编码转换
  override def outputEncoder: Encoder[Double] = Encoders.scalaDouble
}

object SparkSqlApp {

  // 测试方法
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Spark-SQL").master("local[2]").getOrCreate()
    import spark.implicits._
    val ds = spark.read.json("file/emp.json").as[Emp]

    // 10.使用内置avg()函数和自定义函数分别进行计算，验证自定义函数是否正确
    val myAvg = ds.select(MyAverage.toColumn.name("average_sal")).first()
    val avg = ds.select(functions.avg(ds.col("sal"))).first().get(0)

    println("自定义average函数 : " + myAvg)
    println("内置的average函数 : " + avg)
  }
}
