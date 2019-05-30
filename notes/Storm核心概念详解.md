# Storm 核心概念详解

<nav>
<a href="#一storm核心概念">一、Storm核心概念</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11--Topologies拓扑">1.1  Topologies（拓扑）</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12--Streams流">1.2  Streams（流）</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-Spouts">1.3 Spouts</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-Bolts"> 1.4 Bolts</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-Stream-groupings分组策略">1.5 Stream groupings（分组策略）</a><br/>
<a href="#二storm架构详解">二、Storm架构详解</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-nimbus进程">2.1 nimbus进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-supervisor进程">2.2 supervisor进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-zookeeper的作用">2.3 zookeeper的作用</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-worker进程">2.4 worker进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-executor线程">2.5 executor线程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-并行度">2.6 并行度</a><br/>
</nav>

## 一、Storm核心概念

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spout-bolt.png"/> </div>

### 1.1  Topologies（拓扑）

一个完整的Storm流处理程序被称为Storm topology(拓扑)。它是一个是由`Spouts` 和`Bolts`通过`stream`连接起来的有向无环图，Storm会保持每个提交到集群的topology持续地运行，从而处理源源不断的数据流，直到你将主动其杀死(kill)为止。

### 1.2  Streams（流）

`stream`是Storm中的核心概念。一个`stream`是一个无界的、以分布式方式并行创建和处理的`Tuple`序列。Tuple可以包含大多数基本类型以及自定义类型的数据。简单来说，Tuple就是流数据的实际载体，而stream就是一系列Tuple。

### 1.3 Spouts

`Spouts`是流数据的源头，一个Spout 可以向不止一个`Streams`中发送数据。`Spout`通常分为**可靠**和**不可靠**两种：可靠的` Spout`能够在失败时重新发送 Tuple, 不可靠的`Spout`一旦把Tuple 发送出去就置之不理了。

### 1.4 Bolts

`Bolts`是流数据的处理单元，它可以从一个或者多个`Streams`中接收数据，处理完成后再发射到新的`Streams`中。`Bolts`可以执行过滤(filtering)，聚合(aggregations)，连接(joins)等操作，并能与文件系统或数据库进行交互。

### 1.5 Stream groupings（分组策略）

<div align="center"> <img width="400px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/topology-tasks.png"/> </div>

`spouts`和`bolts`在集群上执行任务时，是由多个Task并行执行（如上图，每一个圆圈代表一个Task）。当一个tuple需要从Bolt A发送给Bolt B执行的时候，程序如何知道应该发送给Bolt B的哪一个Task执行呢？

这是由Stream groupings分组策略来决定的。Storm中一共有如下8个内置的 Stream Grouping。你也可以通过实现 `CustomStreamGrouping`接口来自实现自定义Stream groupings。

1. **Shuffle grouping** :  Tuples随机的分发到每个Bolt的每个Task上，每个Bolt获取到等量的Tuples。
2. **Fields grouping** :  Streams通过grouping指定的字段(field)来分组。假设通过 "user-id" 字段进行分区，那么具有相同 "user-id" 的Tuple就会发送到同一个Task；
3. **Partial Key grouping **:  Streams通过grouping中指定的字段(field)来分组，与`Fields Grouping`相似。但是对于两个下游的Bolt来说是负载均衡的，可以在输入数据不平均的情况下提供更好的优化。
4. **All grouping** :  Streams会被所有的Bolt的Tasks进行复制。由于存在数据重复处理，所以需要谨慎使用。
5. **Global grouping** :  整个Streams会进入Bolt的其中一个Task。通常会进入id最小的Task。
6. **None grouping **:  当前None grouping 和Shuffle grouping 等价，都是进行随机分发；
7. **Direct grouping **:  Direct grouping只能被用于direct streams 。使用这种方式需要由Tuple的**生产者**直接指定由哪个Task进行处理；
8. **Local or shuffle grouping** :  如果目标bolt有一个或者多个tasks在同一个worker进程中，tuple将会被shuffled到处于同一个进程的多个tasks上。否则，和普通的Shuffle Grouping行为一致。



