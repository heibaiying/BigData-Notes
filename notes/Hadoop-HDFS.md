# 分布式文件存储系统——HDFS

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
<a href="#四HDFS-shell">四、HDFS shell</a><br/>
<a href="#五HDFS-API">五、HDFS API</a><br/>
<a href="#附图解HDFS存储原理">附：图解HDFS存储原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-HDFS写数据原理">1. HDFS写数据原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-HDFS读数据原理">2. HDFS读数据原理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-HDFS故障类型和其检测方法">3. HDFS故障类型和其检测方法</a><br/>
</nav>



## 一、介绍

**HDFS** （**Hadoop Distributed File System**）是一种分布式文件系统，具有**高容错**、**高吞吐量**等特性，可以部署在**低成本**的硬件上。



## 二、HDFS 设计原理

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfsarchitecture.png"/> </div>

### 2.1 HDFS 架构

HDFS 具有主/从架构，由单个NameNode （NN）和 多个 DataNode (DN) 组成：

- NameNode : 负责执行文件**系统命名空间**的操作，如打开，关闭和重命名文件、目录等，它还负责集群元数据的存储，记录着每个文件中各个块所在数据节点的信息。
- DataNode：负责提供来自文件系统客户端的读写请求，执行块的创建，删除等操作。



### 2.2 文件系统命名空间

HDFS 系统命名空间的层次结构与现在大多数文件系统类似（如 windows）, 可以进行目录、文件的创建、移动、删除和重命名等操作，支持用户配置和访问权限配置，但不支持硬链接和软连接。

NameNode 负责维护文件系统名称空间,记录对名称空间或其属性的任何更改。



### 2.3 数据复制

由于hadoop设计运行在廉价的机器上，这意味着硬件是不可靠的，为了保证高容错，数据复制孕育而生。

HDFS 它将每一个文件存储为一系列**块**，复制文件的块以实现容错，块大小和复制因子可根据文件进行配置（默认块大小是128M,默认复制因子是3）。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfsdatanodes.png"/> </div>

### 2.4 数据复制的实现原理

大型HDFS实例在通常分布在多个机架上的计算机群集上运行。不同机架中两个节点之间的通信必须通过交换机。在大多数情况下，同一机架中的计算机之间的网络带宽大于不同机架中的计算机之间的网络带宽。

HDFS采用机架感知副本放置策略，对于常见情况，当复制因子为3时，HDFS的放置策略是：

在编写器位于datanode上时，将一个副本放在本地计算机上，否则放在随机datanode上；在另一个（远程）机架上的节点上放置另一个副本，最后一个在同一个远程机架中的另一个节点上。此策略可以减少机架间写入流量，从而提高写入性能。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-机架.png"/> </div>

如果复制因子大于3，则随机确定第4个和以下副本的放置，同时保持每个机架的副本数量低于上限，上限值通常为`（复制系数 - 1）/机架数量 + 2`，但是不允许同一个dataNode具有同一块的多个副本。



### 2.5  副本的选择

为了最大限度地减少全局带宽消耗和读取延迟，HDFS尝试满足最接近读取器的副本的读取请求。如果在与读取器节点相同的机架上存在副本，则该副本首选满足读取请求。如果HDFS群集跨越多个数据中心，则本地数据中心的副本优先于任何远程副本。



### 2.6 架构的稳定性

#### 1. 心跳机制和重新复制

每个DataNode定期向NameNode发送心跳消息，如果超过指定时间没有收到心跳消息，则将DataNode标记为死亡。NameNode不会将任何新的IO请求转发给标记为死亡的DataNode，且标记为死亡的DataNode上的数据也不在可用。 由于数据不再可用，可能会导致某些块的复制因子小于其指定值，NameNode会跟踪这些需要复制的块，并在必要的时候进行复制。

#### 2. 数据的完整性

由于存储设备中故障等原因，存储在DataNode获取的数据块可能会发生此损坏。

当客户端创建HDFS文件时，它会计算文件每个块的`校验和`，并将这些`校验和`存储在同一HDFS命名空间中的单独隐藏文件中。当客户端检索文件内容时，它会验证从每个DataNode接收的数据是否与存储在关联校验和文件中的`校验和`匹配。如果没有，则客户端可以选择从另一个DataNode中检索该块。

#### 3.元数据的磁盘故障

`FsImage`和`EditLog`是HDFS架构的中心数据。这些数据的失效会引起HDFS实例失效。因为这个原因，可以配置NameNode使其支持`FsImage`和`EditLog`多副本同步，使得`FsImage`或`EditLog`的任何改变会引起每一份`FsImage`和`EditLog`同步更新。

#### 4.支持快照

快照支持在特定时刻存储数据副本，以便可以将损坏的HDFS实例回滚到先前良好时间点。



## 三、HDFS 的特点

### 3.1 高容错

由于HDFS 采用数据的多副本方案、所以哪怕部分硬件的损坏，也不会导致数据的丢失。

### 3.2 高吞吐量

HDFS是被设计用于批量处理而非用户交互。设计的重点是高吞吐量访问而不是低延迟数据访问。

### 3.3  大文件支持

在HDFS上运行的应用程序具有大型数据集。一个典型文档在HDFS是GB到TB级别的。因此，HDFS被设计为支持大文件。

### 3.3 简单一致性模型

HDFS更适合于一次写入多次读取（write-once-read-many）的访问模型。支持将内容附加到文件末尾，但无法在任意点更新。此假设简化了数据一致性问题，并实现了高吞吐量数据访问。

### 3.4 跨平台移植性

HDFS的设计便于从一个平台移植到另一个平台。这有助于HDFS作为大数据首选存储方案。



## 四、HDFS shell



## 五、HDFS API



## 附：图解HDFS存储原理

说明：本小结图片引用自博客[翻译经典 HDFS 原理讲解漫画](https://blog.csdn.net/hudiefenmu/article/details/37655491)

### 1. HDFS写数据原理

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-write-1.jpg"/> </div>

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-write-2.jpg"/> </div>

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-write-3.jpg"/> </div>



### 2. HDFS读数据原理

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-read-1.jpg"/> </div>



### 3. HDFS故障类型和其检测方法

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-tolerance-1.jpg"/> </div>

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-tolerance-2.jpg"/> </div>



**第二部分：读写故障的处理**

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-tolerance-3.jpg"/> </div>



**第三部分：DataNode故障处理**

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-tolerance-4.jpg"/> </div>



**副本布局策略**：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hdfs-tolerance-5.jpg"/> </div>



## 参考资料

1. [Apache Hadoop 2.9.2 > HDFS Architecture](http://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)
2. Tom White. hadoop权威指南 [M]. 清华大学出版社, 2017.
3. [翻译经典 HDFS 原理讲解漫画](https://blog.csdn.net/hudiefenmu/article/details/37655491)

