# Spark简介

<nav>
<a href="#一简介">一、简介</a><br/>
<a href="#二特点">二、特点</a><br/>
<a href="#三集群架构">三、集群架构</a><br/>
<a href="#四核心组件">四、核心组件</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-Spark--SQL">3.1 Spark  SQL</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-Spark-Streaming">3.2 Spark Streaming</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-MLlib">3.3 MLlib</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-Graphx">3.4 Graphx</a><br/>
<a href="#">  </a><br/>
</nav>

## 一、简介

Spark于2009年诞生于加州大学伯克利分校AMPLab。2013年，该项目被捐赠给Apache软件基金会。2014年2月，成为Apache的顶级项目。相对于MapReduce上的批处理计算，Spark可以带来上百倍的性能提升，因此它成为继MapReduce之后，最为广泛使用的计算框架。

## 二、特点

+ Apache Spark使用最先进的DAG调度程序，查询优化器和物理执行引擎，以实现性能上的保证；
+ 多语言支持，如Java，Scala，Python，R语言;
+ Spark提供80多个高级的API，可以轻松构建并行应用程序；
+ 支持批处理，流处理和复杂的分析；
+ 丰富的类库支持：包括SQL，DataFrames，MLlib，GraphX和Spark Streaming等库。并且可以在同一个应用程序中无缝地进行组合；  
+ 丰富的部署模式：支持本地模式和自带的集群模式，也支持在Hadoop，Mesos，Kubernetes上运行；
+ 多数据源支持：支持访问HDFS，Alluxio，Cassandra，HBase，Hive以及数百个其他数据源中的数据。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/future-of-spark.png"/> </div>

## 三、集群架构

| Term（术语）    | Meaning（含义）                                              |
| --------------- | ------------------------------------------------------------ |
| Application     | Spark应用程序，由集群上的一个Driver节点和多个Executor节点组成。 |
| Driver program  | 主运用程序，该进程运行应用的 main() 方法并且创建了 SparkContext |
| Cluster manager | 集群资源管理器（例如，Standlone Manager，Mesos，YARN）       |
| Worker node     | 执行计算任务的工作节点                                       |
| Executor        | 位于工作节点上的应用进程，负责执行计算任务并且将输出数据保存到内存或者磁盘中 |
| Task            | 被发送到Executor中的工作单元                                 |

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-集群模式.png"/> </div>

**执行过程**：

1. 用户程序创建SparkContext后，其会连接到集群资源管理器，集群资源管理器会为计算程序分配计算资源，并启动Executor；
2. Dirver将计算程序划分为不同的执行阶段和多个Task，之后将Task发送给Executor；
3. Executor负责执行Task，并将执行状态汇报给Driver，同时也会将当前节点资源的使用情况汇报给集群资源管理器。

## 四、核心组件

Spark基于Spark Core扩展了四个核心组件，分别用于满足不同领域的计算需求。

<div align="center"> <img  width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-stack.png"/> </div>

### 3.1 Spark  SQL

Spark SQL主要用于结构化数据的处理。其具有以下特点：

- 能够将SQL查询与Spark程序无缝混合，允许您使用SQL或DataFrame API对结构化数据进行查询；
- 支持多种数据源，包括Hive，Avro，Parquet，ORC，JSON和JDBC；
- 支持HiveQL语法以及Hive SerDes和UDF，允许你访问现有的Hive仓库；
- 支持标准的JDBC和ODBC连接；
- 支持优化器，列式存储和代码生成等特性，以提高查询效率。

### 3.2 Spark Streaming

Spark Streaming主要用于快速构建可扩展，高吞吐量，高容错的流处理程序。支持从HDFS，Flume，Kafka，Twitter和ZeroMQ读取数据，并进行处理。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-streaming-arch.png"/> </div>

 Spark Streaming的本质是微批处理，它将数据流进行极小粒度的拆分，拆分为多个批处理，使得其能够得到接近于流处理的效果。

<div align="center"> <img width="600px"   src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-streaming-flow.png"/> </div>



### 3.3 MLlib

MLlib是Spark的机器学习库。其设计目标是使得机器学习变得简单且可扩展。它提供了以下工具：

+ 常见的机器学习算法：如分类，回归，聚类和协同过滤；
+ 特征化：特征提取，转换，降维和选择；
+ 管道：用于构建，评估和调整ML管道的工具；
+ 持久性：保存和加载算法，模型，管道数据；
+ 实用工具：线性代数，统计，数据处理等。

### 3.4 Graphx

GraphX是Spark中用于图形计算和图形并行计算的新组件。在高层次上，GraphX通过引入一个新的图形抽象来扩展 RDD：一种具有附加到每个顶点和边缘的属性的定向多重图形。为了支持图计算，GraphX提供了一组基本运算符（如： subgraph，joinVertices 和 aggregateMessages）以及优化后的Pregel API。此外，GraphX 还包括越来越多的图形算法和构建器，以简化图形分析任务。

##   
