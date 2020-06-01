# Hadoop分布式文件系统——HDFS

<nav>
<a href="#一介绍">一、介绍</a><br/>
<a href="#二HDFS-设计原理">二、HDFS 设计原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-HDFS-架构">2.1 HDFS 架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-文件系统命名空间">2.2 文件系统命名空间</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-数据复制">2.3 数据复制</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-数据复制的实现原理">2.4 数据复制的实现原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25--副本的选择">2.5  副本的选择</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-架构的稳定性">2.6 架构的稳定性</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-心跳机制和重新复制">1. 心跳机制和重新复制</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-数据的完整性">2. 数据的完整性</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3元数据的磁盘故障">3.元数据的磁盘故障</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#4支持快照">4.支持快照</a><br/>
<a href="#三HDFS-的特点">三、HDFS 的特点</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-高容错">3.1 高容错</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-高吞吐量">3.2 高吞吐量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33--大文件支持">3.3  大文件支持</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-简单一致性模型">3.3 简单一致性模型</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-跨平台移植性">3.4 跨平台移植性</a><br/>
<a href="#附图解HDFS存储原理">附：图解HDFS存储原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-HDFS写数据原理">1. HDFS写数据原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-HDFS读数据原理">2. HDFS读数据原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-HDFS故障类型和其检测方法">3. HDFS故障类型和其检测方法</a><br/>
</nav>



## 一、介绍

**HDFS** （**Hadoop Distributed File System**）是 Hadoop 下的分布式文件系统，具有高容错、高吞吐量等特性，可以部署在低成本的硬件上。



## 二、HDFS 设计原理

<div align="center"> <img width="600px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfsarchitecture.png"/> </div>

### 2.1 HDFS 架构

HDFS 遵循主/从架构，由单个 NameNode(NN) 和多个 DataNode(DN) 组成：

- **NameNode** : 负责执行有关 ` 文件系统命名空间 ` 的操作，例如打开，关闭、重命名文件和目录等。它同时还负责集群元数据的存储，记录着文件中各个数据块的位置信息。
- **DataNode**：负责提供来自文件系统客户端的读写请求，执行块的创建，删除等操作。



### 2.2 文件系统命名空间

HDFS 的 ` 文件系统命名空间 ` 的层次结构与大多数文件系统类似 (如 Linux)， 支持目录和文件的创建、移动、删除和重命名等操作，支持配置用户和访问权限，但不支持硬链接和软连接。`NameNode` 负责维护文件系统名称空间，记录对名称空间或其属性的任何更改。



### 2.3 数据复制

由于 Hadoop 被设计运行在廉价的机器上，这意味着硬件是不可靠的，为了保证容错性，HDFS 提供了数据复制机制。HDFS 将每一个文件存储为一系列**块**，每个块由多个副本来保证容错，块的大小和复制因子可以自行配置（默认情况下，块大小是 128M，默认复制因子是 3）。

<div align="center"> <img width="600px" src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfsdatanodes.png"/> </div>

### 2.4 数据复制的实现原理

大型的 HDFS 实例在通常分布在多个机架的多台服务器上，不同机架上的两台服务器之间通过交换机进行通讯。在大多数情况下，同一机架中的服务器间的网络带宽大于不同机架中的服务器之间的带宽。因此 HDFS 采用机架感知副本放置策略，对于常见情况，当复制因子为 3 时，HDFS 的放置策略是：

在写入程序位于 `datanode` 上时，就优先将写入文件的一个副本放置在该 `datanode` 上，否则放在随机 `datanode` 上。之后在另一个远程机架上的任意一个节点上放置另一个副本，并在该机架上的另一个节点上放置最后一个副本。此策略可以减少机架间的写入流量，从而提高写入性能。

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-机架.png"/> </div>

如果复制因子大于 3，则随机确定第 4 个和之后副本的放置位置，同时保持每个机架的副本数量低于上限，上限值通常为 `（复制系数 - 1）/机架数量 + 2`，需要注意的是不允许同一个 `dataNode` 上具有同一个块的多个副本。



### 2.5  副本的选择