## 二、Storm架构详解

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/Internal-Working-of-Apache-Storm.png"/> </div>

### 2.1 nimbus进程

 也叫做 master node，是storm集群工作的全局指挥官。 

1. 通过thrift接口，监听并接收client对topology的submit， 将topology代码保存到本地目录/nimbus/stormdist/下 ;
2. 为client提交的topology计算任务分配，根据集群worker资源情况，计算出topology的spoult和bolt的tasks应该如何在worker间分配，任务分配结果写入zookeeper ;
3. 通过thrift接口，监听supervisor的下载topology代码的请求，并提供下载 ;
4. 通过thrift接口，监听UI对统计信息的读取，从zookeeper上读取统计信息，返回给UI;
5. 若进程退出后，立即在本机重启，则不影响集群运行。 



### 2.2 supervisor进程

也叫做 worker node ,  storm集群的资源管理者，按需启动worker进程。 

1. 定时从zookeeper 检查是否有新topology代码未下载到本地 ，并定时删除旧topology代码 ;
2. 根据nimbus的任务分配计划，在本机按需启动1个或多个worker进程，并监控守护所有的worker进程；
3. 若进程退出，立即在本机重启，则不影响集群运行。 



### 2.3 zookeeper的作用

Nimbus和Supervisor进程都被设计为**快速失败**（遇到任何意外情况时进程自毁）和**无状态**（所有状态保存在Zookeeper或磁盘上）。  这样设计的好处就是如果它们的进程被意外销毁，那么在重新启动后，就只需要并从zookeeper上获取之前的状态数据即可，并不会造成任何影响。



### 2.4 worker进程

Storm集群的任务构造者 ，构造spoult或bolt的task实例，启动executor线程。 

1. 根据zookeeper上分配的task，在本进程中启动1个或多个executor线程，将构造好的task实例交给executor去运行;
2. 向zookeeper写入心跳 ；
3. 维持传输队列，发送tuple到其他的worker ；
4. 若进程退出，立即在本机重启，则不影响集群运行。 

### 2.5 executor线程

storm集群的任务执行者 ，循环执行task代码。

1. 执行1个或多个task（每个task对应spout或bolt的1个并行度），将输出加入到worker里的tuple队列 ；
2. 执行storm内部线程acker，负责发送消息处理状态给对应spout所在的worker。



### 2.6 并行度

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/relationships-worker-processes-executors-tasks.png"/> </div>

1个worker进程执行的是1个topology的子集（注：不会出现1个worker为多个topology服务）。1个worker进程会启动1个或多个executor线程来执行1个topology的component(spout或bolt)。因此1个运行中的topology就是由集群中多台物理机上的多个worker进程组成的。

executor是1个被worker进程启动的单独线程。每个executor会运行1个component(spout或bolt)的中的一个或者多个task。

task是最终运行spout或bolt中代码的单元。topology启动后，1个component(spout或bolt)的task数目是固定不变的，但该component使用的executor线程数可以动态调整（例如：1个executor线程可以执行该component的1个或多个task实例）。这意味着，对于1个component：`#threads<=#tasks`（即：线程数小于等于task数目）这样的情况是存在的。默认情况下task的数目等于executor线程数目，即1个executor线程只运行1个task。  

**在默认情况下**：

+ 每个worker进程默认启动一个executor线程
+ 每个executor默认启动一个task线程


## 参考资料

1. [storm documentation -> Concepts](http://storm.apache.org/releases/1.2.2/Concepts.html)

2. [Internal Working of Apache Storm](https://www.spritle.com/blogs/2016/04/04/apache-storm/)
3. [Understanding the Parallelism of a Storm Topology](http://storm.apache.org/releases/1.2.2/Understanding-the-parallelism-of-a-Storm-topology.html)
4. [Storm nimbus 单节点宕机的处理](https://blog.csdn.net/daiyutage/article/details/52049519)

