# Structured API基本使用

<nav>
<a href="#一创建DataFrames">一、创建DataFrames</a><br/>
<a href="#二DataFrames基本操作">二、DataFrames基本操作</a><br/>
<a href="#三创建Datasets">三、创建Datasets</a><br/>
<a href="#四DataFrames与Datasets互相转换">四、DataFrames与Datasets互相转换</a><br/>
<a href="#五RDDs转换为DataFramesDatasets">五、RDDs转换为DataFrames\Datasets</a><br/>
</nav>


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

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-sql-shell.png"/> </div>

## 二、DataFrames基本操作

### 2.1 printSchema

```scala
// 以树形结构打印dataframe的schema信息 
df.printSchema()
```

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-scheme.png"/> </div>

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



## 参考资料

[Spark SQL, DataFrames and Datasets Guide > Getting Started](https://spark.apache.org/docs/latest/sql-getting-started.html)