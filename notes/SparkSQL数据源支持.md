1.1 Json

```scala
val empDF = spark.read.json("/usr/file/json/emp.json")
empDF.show()
```

1.2 

```scala
val parquetFileDF = spark.read.parquet("/usr/file/parquet/emp.parquet")
parquetFileDF.show()
```

