# 数据仓库——Hive



<nav>
<a href="#一简介">一、简介</a><br/>
<a href="#二Hive的体系架构">二、Hive的体系架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-command-line-shell--thriftjdbc">2.1 command-line shell & thrift/jdbc</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-Metastore">2.2 Metastore</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-执行流程">2.3 执行流程</a><br/>
</nav>


## 一、简介

Hive构建在Hadoop之上的，可以将结构化的数据文件映射成表，并提供类SQL查询功能，分析查询SQL语句被转化为MapReduce任务在Hadoop框架中运行。

**特点**：

1. 简单、容易上手（提供了类似sql查询语言hql），使得精通sql 但是不了解 Java 编程的人也能很好的进行大数据分析；

2. 其执行延迟高，不适合做实时数据的处理，但其适合做海量数据的离线处理；
3. 灵活性高，可以自定义用户函数(UDF)和自定义存储格式；
4. 为超大的数据集设计的计算／存储扩展能力（基于Hadoop，MR计算，HDFS存储），集群扩展容易;
5. 统一的元数据管理（可与presto／impala／sparksql等共享数据，详见下文）。



## 二、Hive的体系架构

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hive体系架构.png"/> </div>

### 2.1 command-line shell & thrift/jdbc

我们可以用command-line shell和thrift／jdbc两种方式来操作数据：可以通过hive脚本的方式来操作，也可以通过服务器，通过thrift协议按照编译jdbc的方式就能够完成对hive里面的数据进行相应的操作。

### 2.2 Metastore

hive里的表名、表结构、字段名、字段类型、表的分隔符等信息就叫做元数据。Metastore是用来存储Hive的元数据，默认元数据是存储在derby关系型数据库中，但是derby是能同时只有一个实例，也就是说不能多个命令行接口同时使用，所以可以替换mysql等。

这里还需要说明的是hive进行的是同一的元数据管理，就是说你在hive上创建了一张表，然后在presto／impala／sparksql 中都是可以直接使用的，同样的你在presto／impala／sparksql中创建一张表，在hive中也是可以使用的。

### 2.3 执行流程

1. 客户端提交的sql后首先会通过Driver,然后通过SQL Parser进行sql解析，首先把语句解析成**抽象语法树**之后才能转换成**逻辑性执行计划**；
2. 接着查询优化工具Query Optimizer对我们**逻辑性执行计划**进行优化，最终再生成**物理性执行计划**（physical plan）；
3. 在物理性执行计划中还包括序列化和反序列化（SerDes），用户自定义函数（User Defined Functions，UTFs）；
4. 把最终的物理执行计划生成**执行引擎**（Execution）提交到mapreduce上去执行；
5. mapreduce的执行肯定有输入和输出，那么这个输入输出可以是hadoop文件系统上的（Hadoop Storage）比如hdfs，hbase包括本地的文件也都是可以的。



## 参考资料

1. [Hive Getting Started](https://cwiki.apache.org/confluence/display/Hive/GettingStarted)
2. [Hive - 建立在Hadoop架构之上的数据仓库](https://zhuanlan.zhihu.com/p/29209577)

