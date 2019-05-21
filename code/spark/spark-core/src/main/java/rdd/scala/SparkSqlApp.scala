package rdd.scala

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._


object SparkSqlApp {

  // 测试方法
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("aggregations").master("local[2]").getOrCreate()

    val empDF = spark.read.json("/usr/file/json/emp.json")
    empDF.createOrReplaceTempView("emp")

    val deptDF = spark.read.json("/usr/file/json/dept.json")
    deptDF.createOrReplaceTempView("dept")

    deptDF.printSchema()

    // 1.定义联结表达式
    val joinExpression = empDF.col("deptno") === deptDF.col("deptno")
    // 2.联结查询
    empDF.join(deptDF, joinExpression).select("ename", "dname").show()
    spark.sql("SELECT ename,dname FROM emp JOIN dept ON emp.deptno = dept.deptno").show()


    empDF.join(deptDF, joinExpression, "outer").show()
    spark.sql("SELECT * FROM emp FULL OUTER JOIN dept ON emp.deptno = dept.deptno").show()

    empDF.join(deptDF, joinExpression, "left_outer").show()
    spark.sql("SELECT * FROM emp LEFT OUTER JOIN dept ON emp.deptno = dept.deptno").show()

    empDF.join(deptDF, joinExpression, "right_outer").show()
    spark.sql("SELECT * FROM emp RIGHT OUTER JOIN dept ON emp.deptno = dept.deptno").show()

    empDF.join(deptDF, joinExpression, "left_semi").show()
    spark.sql("SELECT * FROM emp LEFT SEMI JOIN dept ON emp.deptno = dept.deptno").show()

    empDF.join(deptDF, joinExpression, "left_anti").show()
    spark.sql("SELECT * FROM emp LEFT ANTI dept ON emp.deptno = dept.deptno").show()

    /*你绝对应该使用交叉连接，100％确定这是你需要的。 在Spark中定义交叉连接时，有一个原因需要明确。 他们很危险！
    高级用户可以将会话级配置spark.sql.crossJoin.enable设置为true，以便允许交叉连接而不发出警告，或者Spark没有尝试为您执行另一个连接。*/
    empDF.join(deptDF, joinExpression, "cross").show()
    spark.sql("SELECT * FROM emp CROSS JOIN dept ON emp.deptno = dept.deptno").show()






    spark.sql("SELECT * FROM graduateProgram NATURAL JOIN person").show()
  }
}
