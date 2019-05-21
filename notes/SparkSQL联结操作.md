## 一、简介



## 二、 数据准备

分别创建员工和部门datafame，并注册为临时视图，代码如下：

```scala
val spark = SparkSession.builder().appName("aggregations").master("local[2]").getOrCreate()

val empDF = spark.read.json("/usr/file/json/emp.json")
empDF.createOrReplaceTempView("emp")

val deptDF = spark.read.json("/usr/file/json/dept.json")
deptDF.createOrReplaceTempView("dept")
```

两表字段中所有字段如下：

```properties
emp员工表
 |-- ENAME: 员工姓名
 |-- DEPTNO: 部门编号
 |-- EMPNO: 员工编号
 |-- HIREDATE: 入职时间
 |-- JOB: 职务
 |-- MGR: 上级编号
 |-- SAL: 薪资
 |-- COMM: 奖金  
```

```properties
dept部门表
 |-- DEPTNO: 部门编号
 |-- DNAME:  部门名称
 |-- LOC:    部门所在城市
```

> 注：emp.json，dept.json可以在本仓库的resources目录进行下载。



## 三、联结操作

### 3.1 Inner Joins 

```scala
// 1.定义联结表达式
val joinExpression = empDF.col("deptno") === deptDF.col("deptno")
// 2.联结查询 
empDF.join(deptDF,joinExpression).select("ename","dname").show()

// 等价SQL如下：
spark.sql("SELECT ename,dname FROM emp JOIN dept ON emp.deptno = dept.deptno").show()
```

