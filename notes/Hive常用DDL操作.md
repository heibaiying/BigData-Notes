# Hive常用DDL操作

## 一、Database

### 1.1 查看数据列表

```sql
show databases;
```

![hive-show-database](D:\BigData-Notes\pictures\hive-show-database.png)

### 1.2 使用数据库

```sql
USE database_name;
```

### 1.3 新建数据库

语法：

```sql
CREATE (DATABASE|SCHEMA) [IF NOT EXISTS] database_name   --DATABASE|SCHEMA是等价的
  [COMMENT database_comment] --数据库注释
  [LOCATION hdfs_path] --存储在HDFS上的位置
  [WITH DBPROPERTIES (property_name=property_value, ...)]; --指定额外属性
```

示例：

```sql
CREATE DATABASE IF NOT EXISTS hive_test
  COMMENT 'hive database for test'
  WITH DBPROPERTIES ('create'='heibaiying');
```



### 1.4 查看数据库信息

语法：

```sql
DESC DATABASE [EXTENDED] db_name; --EXTENDED 表示是否显示额外属性
```

示例：

```sql
DESC DATABASE  EXTENDED hive_test;
```



### 1.5 删除数据库

语法：

```sql
DROP (DATABASE|SCHEMA) [IF EXISTS] database_name [RESTRICT|CASCADE];
--默认行为是RESTRICT，如果数据库中存在表则删除失败。要想删除库及其中的表，可以使用CASCADE级联删除。
```

示例：

```sql
  DROP DATABASE IF EXISTS hive_test CASCADE;
```



## 二、创建表

### 2.1 建表语法

```sql
CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name     --表名
  [(col_name data_type [COMMENT col_comment],
    ... [constraint_specification])]  --列名 列数据类型
  [COMMENT table_comment]   --表描述
  [PARTITIONED BY (col_name data_type [COMMENT col_comment], ...)]  --分区表分区规则
  [CLUSTERED BY (col_name, col_name, ...) [SORTED BY (col_name [ASC|DESC], ...)] INTO num_buckets BUCKETS]  --分桶表分桶规则
  [SKEWED BY (col_name, col_name, ...) ON ((col_value, col_value, ...), (col_value, col_value, ...), ...)  
   [STORED AS DIRECTORIES] 
  ]  --指定倾斜列和值
  [
   [ROW FORMAT row_format]    
   [STORED AS file_format]
     | STORED BY 'storage.handler.class.name' [WITH SERDEPROPERTIES (...)]  
  ]  -- 指定行分隔符、存储文件格式或采用自定义存储格式
  [LOCATION hdfs_path]  -- 指定表的存储位置
  [TBLPROPERTIES (property_name=property_value, ...)]  --指定表的属性
  [AS select_statement];   --从查询结果创建表
```

### 2.2 内部表

```sql
  CREATE TABLE emp(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2),
    deptno INT)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t";
```

### 2.3 外部表

```sql
  CREATE EXTERNAL TABLE emp_external(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2),
    deptno INT)
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"
    LOCATION '/hive/emp_external';
```

使用 `desc format  emp_external`命令可以查看表的详细信息如下：

![hive-external-table](D:\BigData-Notes\pictures\hive-external-table.png)

### 2.4 分区表

```sql
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

### 2.5 分桶表

```sql
  CREATE EXTERNAL TABLE emp_bucket(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2)
    )
    CLUSTERED BY(empno) SORTED BY(empno ASC) INTO 4 BUCKETS  --按照员工编号散列到四个bucket中
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"
    LOCATION '/hive/emp_bucket';
```

### 2.6 倾斜表

```sql
  CREATE EXTERNAL TABLE emp_skewed(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2)
    )
    SKEWED BY (empno) ON (66,88,100)  --指定数据倾斜
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t"
    LOCATION '/hive/emp_skewed';   
