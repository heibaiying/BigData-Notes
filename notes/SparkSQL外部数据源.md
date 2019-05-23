# Spark SQL 外部数据源

## 一、简介

### 1.1 多数据源支持

Spark支持以下六个核心数据源，同时Spark社区还提供了多达上百种数据源的读取方式，能够满足绝大部分使用场景。

- CSV
- JSON
- Parquet
- ORC
- JDBC/ODBC connections
- Plain-text files 

### 1.2 读数据格式

所有数据源读取API都遵循以下调用格式：

```scala
// 格式
DataFrameReader.format(...).option("key", "value").schema(...).load()

// 示例
spark.read.format("csv")
.option("mode", "FAILFAST")          // 读取模式
.option("inferSchema", "true")       // 是否自动推断schema
.option("path", "path/to/file(s)")   // 文件路径
.schema(someSchema)                  // 使用预定义的schema      
.load()
```

读取模式有以下三种可选项：

| 读模式          | 描述                                                         |
| --------------- | ------------------------------------------------------------ |
| `permissive`    | 当遇到损坏的记录时，将其所有字段设置为null，并将所有损坏的记录放在名为_corruption t_record的字符串列中 |
| `dropMalformed` | 删除格式不正确的行                                           |
| `failFast`      | 遇到格式不正确的数据时立即失败                               |

### 1.3 写数据格式

```scala
// 格式
DataFrameWriter.format(...).option(...).partitionBy(...).bucketBy(...).sortBy(
...).save()

//示例
dataframe.write.format("csv")
.option("mode", "OVERWRITE")   //写模式
.option("dateFormat", "yyyy-MM-dd")
.option("path", "path/to/file(s)")
.save()
```

| Scala/Java                         | 描述                                                         |
| :--------------------------------- | :----------------------------------------------------------- |
| `SaveMode.ErrorIfExists`(默认模式) | 如果给定的路径已经存在文件，则抛出异常，这是写数据默认的模式 |
| `SaveMode.Append`                  | 数据以追加的方式写入                                         |
| `SaveMode.Overwrite`               | 数据以覆盖的方式写入                                         |
| `SaveMode.Ignore`                  | Ignore mode means that when saving a DataFrame to a data source, if data already exists, the save operation is expected not to save the contents of the DataFrame and not to change the existing data. This is similar to a `CREATE TABLE IF NOT EXISTS` in SQL. |



## 二、CSV

CSV是一种常见的文本文件格式，其中每一行表示一条记录，记录中的每个字段用逗号分隔。

### 2.1 读取CSV文件

自动推断类型：

```scala
spark.read.format("csv")
.option("header", "true")
.option("mode", "FAILFAST")
.option("inferSchema", "true")
.load("some/path/to/file.csv")
```

使用预定义类型：

```scala
import org.apache.spark.sql.types.{StructField, StructType, StringType,
LongType}
val myManualSchema = new StructType(Array(
new StructField("DEST_COUNTRY_NAME", StringType, true),
new StructField("ORIGIN_COUNTRY_NAME", StringType, true),
new StructField("count", LongType, false)
))
spark.read.format("csv")
.option("header", "true")
.option("mode", "FAILFAST")
.schema(myManualSchema)
.load("/data/flight-data/csv/2010-summary.csv")
.show(5)
```

### 2.2 写入CSV文件

```scala
import org.apache.spark.sql.types.{StructField, StructType, StringType,
LongType}
val myManualSchema = new StructType(Array(
new StructField("DEST_COUNTRY_NAME", StringType, true),
new StructField("ORIGIN_COUNTRY_NAME", StringType, true),
new StructField("count", LongType, false)
))
spark.read.format("csv")
.option("header", "true")
.option("mode", "FAILFAST")
.schema(myManualSchema)
.load("/data/flight-data/csv/2010-summary.csv")
.show(5)
```

将csv文件，转换为tsv文件：

```scala
csvFile.write.format("csv").mode("overwrite").option("sep", "\t")
.save("/tmp/my-tsv-file.tsv")
```

## 三、JSON

### 3.1 读取JSON文件