为了最大限度地减少带宽消耗和读取延迟，HDFS 在执行读取请求时，优先读取距离读取器最近的副本。如果在与读取器节点相同的机架上存在副本，则优先选择该副本。如果 HDFS 群集跨越多个数据中心，则优先选择本地数据中心上的副本。



### 2.6 架构的稳定性

#### 1. 心跳机制和重新复制

每个 DataNode 定期向 NameNode 发送心跳消息，如果超过指定时间没有收到心跳消息，则将 DataNode 标记为死亡。NameNode 不会将任何新的 IO 请求转发给标记为死亡的 DataNode，也不会再使用这些 DataNode 上的数据。 由于数据不再可用，可能会导致某些块的复制因子小于其指定值，NameNode 会跟踪这些块，并在必要的时候进行重新复制。

#### 2. 数据的完整性

由于存储设备故障等原因，存储在 DataNode 上的数据块也会发生损坏。为了避免读取到已经损坏的数据而导致错误，HDFS 提供了数据完整性校验机制来保证数据的完整性，具体操作如下：

当客户端创建 HDFS 文件时，它会计算文件的每个块的 ` 校验和 `，并将 ` 校验和 ` 存储在同一 HDFS 命名空间下的单独的隐藏文件中。当客户端检索文件内容时，它会验证从每个 DataNode 接收的数据是否与存储在关联校验和文件中的 ` 校验和 ` 匹配。如果匹配失败，则证明数据已经损坏，此时客户端会选择从其他 DataNode 获取该块的其他可用副本。

#### 3.元数据的磁盘故障

`FsImage` 和 `EditLog` 是 HDFS 的核心数据，这些数据的意外丢失可能会导致整个 HDFS 服务不可用。为了避免这个问题，可以配置 NameNode 使其支持 `FsImage` 和 `EditLog` 多副本同步，这样 `FsImage` 或 `EditLog` 的任何改变都会引起每个副本 `FsImage` 和 `EditLog` 的同步更新。

#### 4.支持快照

快照支持在特定时刻存储数据副本，在数据意外损坏时，可以通过回滚操作恢复到健康的数据状态。



## 三、HDFS 的特点

### 3.1 高容错

由于 HDFS 采用数据的多副本方案，所以部分硬件的损坏不会导致全部数据的丢失。

### 3.2 高吞吐量

HDFS 设计的重点是支持高吞吐量的数据访问，而不是低延迟的数据访问。

### 3.3  大文件支持

HDFS 适合于大文件的存储，文档的大小应该是是 GB 到 TB 级别的。

### 3.3 简单一致性模型

HDFS 更适合于一次写入多次读取 (write-once-read-many) 的访问模型。支持将内容追加到文件末尾，但不支持数据的随机访问，不能从文件任意位置新增数据。

### 3.4 跨平台移植性

HDFS 具有良好的跨平台移植性，这使得其他大数据计算框架都将其作为数据持久化存储的首选方案。



## 附：图解HDFS存储原理

> 说明：以下图片引用自博客：[翻译经典 HDFS 原理讲解漫画](https://blog.csdn.net/hudiefenmu/article/details/37655491)

### 1. HDFS写数据原理

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-write-1.jpg"/> </div>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-write-2.jpg"/> </div>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-write-3.jpg"/> </div>



### 2. HDFS读数据原理

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-read-1.jpg"/> </div>



### 3. HDFS故障类型和其检测方法

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-tolerance-1.jpg"/> </div>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-tolerance-2.jpg"/> </div>



**第二部分：读写故障的处理**

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-tolerance-3.jpg"/> </div>



**第三部分：DataNode 故障处理**

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-tolerance-4.jpg"/> </div>



**副本布局策略**：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hdfs-tolerance-5.jpg"/> </div>



## 参考资料

1. [Apache Hadoop 2.9.2 > HDFS Architecture](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
2. Tom White . hadoop 权威指南 [M] . 清华大学出版社 . 2017.
3. [翻译经典 HDFS 原理讲解漫画](https://blog.csdn.net/hudiefenmu/article/details/37655491)



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>