```

### 2.7 临时表

临时表仅对当前session可见。数据将存储在用户的暂存目录中，并在会话结束时删除。如果临时表与永久表表名相同，则对该表名的任何引用都将解析为临时表，而不是永久表。临时表具有以下限制：

如果使用数据库中已存在的永久表的数据库/表名创建临时表，则在该会话中，对该表的任何引用都将解析为临时表，而不是永久表。如果不删除临时表或将其重命名为非冲突名称，用户将无法访问该会话中的原始表。

+ 不支持分区列；
+ 不支持创建索引。

```sql
  CREATE TEMPORARY TABLE emp_temp(
    empno INT,
    ename STRING,
    job STRING,
    mgr INT,
    hiredate TIMESTAMP,
    sal DECIMAL(7,2),
    comm DECIMAL(7,2)
    )
    ROW FORMAT DELIMITED FIELDS TERMINATED BY "\t";
```

### 2.8 CTAS创建表

支持从查询语句的结果创建表：

```sql
CREATE TABLE emp_copy AS SELECT * FROM emp WHERE deptno='20';
```

### 2.9 复制表结构

语法：

```sql
CREATE [TEMPORARY] [EXTERNAL] TABLE [IF NOT EXISTS] [db_name.]table_name  --创建表表名
   LIKE existing_table_or_view_name  --被复制表的表名
   [LOCATION hdfs_path]; --存储位置
```

示例：

```sql
CREATE TEMPORARY EXTERNAL TABLE  IF NOT EXISTS  emp_co  LIKE emp
```



### 2.10 加载数据到表

加载数据到表中属于DML操作，不属于本小节讲解的DDL操作，这里为了方便大家测试，所以先简单介绍一下加载本地数据到表中：

```sql
-- 加载数据到emp表中
load data local inpath "/usr/file/emp.txt" into table emp;
```

其中emp.txt的文件内容如下，你可以直接复制粘贴，也可以到本仓库的resources目录下载对应的文件：

```txt
7369	SMITH	CLERK	7902	1980-12-17 00:00:00	800.00		20
7499	ALLEN	SALESMAN	7698	1981-02-20 00:00:00	1600.00	300.00	30
7521	WARD	SALESMAN	7698	1981-02-22 00:00:00	1250.00	500.00	30
7566	JONES	MANAGER	7839	1981-04-02 00:00:00	2975.00		20
7654	MARTIN	SALESMAN	7698	1981-09-28 00:00:00	1250.00	1400.00	30
7698	BLAKE	MANAGER	7839	1981-05-01 00:00:00	2850.00		30
7782	CLARK	MANAGER	7839	1981-06-09 00:00:00	2450.00		10
7788	SCOTT	ANALYST	7566	1987-04-19 00:00:00	1500.00		20
7839	KING	PRESIDENT		1981-11-17 00:00:00	5000.00		10
7844	TURNER	SALESMAN	7698	1981-09-08 00:00:00	1500.00	0.00	30
7876	ADAMS	CLERK	7788	1987-05-23 00:00:00	1100.00		20
7900	JAMES	CLERK	7698	1981-12-03 00:00:00	950.00		30
7902	FORD	ANALYST	7566	1981-12-03 00:00:00	3000.00		20
7934	MILLER	CLERK	7782	1982-01-23 00:00:00	1300.00		10
```

加载后可查询表中数据：

![hive-select-emp](D:\BigData-Notes\pictures\hive-select-emp.png)



## 三、修改表

### 3.1 重命名表

语法：

```sql
ALTER TABLE table_name RENAME TO new_table_name;
```

示例：

```sql
ALTER TABLE emp_temp RENAME TO new_emp; --把emp_temp表重命名为new_emp
```



### 3.2 修改列

语法：

```sql
ALTER TABLE table_name [PARTITION partition_spec] CHANGE [COLUMN] col_old_name col_new_name column_type
  [COMMENT col_comment] [FIRST|AFTER column_name] [CASCADE|RESTRICT];
```

示例：

```sql
-- 修改字段名和类型
ALTER TABLE emp_temp CHANGE empno empno_new INT;
 
-- 修改字段sal的名称 并将其放置到empno字段后
ALTER TABLE emp_temp CHANGE sal sal_new decimal(7,2)  AFTER ename;

-- 为字段增加注释
ALTER TABLE emp_temp CHANGE mgr mgr_new INT COMMENT 'this is column mgr';
```



### 3.3 新增列

示例：

```sql
ALTER TABLE emp_temp ADD COLUMNS (address STRING COMMENT 'home address');
```



### 3.4 修改分区