```json
spark.read.format("json").option("mode", "FAILFAST").schema(myManualSchema)
.load("/data/flight-data/json/2010-summary.json").show(5)
```

### 3.2 写入JSON文件

```scala
csvFile.write.format("json").mode("overwrite").save("/tmp/my-json-file.json")
```

## 四、Parquet

 Parquet是一个开源的面向列的数据存储，它提供了多种存储优化，允许读取单独的列非整个文件，这不仅节省了存储空间而且提升了读取效率，它是Spark是默认的文件格式。

### 4.1 读取Parquet文件

```scala
spark.read.format("parquet")
.load("/data/flight-data/parquet/2010-summary.parquet").show(5)
```

需要注意的是：默认不支持一条数据记录跨越多行，可以通过配置`multiLine`为`true`来进行更改，其默认值为`false`。

### 2.2 写入Parquet文件

```scala
csvFile.write.format("parquet").mode("overwrite")
.save("/tmp/my-parquet-file.parquet")
```

### 2.3 可选配置

Parquet文件有着自己的存储规则，因此其可选配置项比较少，常用的有如下两个：

| 读写操作 | 配置项               | 可选值                                                       | 默认值                                      | 描述                                                         |
| -------- | -------------------- | ------------------------------------------------------------ | ------------------------------------------- | ------------------------------------------------------------ |
| 读       | compression or codec | None,<br/>uncompressed,<br/>bzip2,<br/>deflate, gzip,<br/>lz4, or snappy | None                                        | 压缩文件格式                                                 |
| Read     | mergeSchema          | true, false                                                  | 取决于配置项`spark.sql.parquet.mergeSchema` | 当为真时，Parquet数据源将从所有数据文件收集的Schema合并在一起，否则将从摘要文件中选择Schema，如果没有可用的摘要文件，则从随机数据文件中选择Schema。 |

> 完整的配置列表可以参阅官方文档：https://spark.apache.org/docs/latest/sql-data-sources-parquet.html

## 五、ORC 

ORC是一种自描述的、类型感知的列文件格式，它针对大型数据的读取进行了优化，也是大数据中常用的文件格式。

### 5.1 读取ORC文件 

```scala
spark.read.format("orc").load("/data/flight-data/orc/2010-summary.orc").show(5)
```

### 4.2 写入ORC文件

```scala
csvFile.write.format("orc").mode("overwrite").save("/tmp/my-json-file.orc")
```

## 六、SQL Databases 

spark同样支持与传统的关系型数据库进行数据读写。

### 6.1 获取数据库连接

```scala
import java.sql.DriverManager
val connection = DriverManager.getConnection(url)
connection.isClosed()
connection.close()
```

### 6.2 读取数据

读取全表数据：

```scala
val pgDF = spark.read
.format("jdbc")
.option("driver", "org.postgresql.Driver")
.option("url", "jdbc:postgresql://database_server")
.option("dbtable", "schema.tablename")
.option("user", "username").option("password","my-secret-password").load()
```

读取过滤后的数据：

```scala
val pushdownQuery = """(SELECT DISTINCT(DEST_COUNTRY_NAME) FROM flight_info)
AS flight_info"""
val dbDataFrame = spark.read.format("jdbc")
.option("url", url).option("dbtable", pushdownQuery).option("driver", driver)
.load()
```

```scala
val props = new java.util.Properties
props.setProperty("driver", "org.sqlite.JDBC")
val predicates = Array(
"DEST_COUNTRY_NAME != 'Sweden' OR ORIGIN_COUNTRY_NAME != 'Sweden'",
"DEST_COUNTRY_NAME != 'Anguilla' OR ORIGIN_COUNTRY_NAME != 'Anguilla'")
spark.read.jdbc(url, tablename, predicates, props).count() // 510
```

并行读取数据：

```scala
val dbDataFrame = spark.read.format("jdbc")
.option("url", url).option("dbtable", tablename).option("driver", driver)
.option("numPartitions", 10).load()
```

在这里，我们对第一个分区和最后一个分区分别指定了最小值和最大值。任何超出这些界限的都在第一个分区或最后一个分区中。然后，我们设置希望的分区总数(这是并行度的级别)。

