# Hadoop-YARN
<nav>
<a href="#一hadoop-yarn-简介">一、hadoop yarn 简介</a><br/>
<a href="#二YARN架构">二、YARN架构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-ResourceManager"> 1. ResourceManager</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-ApplicationMaster">2. ApplicationMaster </a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-NodeManager">3. NodeManager</a><br/>
<a href="#三YARN工作原理简述">三、YARN工作原理简述</a><br/>
<a href="#四YARN工作原理详述">四、YARN工作原理详述</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-作业提交">1. 作业提交</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-作业初始化">2. 作业初始化</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-任务分配">3. 任务分配</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#4-任务运行">4. 任务运行</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#5-进度和状态更新">5. 进度和状态更新</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#6-作业完成">6. 作业完成</a><br/>
<a href="#五提交作业到YARN上运行">五、提交作业到YARN上运行</a><br/>
</nav>

## 一、hadoop yarn 简介

**Apache YARN** (Yet Another Resource Negotiator)  在 hadoop 2 中被引入，作为hadoop集群资源管理系统。用户可以将各种各样的计算框架部署在YARN上，由YARN进行统一管理和资源的分配。

<div width="600px" align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/yarn-base.png"/> </div>



## 二、YARN架构

<div width="600px" align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/Figure3Architecture-of-YARN.png"/> </div>

####  1. ResourceManager

以主要后台进程的形式运行，它通常在专用机器上运行，在各种竞争的应用程序之间仲裁可用的集群资源。ResourceManager 会追踪集群中有多少可用的活动节点和资源，协调用户提交的哪些应用程序应该在何时获取这些资源。ResourceManager 是惟一拥有此信息的进程，所以它可通过某种共享的、安全的、多租户的方式制定分配（或者调度）决策（例如，依据应用程序优先级、队列容量、ACLs、数据位置等）。

#### 2. ApplicationMaster 

在用户提交一个应用程序时，一个称为 ApplicationMaster 的轻量型进程实例会启动来协调应用程序内的所有任务的执行。这包括监视任务，重新启动失败的任务，推测性地运行缓慢的任务，以及计算应用程序计数器值的总和。这些职责以前分配给所有作业的单个 JobTracker。ApplicationMaster 和属于它的应用程序的任务，在受 NodeManager 控制的资源容器中运行。

ApplicationMaster 可在容器内运行任何类型的任务。例如，MapReduce ApplicationMaster 请求一个容器来启动 map 或 reduce 任务，而 Giraph ApplicationMaster 请求一个容器来运行 Giraph 任务。

#### 3. NodeManager

NodeManager管理YARN集群中的每个节点。NodeManager 提供针对集群中每个节点的服务，负责节点内容器生命周期的管理、监视资源和跟踪节点健康。



## 三、YARN工作原理简述

<div width="600px" align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/yarn工作原理简图.png"/> </div>

1. Client提交作业到YARN上；

2. Resource Manager选择一个Node Manager，启动一个Container并运行Application Master实例；

3. Application Master根据实际需要向Resource Manager请求更多的Container资源（如果作业很小, 应用管理器会选择在其自己的JVM中运行任务）；

4. Application Master通过获取到的Container资源执行分布式计算。

   

## 四、YARN工作原理详述

<div width="600px" align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/yarn工作原理.png"/> </div>



#### 1. 作业提交

client  调用job.waitForCompletion方法，向整个集群提交MapReduce作业 (第1步) 。新的作业ID(应用ID)由资源管理器分配(第2步)。作业的client核实作业的输出, 计算输入的split, 将作业的资源(包括Jar包，配置文件, split信息)拷贝给HDFS(第3步)。 最后, 通过调用资源管理器的submitApplication()来提交作业(第4步)。

#### 2. 作业初始化

当资源管理器收到submitApplciation()的请求时, 就将该请求发给调度器(scheduler), 调度器分配container, 然后资源管理器在该container内启动应用管理器进程, 由节点管理器监控(第5步)。

MapReduce作业的应用管理器是一个主类为MRAppMaster的Java应用，其通过创造一些bookkeeping对象来监控作业的进度,  得到任务的进度和完成报告(第6步)。然后其通过分布式文件系统得到由客户端计算好的输入split(第7步)，然后为每个输入split创建一个map任务, 根据mapreduce.job.reduces创建reduce任务对象。

#### 3. 任务分配

如果作业很小, 应用管理器会选择在其自己的JVM中运行任务。

如果不是小作业,  那么应用管理器向资源管理器请求container来运行所有的map和reduce任务(第8步)。这些请求是通过心跳来传输的,  包括每个map任务的数据位置，比如存放输入split的主机名和机架(rack)，调度器利用这些信息来调度任务，尽量将任务分配给存储数据的节点, 或者分配给和存放输入split的节点相同机架的节点。

#### 4. 任务运行

当一个任务由资源管理器的调度器分配给一个container后，应用管理器通过联系节点管理器来启动container(第9步)。任务由一个主类为YarnChild的Java应用执行， 在运行任务之前首先本地化任务需要的资源，比如作业配置，JAR文件,  以及分布式缓存的所有文件(第10步。 最后, 运行map或reduce任务(第11步)。

YarnChild运行在一个专用的JVM中, 但是YARN不支持JVM重用。

#### 5. 进度和状态更新

YARN中的任务将其进度和状态(包括counter)返回给应用管理器, 客户端每秒(通mapreduce.client.progressmonitor.pollinterval设置)向应用管理器请求进度更新, 展示给用户。

#### 6. 作业完成

除了向应用管理器请求作业进度外,  客户端每5分钟都会通过调用waitForCompletion()来检查作业是否完成，时间间隔可以通过mapreduce.client.completion.pollinterval来设置。作业完成之后,  应用管理器和container会清理工作状态， OutputCommiter的作业清理方法也会被调用。作业的信息会被作业历史服务器存储以备之后用户核查。

本小结内容引用自博客[初步掌握Yarn的架构及原理](https://www.cnblogs.com/codeOfLife/p/5492740.html)



## 五、提交作业到YARN上运行



## 参考资料

1. [Apache Hadoop 2.9.2 > Apache Hadoop YARN](http://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/YARN.html)
2. [YARN：下一代 Hadoop 计算平台](https://www.ibm.com/developerworks/cn/data/library/bd-yarn-intro/index.html?mhq=yarn)
3. [初步掌握Yarn的架构及原理](https://www.cnblogs.com/codeOfLife/p/5492740.html)

