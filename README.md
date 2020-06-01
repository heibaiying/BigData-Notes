# BigData-Notes



<div align="center"> <img width="444px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/bigdata-notes-icon.png"/> </div>
<br/>

**大数据入门指南**



<table>
    <tr>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop.jpg"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive.jpg"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/spark.jpg"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/flink.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hbase.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/kafka.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/zookeeper.jpg"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/flume.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/sqoop.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/azkaban.png"></th>
      <th><img width="50px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/scala.jpg"></th>
    </tr>
    <tr>
      <td align="center"><a href="#一hadoop">Hadoop</a></td>
      <td align="center"><a href="#二hive">Hive</a></td>
      <td align="center"><a href="#三spark">Spark</a></td>
      <td align="center"><a href="#四storm">Storm</a></td>
      <td align="center"><a href="#五flink">Flink</a></td>
      <td align="center"><a href="#六hbase">HBase</a></td>
      <td align="center"><a href="#七kafka">Kafka</a></td>
      <td align="center"><a href="#八zookeeper">Zookeeper</a></td>
      <td align="center"><a href="#九flume">Flume</a></td>
      <td align="center"><a href="#十sqoop">Sqoop</a></td>
      <td align="center"><a href="#十一azkaban">Azkaban</a></td>
      <td align="center"><a href="#十二scala">Scala</a></td>
    </tr>
  </table>
<br/>

<div align="center">
	<a href = "https://github.com/heibaiying/Full-Stack-Notes"> 
	<img width="150px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin.jpg"/> 
	</a> 
</div>
<div align="center"> <strong> 如果需要离线阅读，可以在公众号上发送 “bigdata” 获取《大数据入门指南》离线阅读版！ </strong> </div>

<br/>

## :black_nib: 前  言

1. [大数据学习路线](notes/大数据学习路线.md)
2. [大数据技术栈思维导图](notes/大数据技术栈思维导图.md)        
3. [大数据常用软件安装指南](notes/大数据常用软件安装指南.md)

## 一、Hadoop

1. [分布式文件存储系统 —— HDFS](notes/Hadoop-HDFS.md)
2. [分布式计算框架 —— MapReduce](notes/Hadoop-MapReduce.md)
3. [集群资源管理器 —— YARN](notes/Hadoop-YARN.md)
4. [Hadoop 单机伪集群环境搭建](notes/installation/Hadoop单机环境搭建.md)
5. [Hadoop 集群环境搭建](notes/installation/Hadoop集群环境搭建.md)
6. [HDFS 常用 Shell 命令](notes/HDFS常用Shell命令.md)
7. [HDFS Java API 的使用](notes/HDFS-Java-API.md)
8. [基于 Zookeeper 搭建 Hadoop 高可用集群](notes/installation/基于Zookeeper搭建Hadoop高可用集群.md)

## 二、Hive

1. [Hive 简介及核心概念](notes/Hive简介及核心概念.md)
2. [Linux 环境下 Hive 的安装部署](notes/installation/Linux环境下Hive的安装部署.md)
4. [Hive CLI 和 Beeline 命令行的基本使用](notes/HiveCLI和Beeline命令行的基本使用.md)
6. [Hive 常用 DDL 操作](notes/Hive常用DDL操作.md)
7. [Hive 分区表和分桶表](notes/Hive分区表和分桶表.md)
8. [Hive 视图和索引](notes/Hive视图和索引.md)
9. [Hive 常用 DML 操作](notes/Hive常用DML操作.md)
10. [Hive 数据查询详解](notes/Hive数据查询详解.md)

## 三、Spark

**Spark Core :**

1. [Spark 简介](notes/Spark简介.md)
2. [Spark 开发环境搭建](notes/installation/Spark开发环境搭建.md)
4. [弹性式数据集 RDD](notes/Spark_RDD.md)
5. [RDD 常用算子详解](notes/Spark_Transformation和Action算子.md)
5. [Spark 运行模式与作业提交](notes/Spark部署模式与作业提交.md)
6. [Spark 累加器与广播变量](notes/Spark累加器与广播变量.md)
7. [基于 Zookeeper 搭建 Spark 高可用集群](notes/installation/Spark集群环境搭建.md)

**Spark SQL :**

1. [DateFrame 和 DataSet ](notes/SparkSQL_Dataset和DataFrame简介.md)
2. [Structured API 的基本使用](notes/Spark_Structured_API的基本使用.md)
3. [Spark SQL 外部数据源](notes/SparkSQL外部数据源.md)
4. [Spark SQL 常用聚合函数](notes/SparkSQL常用聚合函数.md)
5. [Spark SQL JOIN 操作](notes/SparkSQL联结操作.md)

**Spark Streaming ：**

1. [Spark Streaming 简介](notes/Spark_Streaming与流处理.md)
2. [Spark Streaming 基本操作](notes/Spark_Streaming基本操作.md)
3. [Spark Streaming 整合 Flume](notes/Spark_Streaming整合Flume.md)
4. [Spark Streaming 整合 Kafka](notes/Spark_Streaming整合Kafka.md)

## 四、Storm

1. [Storm 和流处理简介](notes/Storm和流处理简介.md)
2. [Storm 核心概念详解](notes/Storm核心概念详解.md)
3. [Storm 单机环境搭建](notes/installation/Storm单机环境搭建.md)
4. [Storm 集群环境搭建](notes/installation/Storm集群环境搭建.md)
5. [Storm 编程模型详解](notes/Storm编程模型详解.md)
6. [Storm 项目三种打包方式对比分析](notes/Storm三种打包方式对比分析.md)
7. [Storm 集成 Redis 详解](notes/Storm集成Redis详解.md)
8. [Storm 集成 HDFS/HBase](notes/Storm集成HBase和HDFS.md)
9. [Storm 集成 Kafka](notes/Storm集成Kakfa.md)

