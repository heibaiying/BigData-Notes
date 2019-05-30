# 数据仓库——Hive



<nav>
<a href="#一简介">一、简介</a><br/>
<a href="#二Hive的体系架构">二、Hive的体系架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-command-line-shell--thriftjdbc">2.1 command-line shell & thrift/jdbc</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-Metastore">2.2 Metastore</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-执行流程">2.3 执行流程</a><br/>
</nav>


## 一、简介

Hive是一个构建在Hadoop之上的数据仓库，它可以将结构化的数据文件映射成表，并提供类SQL查询功能，用于查询的SQL语句会被转化为MapReduce作业，然后提交到Hadoop上运行。

**特点**：

1. 简单、容易上手(提供了类似sql的查询语言hql)，使得精通sql但是不了解Java编程的人也能很好地进行大数据分析；
3. 灵活性高，可以自定义用户函数(UDF)和存储格式；
4. 为超大的数据集设计的计算和存储能力，集群扩展容易;
5. 统一的元数据管理，可与presto／impala／sparksql等共享数据；
5. 执行延迟高，不适合做数据的实时处理，但适合做海量数据的离线处理。



## 二、Hive的体系架构

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive体系架构.png"/> </div>

### 2.1 command-line shell & thrift/jdbc

可以用command-line shell和thrift／jdbc两种方式来操作数据：

+ **command-line shell**：可以通过hive命令行的的方式来操作数据；
+ **thrift／jdbc**：可以通过thrift协议按照标准的JDBC的方式操作数据。

### 2.2 Metastore

在Hive中，表名、表结构、字段名、字段类型、表的分隔符等统一被称为元数据。所有的元数据默认存储在Hive内置的derby数据库中，但由于derby只能有一个实例，也就是说不能有多个命令行客户端同时访问，所以在实际生产环境中，通常使用MySQL代替derby。

Hive进行的是统一的元数据管理，就是说你在Hive上创建了一张表，然后在presto／impala／sparksql 中都是可以直接使用的，它们会从Metastore中获取统一的元数据信息，同样的你在presto／impala／sparksql中创建一张表，在Hive中也可以直接使用。

### 2.3 执行流程

1. 客户端提交的SQL后首先会通过SQL Parser进行解析，把语句解析成**抽象语法树**后转换成**逻辑性执行计划**；
2. 接着查询优化工具Query Optimizer会对**逻辑性执行计划**进行优化，生成**物理性执行计划**（physical plan）；
3. 在生成物理执行计划中，还包括一些序列化和反序列化操作(SerDes)，以及解析用户自定义函数(User Defined Functions，UTFs)的操作；
4. 接着物理执行计划被转换为MapReduce作业，提交到Hadoop上执行；



## 参考资料

1. [Hive Getting Started](https://cwiki.apache.org/confluence/display/Hive/GettingStarted)
2. [Hive - 建立在Hadoop架构之上的数据仓库](https://zhuanlan.zhihu.com/p/29209577)

