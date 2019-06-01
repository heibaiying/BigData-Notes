# Hbase系统架构及数据结构

<nav>
<a href="#一基本概念">一、基本概念</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-Row-Key-行键">2.1 Row Key (行键)</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-Column-Family列族">2.2 Column Family（列族）</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-Column-Qualifier-列限定符">2.3 Column Qualifier (列限定符)</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-Column列">2.4 Column(列)</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-Cell">2.5 Cell</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-Timestamp时间戳">2.6 Timestamp(时间戳)</a><br/>
<a href="#二存储结构">二、存储结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-Regions">2.1 Regions</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-Region-Server">2.2 Region Server</a><br/>
<a href="#三Hbase系统架构">三、Hbase系统架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-系统架构">3.1 系统架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-组件间的协作">3.2 组件间的协作</a><br/>
<a href="#四数据的读写流程简述">四、数据的读写流程简述</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#41-写入数据的流程">4.1 写入数据的流程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#42-读取数据的流程">4.2 读取数据的流程</a><br/>
</nav>

## 一、基本概念

一个典型的Hbase Table 表如下：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-webtable.png"/> </div>

### 2.1 Row Key (行键)

`Row Key`是用来检索记录的主键。想要访问HBase Table中的数据，只有以下三种方式：

+ 通过指定的`Row Key`进行访问；

+ 通过Row Key的range进行访问，即访问指定范围内的行；

+ 进行全表扫描。

`Row Key`可以是任意字符串，存储时数据按照`Row Key`的字典序进行排序。这里需要注意以下两点：

+ 因为字典序对Int排序的结果是1,10,100,11,12,13,14,15,16,17,18,19,2,20,21,…,9,91,92,93,94,95,96,97,98,99。如果你使用整型的字符串作为行键，那么为了保持整型的自然序，行键必须用0作左填充。

+ 行的一次读写操作时原子性的 (不论一次读写多少列)。



### 2.2 Column Family（列族）

HBase表中的每个列，都归属于某个列族。列族是表的Schema的一部分，所以列族需要在创建表时进行定义。列族的所有列都以列族名作为前缀，例如`courses:history`，`courses:math`都属于`courses`这个列族。



### 2.3 Column Qualifier (列限定符)

列限定符，你可以理解为是具体的列名，例如`courses:history`，`courses:math`都属于`courses`这个列族，它们的列限定符分别是`history`和`math`。需要注意的是列限定符不是表Schema的一部分，你可以在插入数据的过程中动态创建列。



### 2.4 Column(列)

HBase中的列由列族和列限定符组成，它们由`:`(冒号)进行分隔，即一个完整的列名应该表述为`列族名 ：列限定符`。



### 2.5 Cell

`Cell`是行，列族和列限定符的组合，并包含值和时间戳。你可以等价理解为关系型数据库中由指定行和指定列确定的一个单元格，但不同的是HBase中的一个单元格是由多个版本的数据组成的，每个版本的数据用时间戳进行区分。



### 2.6 Timestamp(时间戳)

HBase 中通过`row key`和`column`确定的为一个存储单元称为`Cell`。每个`Cell`都保存着同一份数据的多个版本。版本通过时间戳来索引，时间戳的类型是 64位整型，时间戳可以由HBase在数据写入时自动赋值，也可以由客户显式指定。每个`Cell`中，不同版本的数据按照时间戳倒序排列，即最新的数据排在最前面。



## 二、存储结构

### 2.1 Regions

HBase Table中的所有行按照`Row Key`的字典序排列。HBase Tables 通过行键的范围(row key range)被水平切分成多个`Region`, 一个`Region`包含了在start key 和 end key之间的所有行。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig2.png"/> </div>

每个表一开始只有一个`Region`，随着数据不断增加，`Region`会不断增大，当增大到一个阀值的时候，`Region`就会等分为两个新的`Region`。当Table中的行不断增多，就会有越来越多的`Region`。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-region-splite.png"/> </div>

`Region`是HBase中**分布式存储和负载均衡的最小单元**。这意味着不同的`Region`可以分布在不同的`Region Server`上。但一个`Region`是不会拆分到多个Server上的。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-region-dis.png"/> </div>

### 2.2 Region Server

`Region Server`运行在HDFS的DataNode上。它具有以下组件：

- **WAL(Write Ahead Log，预写日志)**：用于存储尚未进持久化存储的数据记录，以便在发生故障时进行恢复。
- **BlockCache**：读缓存。它将频繁读取的数据存储在内存中，如果存储不足，它将按照`最近最少使用原则`清除多余的数据。
- **MemStore**：写缓存。它存储尚未写入磁盘的新数据，并会在数据写入磁盘之前对其进行排序。每个Region上的每个列族都有一个MemStore。
- **HFile** ：将行数据按照Key\Values的形式存储在文件系统上。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-Region-Server.png"/> </div>