```scala
val colName = "count"
val lowerBound = 0L
val upperBound = 348113L // this is the max count in our database
val numPartitions = 10
spark.read.jdbc(url,tablename,colName,lowerBound,upperBound,numPartitions,props)
.count() // 255
```



### 6.3 写入数据

```scala
val newPath = "jdbc:sqlite://tmp/my-sqlite.db"
csvFile.write.mode("overwrite").jdbc(newPath, tablename, props)
```

## 七、Text 

### 7.1 读取Text数据

```scala
spark.read.textFile("/data/flight-data/csv/2010-summary.csv")
.selectExpr("split(value, ',') as rows").show()
```

### 7.2 写入Text数据

```scala
csvFile.select("DEST_COUNTRY_NAME").write.text("/tmp/simple-text-file.txt")
```

## 八、数据读写高级概念

### 8.1 并行读

多个executors不能同时读取同一个文件，但它们可以同时读取不同的文件。一般来说，这意味着当您从一个包含多个文件的文件夹中读取数据时，这些文件中的每一个都将成为DataFrame中的一个分区，并由可用的executors并行读取。

### 8.2 并行写

写入的文件或数据的数量取决于写入数据时DataFrame拥有的分区数量。默认情况下，每个数据分区写一个文件。

### 8.3 分区写入

```scala
csvFile.limit(10).write.mode("overwrite").partitionBy("DEST_COUNTRY_NAME")
.save("/tmp/partitioned-files.parquet")
```

### 8.3 分桶写入

```scala
val numberBuckets = 10
val columnToBucketBy = "count"
csvFile.write.format("parquet").mode("overwrite").bucketBy(numberBuckets, columnToBucketBy).saveAsTable("bucketedFiles")
```

### 8.5 文件大小管理

如果写入产生大量小文件，这时会产生大量的元数据开销。Spark和HDFS一样，都不能很好的处理这个问题，这被称为“small file problem”。同时数据文件也不能过大，否则在查询时会有不必要的性能开销，因此要把文件大小控制在一个合理的范围内。

在上文我们已经介绍过可以通过分区数量来控制生成文件的数量，从而间接控制文件大小。Spark 2.2引入了一种新的方法，以更自动化的方式控制文件大小，这就是`maxRecordsPerFile`参数，它允许你通过控制写入文件的记录数来控制文件大小。

```scala
 // Spark将确保文件最多包含5000条记录
 df.write.option(“maxRecordsPerFile”, 5000)
```





## 九、附录

### 9.1 CSV读写可选配置

| 读\写操作 | 配置项                      | 可选值                                                       | 默认值                     | 描述                                                         |
| --------- | --------------------------- | ------------------------------------------------------------ | -------------------------- | ------------------------------------------------------------ |
| Both      | seq                         | 任意字符                                                     | `,`(逗号)                  | 分隔符                                                       |
| Both      | header                      | true, false                                                  | false                      | 文件中的第一行是否为列的名称。                               |
| Read      | escape                      | 任意字符                                                     | \                          | 转义字符                                                     |
| Read      | inferSchema                 | true, false                                                  | false                      | 是否自动推断列类型                                           |
| Read      | ignoreLeadingWhiteSpace     | true, false                                                  | false                      | 是否跳过值前面的空格                                         |
| Both      | ignoreTrailingWhiteSpace    | true, false                                                  | false                      | 是否跳过值后面的空格                                         |
| Both      | nullValue                   | 任意字符                                                     | “”                         | 声明文件中哪个字符表示空值                                   |
| Both      | nanValue                    | 任意字符                                                     | NaN                        | 声明哪个值表示NaN或者缺省值                                  |
| Both      | positiveInf                 | 任意字符                                                     | Inf                        | 正无穷                                                       |
| Both      | negativeInf                 | 任意字符                                                     | -Inf                       | 负无穷                                                       |
| Both      | compression or codec        | None,<br/>uncompressed,<br/>bzip2, deflate,<br/>gzip, lz4, or<br/>snappy | none                       | 文件压缩格式                                                 |
| Both      | dateFormat                  | 任何能转换为 Java的 <br/>SimpleDataFormat的字符串            | yyyy-MM-dd                 | 日期格式                                                     |
| Both      | timestampFormat             | 任何能转换为 Java的 <br/>SimpleDataFormat的字符串            | yyyy-MMdd’T’HH:mm:ss.SSSZZ | 时间戳格式                                                   |
| Read      | maxColumns                  | 任意整数                                                     | 20480                      | 声明文件中的最大列数                                         |
| Read      | maxCharsPerColumn           | 任意整数                                                     | 1000000                    | 声明一个列中的最大字符数。                                   |
| Read      | escapeQuotes                | true, false                                                  | true                       | 是否应该转义行中的引号。                                     |
| Read      | maxMalformedLogPerPartition | 任意整数                                                     | 10                         | 声明每个分区中最多允许多少条格式错误的数据，超过这个值后格式错误的数据将不会被读取 |
| Write     | quoteAll                    | true, false                                                  | false                      | 指定是否应该将所有值都括在引号中，而不只是转义具有引号字符的值。 |
| Read      | multiLine                   | true, false                                                  | false                      | 是否允许每条完整记录跨域多行                                 |

