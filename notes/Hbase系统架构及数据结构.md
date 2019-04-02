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

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-webtable.png"/> </div>

### 2.1 Row Key (行键)

row key是用来检索记录的主键。访问Hbase table中的行，只有三种方式：

+ 通过单个row key访问

+ 通过row key的range

+ 全表扫描

Row key行键 (Row key)可以是任意字符串(最大长度是 64KB，实际应用中长度一般为 10-100bytes)，在Hbase内部，row key保存为字节数组。存储时，数据按照Row key的字典序(byte order)排序存储。设计key时，要充分排序存储这个特性，将经常一起读取的行存储放到一起。(位置相关性)

注意：

+ 字典序对int排序的结果是1,10,100,11,12,13,14,15,16,17,18,19,2,20,21,…,9,91,92,93,94,95,96,97,98,99。要保持整形的自然序，行键必须用0作左填充。

+ 行的一次读写是原子操作 (不论一次读写多少列)。



### 2.2 Column Family（列族）

hbase表中的每个列（Column），都归属与某个列族。列族是表的schema的一部分(列不是)，必须在使用表之前定义。列名都以列族作为前缀。例如courses:history，courses:math都属于courses 这个列族。



### 2.3 Column Qualifier (列限定符)

列限定符被添加到列族中，以提供给定数据的索引。给定列族`content`，列限定符可能是`content:html`，另一个可能是`content:pdf`。虽然列族在创建表时是固定的，但列限定符是可变的，并且行与行之间可能有很大差异。



### 2.4 Column(列)

HBase 中的列由列族和列限定符组成，它们由`:`（冒号）字符分隔。



### 2.5 Cell

cell是行，列族和列限定符的组合，并包含值和时间戳。



### 2.6 Timestamp(时间戳)

HBase 中通过row和columns确定的为一个存贮单元称为cell。每个 cell都保存着同一份数据的多个版本。版本通过时间戳来索引。时间戳的类型是 64位整型。时间戳可以由hbase(在数据写入时自动 )赋值，此时时间戳是精确到毫秒的当前系统时间。时间戳也可以由客户显式赋值。如果应用程序要避免数据版本冲突，就必须自己生成具有唯一性的时间戳。每个 cell中，不同版本的数据按照时间倒序排序，即最新的数据排在最前面。



## 二、存储结构

### 2.1 Regions

Hbase Table中的所有行都按照row key的字典序排列。HBase Tables 通过行键的范围（row key range）被水平切分成多个Region, 一个Region包含了在start key 和 end key之间的所有行。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig2.png"/> </div>

每个表一开始只有一个Region ，随着数据不断插入表，Region不断增大，当增大到一个阀值的时候，Region就会等分会两个新的Region。当Table中的行不断增多，就会有越来越多的Region。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-region-splite.png"/> </div>

Region是Hbase中**分布式存储和负载均衡的最小单元**。最小单元就表示不同的Region可以分布在不同的Region Server上。但一个Region是不会拆分到多个server上的。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-region-dis.png"/> </div>

### 2.2 Region Server

Region Server在HDFS data node上运行。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-hadoop.png"/> </div>

Region Server 存取一个子表时，会创建一个 Region 对象，然后对表的每个列族 (Column Family) 创建一个 Store 实例，每个 Store 都会有 0 个或多个 StoreFile 与之对应，每个 StoreFile 都会对应一个 HFile，HFile 就是实际的存储文件。因此，一个 Region 有多少个列族就有多少个 Store。

Region Server还具有以下组件：

+ WAL：Write Ahead Log（预写日志）是分布式文件系统上的文件。 WAL用于存储尚未进持久化存储的新数据，以便在发生故障时进行恢复。
+ BlockCache：是读缓存。它将频繁读取的数据存储在内存中。如果存储不足，它将按照`最近最少使用原则`清除多余的数据。
+ MemStore：是写缓存。它存储尚未写入磁盘的新数据，并会在数据写入磁盘之前对其进行排序。每个Region上的每个列族都有一个MemStore。
+ HFile将行数据按照KeyValues的形式存储在文件系统上。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hbase-Region-Server.png"/> </div>

## 三、Hbase系统架构

### 3.1 系统架构

HBase系统遵循master/salve架构，由三种不同类型的组件组成。

**Zookeeper**

1. 保证任何时候，集群中只有一个master

2. 存贮所有Region的寻址入口

3. 实时监控Region Server的状态，将Region server的上线和下线信息实时通知给Master

4. 存储Hbase的schema,包括有哪些Table，每个Table有哪些column family

**Master**

1. 为Region server分配Region 

2. 负责Region server的负载均衡

3. 发现失效的Region server并重新分配其上的Region 

4. GFS上的垃圾文件回收

5. 处理schema更新请求

**Region Server**

1. Region server维护Master分配给它的Region ，处理发到Region上的IO请求

2. Region server负责切分在运行过程中变得过大的Region

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig1.png"/> </div>

### 3.2 组件间的协作

 HBase使用ZooKeeper作为分布式协调服务来维护集群中的服务器状态。 Zookeeper维护可用服务列表，并提供服务故障通知。

+ 每个Region Server都会在ZooKeeper上创建一个临时节点，HMaster通过Zookeeper的Watcher机制监控这些节点以发现可用的Region Server和故障的Region Server；

+ Masters会竞争创建临时节点， Zookeeper确定第一个并使用它来确保只有一个主服务器处于活动状态。主Master向Zookeeper发送心跳，备用HMaster监听主HMaster故障的通知，在主HMaster发生故障的时候取而代之。

+ 如果Region Server或主HMaster未能发送心跳，则会话过期并删除相应的临时节点。这会触发定义在该节点上的Watcher事件，使得Region Server或备用Region Server得到通知。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig5.png"/> </div>



## 四、数据的读写流程简述

### 4.1 写入数据的流程

1. client向region server提交写请求

2. region server找到目标region

3. region检查数据是否与schema一致

4. 如果客户端没有指定版本，则获取当前系统时间作为数据版本

5. 将更新写入WAL log

6. 将更新写入Memstore

7. 判断Memstore存储是否已满，如果存储已满则需要flush为Store Hfile文件

> 更为详细写入流程可以参考：[HBase － 数据写入流程解析](http://hbasefly.com/2016/03/23/hbase_writer/)



### 4.2 读取数据的流程

以下是客户端首次读写Hbase 的流程：

1. 客户端从Zookeeper获取 META 表所在的Region Server。

2. 客户端访问 META 表所在的Region Server，查询META 表获取它想访问的行键（Row Key）所在的Region Server。客户端将缓存这些信息以及META表的位置。

3. 客户端端将从相应的Region Server获取行数据。

如果再次读取，客户端将使用缓存来获取META 的位置及之前的行键。这样时间久了，客户端不需要查询META表，除非Region移动所导致的缓存失效，这样的话，则将会重新查询更新缓存。

注：META 表是Hbase中一张特殊的表，它保存了Hbase中所有数据表的Region位置信息，ZooKeeper存储着META 表的位置。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/HBaseArchitecture-Blog-Fig7.png"/> </div>

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