Region Server存取一个子表时，会创建一个Region对象，然后对表的每个列族创建一个`Store`实例，每个`Store`会有 0 个或多个`StoreFile`与之对应，每个`StoreFile`则对应一个`HFile`，HFile 就是实际存储在HDFS上的文件。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-hadoop.png"/> </div>



## 三、Hbase系统架构

### 3.1 系统架构

HBase系统遵循Master/Salve架构，由三种不同类型的组件组成：

**Zookeeper**

1. 保证任何时候，集群中只有一个Master；

2. 存贮所有Region的寻址入口；

3. 实时监控Region Server的状态，将Region Server的上线和下线信息实时通知给Master；

4. 存储HBase的Schema，包括有哪些Table，每个Table有哪些Column Family等信息。

**Master**

1. 为Region Server分配Region ；

2. 负责Region Server的负载均衡 ；

3. 发现失效的Region Server并重新分配其上的Region； 

4. GFS上的垃圾文件回收；

5. 处理Schema的更新请求。

**Region Server**

1. Region Server负责维护Master分配给它的Region ，并处理发送到Region上的IO请求；

2. Region Server负责切分在运行过程中变得过大的Region。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig1.png"/> </div>

### 3.2 组件间的协作

 HBase使用ZooKeeper作为分布式协调服务来维护集群中的服务器状态。 Zookeeper负责维护可用服务列表，并提供服务故障通知等服务：

+ 每个Region Server都会在ZooKeeper上创建一个临时节点，Master通过Zookeeper的Watcher机制对节点进行监控，从而可以发现新加入的Region Server或故障退出的Region Server；

+ 所有Masters会竞争性地在Zookeeper上创建同一个临时节点，由于Zookeeper只能有一个同名节点，所以必然只有一个Master能够创建成功，此时该Master就是主Master，主Master会定期向Zookeeper发送心跳。备用Masters则通过Watcher机制对主HMaster所在节点进行监听；

+ 如果主Master未能定时发送心跳，则其持有的Zookeeper会话会过期，相应的临时节点也会被删除，这会触发定义在该节点上的Watcher事件，使得备用的Master Servers得到通知。所有备用的Master Servers在接到通知后，会再次去竞争性地创建临时节点，完成主Master的选举。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig5.png"/> </div>



## 四、数据的读写流程简述

### 4.1 写入数据的流程

1. Client向Region Server提交写请求；

2. Region Server找到目标Region；

3. Region检查数据是否与Schema一致；

4. 如果客户端没有指定版本，则获取当前系统时间作为数据版本；

5. 将更新写入WAL Log；

6. 将更新写入Memstore；

7. 判断Memstore存储是否已满，如果存储已满则需要flush为Store Hfile文件。

> 更为详细写入流程可以参考：[HBase － 数据写入流程解析](http://hbasefly.com/2016/03/23/hbase_writer/)



### 4.2 读取数据的流程

以下是客户端首次读写HBase上数据的流程：

1. 客户端从Zookeeper获取`META`表所在的Region Server；

2. 客户端访问`META`表所在的Region Server，从`META`表中查询到访问行键所在的Region Server，之后客户端将缓存这些信息以及`META`表的位置；

3. 客户端从行键所在的Region Server上获取数据。

如果再次读取，客户端将从缓存中获取行键所在的Region Server。这样客户端就不需要再次查询`META`表，除非Region移动导致缓存失效，这样的话，则将会重新查询并更新缓存。

注：`META`表是HBase中一张特殊的表，它保存了所有Region的位置信息，META表自己的位置信息则存储在ZooKeeper上。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig7.png"/> </div>

> 更为详细读取数据流程参考：
>
> [HBase原理－数据读取流程解析](http://hbasefly.com/2016/12/21/hbase-getorscan/)
>
> [HBase原理－迟到的‘数据读取流程部分细节](http://hbasefly.com/2017/06/11/hbase-scan-2/)





## 参考资料

本篇文章内容主要参考自官方文档和以下两篇博客，图片也主要引用自以下两篇博客：

+ [HBase Architectural Components](https://mapr.com/blog/in-depth-look-hbase-architecture/#.VdMxvWSqqko)

+ [Hbase系统架构及数据结构](https://www.open-open.com/lib/view/open1346821084631.html)

官方文档：

+ [Apache HBase ™ Reference Guide](https://hbase.apache.org/2.1/book.html)



