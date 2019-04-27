# Hive基本概念讲解

<nav>
<a href="#一数据类型">一、数据类型</a><br/>
<a href="#二文件格式">二、文件格式</a><br/>
<a href="#三存储格式">三、存储格式</a><br/>
</nav>

## 一、数据类型

### 1.1 基本数据类型

Hive表中的列支持以下基本数据类型：

| 大类                                    | 类型                                                         |
| --------------------------------------- | ------------------------------------------------------------ |
| **Integers（整型）**                    | TINYINT—1字节的有符号整数 <br/>SMALLINT—2字节的有符号整数<br/> INT—4字节的有符号整数<br/> BIGINT—8字节的有符号整数 |
| **Boolean（布尔型）**                   | BOOLEAN—TRUE/FALSE                                           |
| **Floating point numbers（浮点型）**    | FLOAT— 单精度浮点型 <br/>DOUBLE—双精度浮点型                 |
| **Fixed point numbers（定点数）**       | DECIMAL—用户自定义精度定点数，比如DECIMAL(7,2)               |
| **String types（字符串）**              | STRING—指定字符集的字符序列<br/> VARCHAR—具有最大长度限制的字符序列 <br/>CHAR—固定长度的字符序列 |
| **Date and time types（日期时间类型）** | TIMESTAMP —  时间戳 <br/>TIMESTAMP WITH LOCAL TIME ZONE — 时间戳，纳秒精度<br/> DATE—日期类型 |
| **Binary types（二进制类型）**          | BINARY—字节序列                                              |

>TIMESTAMP 和 TIMESTAMP WITH LOCAL TIME ZONE 的区别：
>
>+ TIMESTAMP WITH LOCAL TIME ZONE：用户提交时间给数据库时，该类型会转换成数据库的时区来保存。查询时则按照查询客户端的不同，转换为查询客户端所在的时区的时间。
>
>+ TIMESTAMP ：提交什么时间就保存什么时间，查询时也不做任何转换。

### 1.2 隐式转换

Hive中基本数据类型遵循以下的层次结构，按照这个层次结构，子类型到祖先类型允许隐式转换。例如INT类型的数据允许隐式转换为BIGINT类型。额外注意的是：按照类型层次结构允许将STRING类型隐式转换为DOUBLE类型。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive-data-type.png"/> </div>



### 1.3 复杂类型

| 类型       | 描述                                                         | 示例                                   |
| ---------- | ------------------------------------------------------------ | -------------------------------------- |
| **STRUCT** | 类似于对象，是字段的集合，字段的类型可以不同，可以使用 `名称.字段名`方式进行访问 | STRUCT ('xiaoming', 12 , '2018-12-12') |
| **MAP**    | 键值对的集合，可以使用`名称[key]`的方式访问对应的值          | map('a', 1, 'b', 2)                    |
| **ARRAY**  | 数组是一组具有相同类型和名称的变量的集合，可以使用`名称[index]`访问对应的值 | ARRAY('a', 'b', 'c', 'd')              |



### 1.4 示例

如下给出一个基本数据类型和复杂数据类型的使用示例：

```sql
CREATE TABLE students(
  name      STRING,   -- 姓名
  age       INT,      -- 年龄
  subject   ARRAY<STRING>,   --学科
  score     MAP<STRING,FLOAT>,  --各个学科考试成绩
  address   STRUCT<houseNumber:int, street:STRING, city:STRING, province：STRING>  --家庭居住地址
) ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t";
```



## 二、内容格式

当数据存储在文本文件中，必须按照一定格式区别行和列，比如使用逗号作为分隔符的CSV文件(Comma-Separated Values)或者使用制表符作为分隔值的TSV文件(Tab-Separated Values)。但是使用这些字符作为分隔符的时候存在一个缺点，就是正常的文件内容中也可能出现逗号或者制表符。

所以Hive默认使用了几个平时很少出现的字符，这些字符一般不会作为内容出现在文件中。Hive默认的行和列分隔符如下表所示。

| 分隔符      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| **\n**          | 对于文本文件来说，每行是一条记录，所以可以使用换行符来分割记录 |
| **^A (Ctrl+A)** | 分割字段(列)，在CREATE TABLE语句中也可以使用八进制编码 `\001` 来表示 |
| **^B**          | 用于分割 ARRAY 或者 STRUCT 中的元素，或者用于 MAP 中键值对之间的分割，<br/>在CREATE TABLE语句中也可以使用八进制编码`\002` 表示 |
| **^C**          | 用于 MAP 中键和值之间的分割，在CREATE TABLE语句中也可以使用八进制编码`\003` 表示 |