### 9.2 JSON读写可选配置

| 读\写操作 | 配置项                             | 可选值                                                       | 默认值                           |
| --------- | ---------------------------------- | ------------------------------------------------------------ | -------------------------------- |
| Both      | compression or codec               | None,<br/>uncompressed,<br/>bzip2, deflate,<br/>gzip, lz4, or<br/>snappy | none                             |
| Both      | dateFormat                         | 任何能转换为 Java的 SimpleDataFormat的字符串                 | yyyy-MM-dd                       |
| Both      | timestampFormat                    | 任何能转换为 Java的 SimpleDataFormat的字符串                 | yyyy-MMdd’T’HH:mm:ss.SSSZZ       |
| Read      | primitiveAsString                  | true, false                                                  | false                            |
| Read      | allowComments                      | true, false                                                  | false                            |
| Read      | allowUnquotedFieldNames            | true, false                                                  | false                            |
| Read      | allowSingleQuotes                  | true, false                                                  | true                             |
| Read      | allowNumericLeadingZeros           | true, false                                                  | false                            |
| Read      | allowBackslashEscapingAnyCharacter | true, false                                                  | false                            |
| Read      | columnNameOfCorruptRecord          | true, false                                                  | Value of spark.sql.column&NameOf |
| Read      | multiLine                          | true, false                                                  | false                            |

### 9.3 数据库读写可选配置

| 属性名称                                   | 含义                                                         |
| ------------------------------------------ | ------------------------------------------------------------ |
| url                                        | 数据库地址                                                   |
| dbtable                                    | 表名称                                                       |
| driver                                     | 数据库驱动                                                   |
| partitionColumn,<br/>lowerBound, upperBoun | 分区总数，上界，下界                                         |
| numPartitions                              | 可用于表读写并行性的最大分区数。如果要写的分区数量超过这个限制，那么可以调用coalesce(numpartition)重置分区数。 |
| fetchsize                                  | 每次往返要获取多少行数据。此选项仅适用于读取数据。           |
| batchsize                                  | 每次往返插入多少行数据，这个选项只适用于写入数据。默认值是1000。 |
| isolationLevel                             | 事务隔离级别：可以是NONE，READ_COMMITTED, READ_UNCOMMITTED，REPEATABLE_READ或SERIALIZABLE，即标准事务隔离级别。<br/>默认值是READ_UNCOMMITTED。这个选项只适用于数据读取。 |
| createTableOptions                         | 写入数据时自定义创建表的相关配置                             |
| createTableColumnTypes                     | 写入数据时自定义创建列的列类型                               |

> 完整的配置可以参阅官方文档：https://spark.apache.org/docs/latest/sql-data-sources-jdbc.html





## 参考资料

1. Matei Zaharia, Bill Chambers . Spark: The Definitive Guide[M] . 2018-02 

