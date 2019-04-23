# Hive数据类型和文件格式

<nav>
<a href="#一数据类型">一、数据类型</a><br/>
<a href="#二文件格式">二、文件格式</a><br/>
<a href="#三存储格式">三、存储格式</a><br/>
</nav>


## 一、数据类型

### 1.1 基本数据类型

Hive表中的列支持以下基本数据类型：

- **Integers（整型）**
  - TINYINT—1字节的有符号整数
  - SMALLINT—2字节的有符号整数
  - INT—4字节的有符号整数
  - BIGINT—8字节的有符号整数
- **Boolean（布尔型）**
  - BOOLEAN—TRUE/FALSE
- **Floating point numbers（浮点型）**
  - FLOAT— 单精度浮点型
  - DOUBLE—双精度浮点型
- **Fixed point numbers（定点数）**
  - DECIMAL—用户自定义精度定点数，比如2.4，3.68
- **String types（字符串）**
  - STRING—指定字符集的字符序列
  - VARCHAR—具有最大长度限制的字符序列
  - CHAR—固定长度的字符序列
- **Date and time types（日期时间类型）**
  - TIMESTAMP —  时间戳
  - TIMESTAMP WITH LOCAL TIME ZONE — 时间戳，纳秒精度
  - DATE—日期类型
- **Binary types（二进制类型）**
  - BINARY—字节序列

>TIMESTAMP 和 TIMESTAMP WITH LOCAL TIME ZONE 的区别：
>
>TIMESTAMP WITH LOCAL TIME ZONE：用户提交时间给数据库时，该类型会转换成数据库的时区来保存。查询时则按照查询客户端的不同，转换为查询客户端所在的时区的时间。
>
>TIMESTAMP ：提交什么时间就保存什么时间，查询时也不做任何转换。

### 1.2 隐式转换

Hive中基本数据类型遵循以下的层次结构，按照这个层次结构，子类型到祖先类型允许隐式转换。例如INT类型的数据允许隐式转换为BIGINT类型。额外注意的是：按照类型层次结构允许将STRING类型隐式转换为DOUBLE类型。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive-data-type.png"/> </div>



### 1.3 复杂类型

| 类型   | 描述                                                         | 示例                                   |
| ------ | ------------------------------------------------------------ | -------------------------------------- |
| STRUCT | 类似于对象，是字段的集合，字段的类型可以不同，可以使用 `名称.字段名`方式进行访问 | STRUCT ('xiaoming', 12 , '2018-12-12') |
| MAP    | 键值对的集合，可以使用`名称[key]`的方式访问对应的值          | map('a', 1, 'b', 2)                    |
| ARRAY  | 数组是一组具有相同类型和名称的变量的集合，可以使用`名称[index]`访问对应的值 | ARRAY('a', 'b', 'c', 'd')              |



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



## 二、文件格式

当数据存储在文本文件中，必须按照一定格式区别行和列，比如使用逗号作为分隔符的CSV文件(Comma-Separated Values)或者使用制表符作为分隔值的TSV文件(Tab-Separated Values)。但是使用这些字符作为分隔符的时候存在一个缺点，就是正常的文件内容中也可能出现逗号或者制表符。

所以Hive默认使用了几个平时很少出现的字符，这些字符一般不会作为内容出现在文件中。Hive默认的行和列分隔符如下表所示。

| 分隔符      | 描述                                                         |
| ----------- | ------------------------------------------------------------ |
| \n          | 对于文本文件来说，每行是一条记录，所以可以使用换行符来分割记录 |
| ^A (Ctrl+A) | 分割字段(列)，在CREATE TABLE语句中也可以使用八进制编码 `\001` 来表示 |
| ^B          | 用于分割 ARRAY 或者 STRUCT 中的元素，或者用于 MAP 中键值对之间的分割，<br/>在CREATE TABLE语句中也可以使用八进制编码`\002` 表示 |
| ^C          | 用于 MAP 中键和值之间的分割，在CREATE TABLE语句中也可以使用八进制编码`\003` 表示 |



## 三、存储格式