使用示例如下：

```sql
CREATE TABLE page_view(viewTime INT, userid BIGINT)
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\001'
   COLLECTION ITEMS TERMINATED BY '\002'
   MAP KEYS TERMINATED BY '\003'
 STORED AS SEQUENCEFILE;
```



## 三、存储格式

### 3.1 支持的存储格式

Hive会在HDFS为每个数据库上创建一个目录，数据库中的表是该目录的子目录，表中的数据会以文件的形式存储在对应的表目录下。Hive支持以下几种文件存储格式：

| 格式             | 说明                                                         |
| ---------------- | ------------------------------------------------------------ |
| **TextFile**     | 存储为纯文本文件。 这是Hive默认的文件存储格式。这种存储方式数据不做压缩，磁盘开销大，数据解析开销大。 |
| **SequenceFile** | SequenceFile是Hadoop API提供的一种二进制文件，它将数据以<key,value>的形式序列化到文件中。这种二进制文件内部使用Hadoop的标准的Writable 接口实现序列化和反序列化。它与Hadoop API中的MapFile 是互相兼容的。Hive中的SequenceFile 继承自Hadoop API 的SequenceFile，不过它的key为空，使用value 存放实际的值，这样是为了避免MR 在运行map阶段的排序过程。 |
| **RCFile**       | RCFile文件格式是FaceBook开源的一种Hive的文件存储格式，首先将表分为几个行组，对每个行组内的数据进行按列存储，每一列的数据都是分开存储。 |
| **ORC Files**    | ORC是在一定程度上扩展了RCFile，是对RCFile的优化。            |
| **Avro Files**   | Avro是一个数据序列化系统，设计用于支持大批量数据交换的应用。它的主要特点有：支持二进制序列化方式，可以便捷，快速地处理大量数据；动态语言友好，Avro提供的机制使动态语言可以方便地处理Avro数据。 |
| **Parquet**      | Parquet就是基于Dremel的数据模型和算法实现的，面向分析型业务的列式存储格式。辅以按列的高效压缩和编码技术，实现降低存储空间，提高IO效率，降低上层应用延迟。 |

> 以上压缩格式中ORC和parquet的综合性能突出，使用较为广泛，推荐使用这两种格式。

### 3.2 指定存储格式

通常在创建表的时候使用`STORED AS`参数指定：

```sql
CREATE TABLE page_view(viewTime INT, userid BIGINT)
 ROW FORMAT DELIMITED
   FIELDS TERMINATED BY '\001'
   COLLECTION ITEMS TERMINATED BY '\002'
   MAP KEYS TERMINATED BY '\003'
 STORED AS SEQUENCEFILE;
```

各个存储文件类型指定方式如下：

- STORED AS TEXTFILE

- STORED AS SEQUENCEFILE

- STORED AS ORC

- STORED AS PARQUET

- STORED AS AVRO

- STORED AS RCFILE



## 四、内部表和外部表

内部表又叫做管理表(Managed/Internal Table)，创建表时不做任何指定，默认创建的就是内部表。想要创建外部表(External Table)，则需要使用External进行修饰。 内部表和外部表主要区别如下：

|              | 内部表                                                       | 外部表                                                       |
| ------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 数据存储位置 | 内部表数据存储的位置由hive.metastore.warehouse.dir参数指定，默认情况下表的数据存储在HDFS的`/user/hive/warehouse/数据库名.db/表名/`  目录下 | 外部表数据的存储位置创建表时由`Location`参数指定；           |
| 导入数据     | 在导入数据到内部表，内部表将数据移动到自己的数据仓库目录下，数据的生命周期由Hive来进行管理 | 外部表不会将数据移动到自己的数据仓库目录下，只是在元数据中存储了数据的位置 |
| 删除表       | 删除元数据（metadata）和文件                                 | 只删除元数据（metadata）                                     |




## 参考文档

1. [LanguageManual DDL](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+DDL)
2. [LanguageManual Types](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Types)
3. [Managed vs. External Tables](https://cwiki.apache.org/confluence/display/Hive/Managed+vs.+External+Tables)
