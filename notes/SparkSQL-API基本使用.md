# SparkSQL API基本使用

## 一、创建DataFrames

Spark中所有功能的入口点是`SparkSession`，可以使用`SparkSession.builder()`创建。创建后应用程序就可以从现有RDD，Hive表或Spark数据源创建DataFrame。如下所示：

```scala
val spark = SparkSession.builder().appName("Spark-SQL").master("local[2]").getOrCreate()
val df = spark.read.json("/usr/file/emp.json")
df.show()

// 建议在进行spark SQL编程前导入下面的隐式转换，因为DataFrames和dataSets中很多操作都依赖了隐式转换
import spark.implicits._
```

这里可以启动`spark-shell`进行测试，需要注意的是`spark-shell`启动后会自动创建一个名为`spark`的`SparkSession`，在命令行中可以直接引用即可：

![spark-sql-shell](D:\BigData-Notes\pictures\spark-sql-shell.png)

## 二、DataFrames基本操作

### 2.1 printSchema

```scala
// 以树形结构打印dataframe的schema信息 
df.printSchema()
```

![spark-scheme](D:\BigData-Notes\pictures\spark-scheme.png)

### 2.2 使用DataFrame API进行基本查询

```scala
// 查询员工姓名及工作
df.select($"ename", $"job").show()

// 查询工资大于2000的员工信息
df.filter($"sal" > 2000).show()

// 分组统计部门人数
df.groupBy("deptno").count().show()
```

### 2.3 使用SQL进行基本查询

```scala
// 首先需要将DataFrame注册为临时视图
df.createOrReplaceTempView("emp")

// 查询员工姓名及工作
spark.sql("SELECT ename,job FROM emp").show()

// 查询工资大于2000的员工信息
spark.sql("SELECT * FROM emp where sal > 2000").show()

// 分组统计部门人数
spark.sql("SELECT deptno,count(ename) FROM emp group by deptno").show()
```

### 2.4 全局临时视图

上面使用`createOrReplaceTempView`创建的是会话临时视图，它的生命周期仅限于会话范围，会随会话的结束而结束。

你也可以使用`createGlobalTempView`创建全局临时视图，全局临时视图可以在所有会话之间共享，并直到整个Spark应用程序终止才会消失。全局临时视图被定义在内置的`global_temp`数据库下，需要使用限定名称进行引用，如`SELECT * FROM global_temp.view1`。

```scala
// 注册为全局临时视图
df.createGlobalTempView("gemp")

// 查询员工姓名及工作，使用限定名称进行引用
spark.sql("SELECT ename,job FROM global_temp.gemp").show()

// 查询工资大于2000的员工信息，使用限定名称进行引用
spark.sql("SELECT * FROM global_temp.gemp where sal > 2000").show()

// 分组统计部门人数，使用限定名称进行引用
spark.sql("SELECT deptno,count(ename) FROM global_temp.gemp  group by deptno").show()
```

## 三、创建Datasets

### 3.1 由外部数据集创建

```scala
// 1.需要导入隐式转换
import spark.implicits._

// 2.创建case class,等价于Java Bean
case class Emp(ename: String, comm: Double, deptno: Long, empno: Long, 
               hiredate: String, job: String, mgr: Long, sal: Double)

// 3.由外部数据集创建Datasets
val ds = spark.read.json("/usr/file/emp.json").as[Emp]
ds.show()
```

### 3.2 由内部数据集创建

```scala
// 1.需要导入隐式转换
import spark.implicits._

// 2.创建case class,等价于Java Bean
case class Emp(ename: String, comm: Double, deptno: Long, empno: Long, 
               hiredate: String, job: String, mgr: Long, sal: Double)

// 3.由内部数据集创建Datasets
val caseClassDS = Seq(Emp("ALLEN", 300.0, 30, 7499, "1981-02-20 00:00:00", "SALESMAN", 7698, 1600.0),
                      Emp("JONES", 300.0, 30, 7499, "1981-02-20 00:00:00", "SALESMAN", 7698, 1600.0))
                    .toDS()
caseClassDS.show()
```



## 四、DataFrames与Datasets互相转换

Spark提供了非常简单的转换方法用于DataFrames与Datasets互相转换，示例如下：

```shell
# DataFrames转Datasets
scala> df.as[Emp]
res1: org.apache.spark.sql.Dataset[Emp] = [COMM: double, DEPTNO: bigint ... 6 more fields]

# Datasets转DataFrames
scala> ds.toDF()
res2: org.apache.spark.sql.DataFrame = [COMM: double, DEPTNO: bigint ... 6 more fields]
```



## 五、RDDs转换为DataFrames\Datasets

Spark支持两种方式把RDD转换为DataFrames，分别是使用反射推断和指定schema转换。

### 5.1 使用反射推断