## 五、Flink

1. [Flink 核心概念综述](notes/Flink核心概念综述.md)
2. [Flink 开发环境搭建](notes/Flink开发环境搭建.md)
3. [Flink Data Source](notes/Flink_Data_Source.md)
4. [Flink Data Transformation](notes/Flink_Data_Transformation.md)
4. [Flink Data Sink](notes/Flink_Data_Sink.md)
6. [Flink 窗口模型](notes/Flink_Windows.md)
7. [Flink 状态管理与检查点机制](notes/Flink状态管理与检查点机制.md)
8. [Flink Standalone 集群部署](notes/installation/Flink_Standalone_Cluster.md)


## 六、HBase

1. [Hbase 简介](notes/Hbase简介.md)
2. [HBase 系统架构及数据结构](notes/Hbase系统架构及数据结构.md)
3. [HBase 基本环境搭建 (Standalone /pseudo-distributed mode)](notes/installation/HBase单机环境搭建.md)
4. [HBase 集群环境搭建](notes/installation/HBase集群环境搭建.md)
5. [HBase 常用 Shell 命令](notes/Hbase_Shell.md)
6. [HBase Java API](notes/Hbase_Java_API.md)
7. [HBase 过滤器详解](notes/Hbase过滤器详解.md)
8. [HBase 协处理器详解](notes/Hbase协处理器详解.md)
9. [HBase 容灾与备份](notes/Hbase容灾与备份.md)
10. [HBase的 SQL 中间层 —— Phoenix](notes/Hbase的SQL中间层_Phoenix.md)
11. [Spring/Spring Boot 整合 Mybatis + Phoenix](notes/Spring+Mybtais+Phoenix整合.md)

## 七、Kafka

1. [Kafka 简介](notes/Kafka简介.md)
2. [基于 Zookeeper 搭建 Kafka 高可用集群](notes/installation/基于Zookeeper搭建Kafka高可用集群.md)
3. [Kafka 生产者详解](notes/Kafka生产者详解.md)
4. [Kafka 消费者详解](notes/Kafka消费者详解.md)
5. [深入理解 Kafka 副本机制](notes/Kafka深入理解分区副本机制.md)

## 八、Zookeeper

1. [Zookeeper 简介及核心概念](notes/Zookeeper简介及核心概念.md)
2. [Zookeeper 单机环境和集群环境搭建](notes/installation/Zookeeper单机环境和集群环境搭建.md) 
3. [Zookeeper 常用 Shell 命令](notes/Zookeeper常用Shell命令.md)
4. [Zookeeper Java 客户端 —— Apache Curator](notes/Zookeeper_Java客户端Curator.md)
5. [Zookeeper  ACL 权限控制](notes/Zookeeper_ACL权限控制.md)

## 九、Flume

1. [Flume 简介及基本使用](notes/Flume简介及基本使用.md)
2. [Linux 环境下 Flume 的安装部署](notes/installation/Linux下Flume的安装.md)
3. [Flume 整合 Kafka](notes/Flume整合Kafka.md)

## 十、Sqoop

1. [Sqoop 简介与安装](notes/Sqoop简介与安装.md)
2. [Sqoop 的基本使用](notes/Sqoop基本使用.md)

## 十一、Azkaban

1. [Azkaban 简介](notes/Azkaban简介.md)
2. [Azkaban3.x 编译及部署](notes/installation/Azkaban_3.x_编译及部署.md)
3. [Azkaban Flow 1.0 的使用](notes/Azkaban_Flow_1.0_的使用.md)
4. [Azkaban Flow 2.0 的使用](notes/Azkaban_Flow_2.0_的使用.md)

## 十二、Scala

1. [Scala 简介及开发环境配置](notes/Scala简介及开发环境配置.md)
2. [基本数据类型和运算符](notes/Scala基本数据类型和运算符.md)
3. [流程控制语句](notes/Scala流程控制语句.md)
4. [数组 —— Array](notes/Scala数组.md)
5. [集合类型综述](notes/Scala集合类型.md)
6. [常用集合类型之 —— List & Set](notes/Scala列表和集.md)
7. [常用集合类型之 —— Map & Tuple](notes/Scala映射和元组.md)
8. [类和对象](notes/Scala类和对象.md)
9. [继承和特质](notes/Scala继承和特质.md)
10. [函数 & 闭包 & 柯里化](notes/Scala函数和闭包.md)
11. [模式匹配](notes/Scala模式匹配.md)
12. [类型参数](notes/Scala类型参数.md)
13. [隐式转换和隐式参数](notes/Scala隐式转换和隐式参数.md)

## 十三、公共内容

1. [大数据应用常用打包方式](notes/大数据应用常用打包方式.md)

<br>

## :bookmark_tabs: 后  记

[资料分享与开发工具推荐](notes/资料分享与工具推荐.md)

<br>

<div align="center">
	<a href = "https://blog.csdn.net/m0_37809146"> 
	<img width="200px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/blog-logo.png"/> 
	</a> 
</div>
<div align="center"> <a  href = "https://blog.csdn.net/m0_37809146"> 欢迎关注我的博客：https://blog.csdn.net/m0_37809146</a> </div>
