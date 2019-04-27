# Hive分区表和分桶表

<nav>
<a href="#一分区表">一、分区表</a><br/>
<a href="#二分桶表">二、分桶表</a><br/>
<a href="#三分区表和分桶表结合使用">三、分区表和分桶表结合使用</a><br/>
</nav>


## 一、分区表

### 1.1 概念

Hive中的表表现为HDFS上的某个目录，在查询数据时候，默认都会对全表进行扫描，这样时间和性能的消耗都非常大。

**分区表现为HDFS上表目录的子目录**，数据按照分区存储在子目录中。如果查询的`where`字句的中包含分区条件，则直接从该分区去查找，而不是扫描整个表目录，合理的分区设计可以极大提高查询速度和性能。

>这里说明一下分区表并Hive独有的概念，实际上这个概念非常常见。比如在我们常用的Oracle数据库中，当表中的数据量不断增大，查询数据的速度就会下降，这时也可以对表进行分区。表进行分区后，逻辑上表仍然是一张完整的表，只是将表中的数据存放到多个表空间（物理文件上），这样查询数据时，就不必要每次都扫描整张表，从而提升查询性能。

### 1.2  使用场景

通常，在管理大型生产数据集的时候都需要进行分区，比如在日志文件分析的项目中，通过按天进行分区，从而保证数据的细粒度划分，使得查询性能得以提升。

### 1.3 创建分区表

在Hive中可以使用`PARTITIONED BY`子句创建分区表。表可以包含一个或多个分区列，程序会为分区列中的每个不同值组合创建单独的数据目录。下面的我们创建一张雇员表作为测试：

```shell
 CREATE EXTERNAL TABLE emp_partition(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2)
    )
    PARTITIONED BY (deptno INT)   -- 按照部门编号进行分区
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"
    LOCATION '/hive/emp_partition';
```

### 1.4 加载数据到分区表

加载数据到分区表时候必须要指定数据所处的分区。

```shell
# 加载部门编号为20的数据到表中
LOAD DATA LOCAL INPATH "/usr/file/emp20.txt" OVERWRITE INTO TABLE emp_partition PARTITION (deptno=20)
# 加载部门编号为30的数据到表中
LOAD DATA LOCAL INPATH "/usr/file/emp30.txt" OVERWRITE INTO TABLE emp_partition PARTITION (deptno=30)
```

### 1.5 查看分区目录

这时候我们直接查看表目录，可以看到表目录下存在两个目录，分别是`deptno=20`和`deptno=30`,这就是分区目录，分区目录下才是我们加载的数据文件。

```shell
# hadoop fs -ls  hdfs://hadoop001:8020/hive/emp_partition/
```

这时候当你的查询语句的`where`包含`deptno=20`，则就去对应的分区目录下进行查找，而不用扫描全表。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive-hadoop-partitation.png"/> </div>



## 二、分桶表

### 1.1 简介

分区提供了一个隔离数据和优化查询的可行方案，但是并非所有的数据集都可以形成合理的分区，分区的数量也不是越多越好，过多的分区条件可能会导致很多分区上并没有数据。同时Hive会限制动态分区可以创建的最大分区数，用来避免过多分区文件的产生而对文件系统的处理能力产生负担。鉴于以上原因，Hive还提供了一种更加细粒度的数据拆分方案：分桶表(bucket Table)。

分桶表会将指定列的值进行哈希散列，并对bucket（桶数量）取余，然后存储到对应的bucket（桶）中。

### 1.2 理解分桶表

单从概念上理解分桶表可能会比较晦涩，其实和分区一样，分桶这个概念同样不是Hive独有的，实际上对于Java开发人员而言，这可能是一个每天都会用到的概念，因为Hive中的分桶概念和Java数据结构中的HashMap的分桶概念是一致的。

在HashMap中，当我们给put()方法传递键和值时，我们先对键调用hashCode()方法，返回的hashCode用于找到bucket(桶)位置，最后将键值对存储在对应桶的链表结构中，链表达到一定阈值后会转换为红黑树(JDK1.8+)。下图为HashMap的数据结构图：

<div align="center"> <img width="600px"  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HashMap-HashTable.png"/> </div>

> 图片引用自：[HashMap vs. Hashtable](http://www.itcuties.com/java/hashmap-hashtable/)

### 1.3 创建分桶表

在Hive中，我们可以通过`CLUSTERED BY`指定分桶列，并通过`SORTED BY`指定桶中数据排序参考列。下面为分桶表的建表语句示例：

```sql
  CREATE EXTERNAL TABLE emp_bucket(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2),
    deptno INT)
    CLUSTERED BY(empno) SORTED BY(empno ASC) INTO 4 BUCKETS  --按照员工编号散列到四个bucket中
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"
    LOCATION '/hive/emp_bucket';
```

### 1.4 加载数据到分桶表

这里直接使用`Load`语句向分桶表加载数据，数据时可以加载成功的，但是数据并不会分桶。

这是由于分桶的实质是对指定字段做了hash散列然后存放到对应文件中，这意味着向分桶表中插入数据是必然要通过MapReduce，且Reducer的数量必须等于分桶的数量。由于以上原因，分桶表的数据通常只能使用CTAS(CREATE TABLE AS SELECT)方式插入，因为CTAS操作会触发MapReduce。加载数据步骤如下：

#### 1. 设置强制分桶

```sql
set hive.enforce.bucketing = true; --Hive 2.x不需要这一步
```
在Hive 0.x and 1.x版本，必须使用设置`hive.enforce.bucketing = true`，表示强制分桶，允许程序根据表结构自动选择正确数量的Reducer和cluster by  column来进行分桶。

#### 2. CTAS导入数据

```sql
INSERT INTO TABLE emp_bucket SELECT *  FROM emp;  --这里的emp表就是一张普通的雇员表
```

可以从执行日志看到CTAS触发MapReduce操作，且Reducer数量和建表时候指定bucket数量一致：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive-hadoop-mapreducer.png"/> </div>

### 1.5 查看分桶文件

bucket(桶)本质上就是表目录下的具体文件：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive-hadoop-bucket.png"/> </div>



## 三、分区表和分桶表结合使用

分区表和分桶表的本质都是将数据按照不同粒度进行拆分，从而使得在查询时候不必扫描全表，只需要扫描对应的分区或分桶，从而提升查询效率。两者可以结合起来使用，从而保证表数据在不同粒度上都能得到合理的拆分。下面是Hive官方给出的示例：

```sql
CREATE TABLE page_view_bucketed(
	viewTime INT, 
    userid BIGINT,
    page_url STRING, 
    referrer_url STRING,
    ip STRING )
 PARTITIONED BY(dt STRING)
 CLUSTERED BY(userid) SORTED BY(viewTime) INTO 32 BUCKETS
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\001'
   COLLECTION ITEMS TERMINATED BY '\002'
   MAP KEYS TERMINATED BY '\003'
 STORED AS SEQUENCEFILE;
```

此时导入数据时候也需要指定分区：

```shell
INSERT OVERWRITE page_view_bucketed
PARTITION (dt='2009-02-25')
SELECT * FROM page_view WHERE dt='2009-02-25';
```



## 参考资料

1. [LanguageManual DDL BucketedTables](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL+BucketedTables)