```scala
// 1.导入隐式转换
import spark.implicits._

// 2.创建部门类
case class Dept(deptno: Long, dname: String, loc: String)

// 3.创建RDD并转换为dataSet
val rddToDS = spark.sparkContext
  .textFile("/usr/file/dept.txt")
  .map(_.split("\t"))
  .map(line => Dept(line(0).trim.toLong, line(1), line(2)))
  .toDS()  // 如果调用toDF()则转换为dataFrame 
```

### 5.2 以编程方式指定Schema

```scala
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._


// 1.定义每个列的列类型
val fields = Array(StructField("deptno", LongType, nullable = true),
                   StructField("dname", StringType, nullable = true),
                   StructField("loc", StringType, nullable = true))

// 2.创建schema
val schema = StructType(fields)

// 3.创建RDD
val deptRDD = spark.sparkContext.textFile("/usr/file/dept.txt")
val rowRDD = deptRDD.map(_.split("\t")).map(line => Row(line(0).toLong, line(1), line(2)))


// 4.将RDD转换为dataFrame
val deptDF = spark.createDataFrame(rowRDD, schema)
deptDF.show()
```

## 六、使用自定义聚合函数

Scala提供了两种自定义聚合函数的方法，分别如下：

+ 有类型的自定义聚合函数，主要适用于DataSets；
+ 无类型的自定义聚合函数，主要适用于DataFrames。

以下分别使用两种方式来自定义一个求平均值的聚合函数，这里以计算员工平均工资为例。两种自定义方式分别如下：

### 6.1 有类型的自定义函数

```scala
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
```

自定义聚合函数需要实现的方法比较多，这里以绘图的方式来演示其执行流程，以及每个方法的作用：

![spark-sql-自定义函数](D:\BigData-Notes\pictures\spark-sql-自定义函数.png)



关于`zero`,`reduce`,`merge`,`finish`方法的作用在上图都有说明，这里解释一下中间类型和输出类型的编码转换，这个写法比较固定，基本上就是两种情况：

+ 自定义类型case class或者元组就使用`Encoders.product`方法；
+ 基本类型就使用其对应名称的方法，如`scalaByte `，`scalaFloat`，`scalaShort`等。

```scala
override def bufferEncoder: Encoder[SumAndCount] = Encoders.product
override def outputEncoder: Encoder[Double] = Encoders.scalaDouble
```



### 6.2 无类型的自定义聚合函数

理解了有类型的自定义聚合函数后，无类型的定义方式也基本相同，代码如下：

```scala
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Row, SparkSession}

object MyAverage extends UserDefinedAggregateFunction {
  // 1.聚合操作输入参数的类型,字段名称可以自定义
  def inputSchema: StructType = StructType(StructField("MyInputColumn", LongType) :: Nil)

  // 2.聚合操作中间值的类型,字段名称可以自定义
  def bufferSchema: StructType = {
    StructType(StructField("sum", LongType) :: StructField("MyCount", LongType) :: Nil)
  }

  // 3.聚合操作输出参数的类型
  def dataType: DataType = DoubleType

  // 4.此函数是否始终在相同输入上返回相同的输出,通常为true
  def deterministic: Boolean = true

  // 5.定义零值
  def initialize(buffer: MutableAggregationBuffer): Unit = {
    buffer(0) = 0L
    buffer(1) = 0L
  }

  // 6.同一分区中的reduce操作
  def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    if (!input.isNullAt(0)) {
      buffer(0) = buffer.getLong(0) + input.getLong(0)
      buffer(1) = buffer.getLong(1) + 1
    }
  }

  // 7.不同分区中的merge操作
  def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    buffer1(0) = buffer1.getLong(0) + buffer2.getLong(0)
    buffer1(1) = buffer1.getLong(1) + buffer2.getLong(1)
  }

  // 8.计算最终的输出值
  def evaluate(buffer: Row): Double = buffer.getLong(0).toDouble / buffer.getLong(1)
}

object SparkSqlApp {

  // 测试方法
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder().appName("Spark-SQL").master("local[2]").getOrCreate()
    // 9.注册自定义的聚合函数
    spark.udf.register("myAverage", MyAverage)

    val df = spark.read.json("file/emp.json")
    df.createOrReplaceTempView("emp")

    // 10.使用自定义函数和内置函数分别进行计算
    val myAvg = spark.sql("SELECT myAverage(sal) as avg_sal FROM emp").first()
    val avg = spark.sql("SELECT avg(sal) as avg_sal FROM emp").first()

    println("自定义average函数 : " + myAvg)
    println("内置的average函数 : " + avg)
  }
}
```



## 参考资料

[Spark SQL, DataFrames and Datasets Guide > Getting Started](https://spark.apache.org/docs/latest/sql-getting-started.html)