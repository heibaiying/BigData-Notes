package rdd.scala

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


object SparkSqlApp {

  // 测试方法
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("aggregations").master("local[2]").getOrCreate()
    val empDF = spark.read.json("/usr/file/json/emp.json")
    empDF.createOrReplaceTempView("emp")
    empDF.show()

    empDF.select(count("ename")).show()
    empDF.select(countDistinct("deptno")).show()
    empDF.select(approx_count_distinct("ename", 0.1)).show()
    empDF.select(first("ename"), last("job")).show()
    empDF.select(min("sal"), max("sal")).show()
    empDF.select(sum("sal")).show()
    empDF.select(sumDistinct("sal")).show()
    empDF.select(avg("sal")).show()


    // 总体方差 均方差 总体标准差 样本标准差
    empDF.select(var_pop("sal"), var_samp("sal"), stddev_pop("sal"), stddev_samp("sal")).show()


    // 偏度和峰度
    empDF.select(skewness("sal"), kurtosis("sal")).show()

    // 计算两列的 皮尔逊相关系数 样本协方差 总体协方差
    empDF.select(corr("empno", "sal"), covar_samp("empno", "sal"),
      covar_pop("empno", "sal")).show()

    empDF.agg(collect_set("job"), collect_list("ename")).show()


    empDF.groupBy("deptno", "job").count().show()
    spark.sql("SELECT deptno, job, count(*) FROM emp GROUP BY deptno, job").show()

    empDF.groupBy("deptno").agg(count("ename").alias("人数"), sum("sal").alias("总工资")).show()
    spark.sql("SELECT deptno, count(ename) ,sum(sal) FROM emp GROUP BY deptno").show()


    empDF.groupBy("deptno").agg("ename"->"count","sal"->"sum").show()




  }
}
