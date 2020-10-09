# 基于ZooKeeper搭建Hadoop高可用集群

<nav>
<a href="#一高可用简介">一、高可用简介</a><br/>
<a href="#二集群规划">二、集群规划</a><br/>
<a href="#三前置条件">三、前置条件</a><br/>
<a href="#四集群配置">四、集群配置</a><br/>
<a href="#五启动集群">五、启动集群</a><br/>
<a href="#六查看集群">六、查看集群</a><br/>
<a href="#七集群的二次启动">七、集群的二次启动</a><br/>
</nav>



## 一、高可用简介

Hadoop 高可用 (High Availability) 分为 HDFS 高可用和 YARN 高可用，两者的实现基本类似，但 HDFS NameNode 对数据存储及其一致性的要求比 YARN ResourceManger 高得多，所以它的实现也更加复杂，故下面先进行讲解：

### 1.1 高可用整体架构

HDFS 高可用架构如下：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/HDFS-HA-Architecture-Edureka.png"/> </div>

> *图片引用自：https://www.edureka.co/blog/how-to-set-up-hadoop-cluster-with-hdfs-high-availability/*

HDFS 高可用架构主要由以下组件所构成：

+ **Active NameNode 和 Standby NameNode**：两台 NameNode 形成互备，一台处于 Active 状态，为主 NameNode，另外一台处于 Standby 状态，为备 NameNode，只有主 NameNode 才能对外提供读写服务。

+ **主备切换控制器 ZKFailoverController**：ZKFailoverController 作为独立的进程运行，对 NameNode 的主备切换进行总体控制。ZKFailoverController 能及时检测到 NameNode 的健康状况，在主 NameNode 故障时借助 Zookeeper 实现自动的主备选举和切换，当然 NameNode 目前也支持不依赖于 Zookeeper 的手动主备切换。

+ **Zookeeper 集群**：为主备切换控制器提供主备选举支持。

+ **共享存储系统**：共享存储系统是实现 NameNode 的高可用最为关键的部分，共享存储系统保存了 NameNode 在运行过程中所产生的 HDFS 的元数据。主 NameNode 和 NameNode 通过共享存储系统实现元数据同步。在进行主备切换的时候，新的主 NameNode 在确认元数据完全同步之后才能继续对外提供服务。

+ **DataNode 节点**：除了通过共享存储系统共享 HDFS 的元数据信息之外，主 NameNode 和备 NameNode 还需要共享 HDFS 的数据块和 DataNode 之间的映射关系。DataNode 会同时向主 NameNode 和备 NameNode 上报数据块的位置信息。

### 1.2 基于 QJM 的共享存储系统的数据同步机制分析

目前 Hadoop 支持使用 Quorum Journal Manager (QJM) 或 Network File System (NFS) 作为共享的存储系统，这里以 QJM 集群为例进行说明：Active NameNode 首先把 EditLog 提交到 JournalNode 集群，然后 Standby NameNode 再从 JournalNode 集群定时同步 EditLog，当 Active NameNode  宕机后， Standby  NameNode 在确认元数据完全同步之后就可以对外提供服务。

需要说明的是向 JournalNode 集群写入 EditLog 是遵循 “过半写入则成功” 的策略，所以你至少要有 3 个 JournalNode 节点，当然你也可以继续增加节点数量，但是应该保证节点总数是奇数。同时如果有 2N+1 台 JournalNode，那么根据过半写的原则，最多可以容忍有 N 台 JournalNode 节点挂掉。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop-QJM-同步机制.png"/> </div>

### 1.3 NameNode 主备切换

NameNode 实现主备切换的流程下图所示：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop-namenode主备切换.png"/> </div>
1. HealthMonitor 初始化完成之后会启动内部的线程来定时调用对应 NameNode 的 HAServiceProtocol RPC 接口的方法，对 NameNode 的健康状态进行检测。
2. HealthMonitor 如果检测到 NameNode 的健康状态发生变化，会回调 ZKFailoverController 注册的相应方法进行处理。
3. 如果 ZKFailoverController 判断需要进行主备切换，会首先使用 ActiveStandbyElector 来进行自动的主备选举。
4. ActiveStandbyElector 与 Zookeeper 进行交互完成自动的主备选举。
5. ActiveStandbyElector 在主备选举完成后，会回调 ZKFailoverController 的相应方法来通知当前的 NameNode 成为主 NameNode 或备 NameNode。
6. ZKFailoverController 调用对应 NameNode 的 HAServiceProtocol RPC 接口的方法将 NameNode 转换为 Active 状态或 Standby 状态。

   
### 1.4 YARN高可用

YARN ResourceManager 的高可用与 HDFS NameNode 的高可用类似，但是 ResourceManager 不像 NameNode ，没有那么多的元数据信息需要维护，所以它的状态信息可以直接写到 Zookeeper 上，并依赖 Zookeeper 来进行主备选举。



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop-rm-ha-overview.png"/> </div>


## 二、集群规划

按照高可用的设计目标：需要保证至少有两个 NameNode (一主一备)  和 两个 ResourceManager (一主一备)  ，同时为满足“过半写入则成功”的原则，需要至少要有 3 个 JournalNode 节点。这里使用三台主机进行搭建，集群规划如下：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群规划.png"/> </div>


## 三、前置条件

+ 所有服务器都安装有 JDK，安装步骤可以参见：[Linux 下 JDK 的安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)；
+ 搭建好 ZooKeeper 集群，搭建步骤可以参见：[Zookeeper 单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper单机环境和集群环境搭建.md)
+ 所有服务器之间都配置好 SSH 免密登录。



## 四、集群配置

### 4.1 下载并解压

下载 Hadoop。这里我下载的是 CDH 版本 Hadoop，下载地址为：http://archive.cloudera.com/cdh5/cdh/5/

```shell
# tar -zvxf hadoop-2.6.0-cdh5.15.2.tar.gz 
```

### 4.2 配置环境变量

编辑 `profile` 文件：

```shell
# vim /etc/profile
```

增加如下配置：

```
export HADOOP_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2
export  PATH=${HADOOP_HOME}/bin:$PATH
```

执行 `source` 命令，使得配置立即生效：

```shell
# source /etc/profile
```

### 4.3 修改配置

进入 `${HADOOP_HOME}/etc/hadoop` 目录下，修改配置文件。各个配置文件内容如下：

#### 1. hadoop-env.sh

```shell
# 指定JDK的安装位置
export JAVA_HOME=/usr/java/jdk1.8.0_201/
```

#### 2.  core-site.xml

```xml
<configuration>
    <property>
        <!-- 指定 namenode 的 hdfs 协议文件系统的通信地址 -->
        <name>fs.defaultFS</name>
        <value>hdfs://hadoop001:8020</value>
    </property>
    <property>
        <!-- 指定 hadoop 集群存储临时文件的目录 -->
        <name>hadoop.tmp.dir</name>
        <value>/home/hadoop/tmp</value>
    </property>
    <property>
        <!-- ZooKeeper 集群的地址 -->
        <name>ha.zookeeper.quorum</name>
        <value>hadoop001:2181,hadoop002:2181,hadoop003:2181</value>
    </property>
    <property>
        <!-- ZKFC 连接到 ZooKeeper 超时时长 -->
        <name>ha.zookeeper.session-timeout.ms</name>
        <value>10000</value>
    </property>
</configuration>
```

#### 3. hdfs-site.xml

```xml
<configuration>
    <property>
        <!-- 指定 HDFS 副本的数量 -->
        <name>dfs.replication</name>
        <value>3</value>
    </property>
    <property>
        <!-- namenode 节点数据（即元数据）的存放位置，可以指定多个目录实现容错，多个目录用逗号分隔 -->
        <name>dfs.namenode.name.dir</name>
        <value>/home/hadoop/namenode/data</value>
    </property>
    <property>
        <!-- datanode 节点数据（即数据块）的存放位置 -->
        <name>dfs.datanode.data.dir</name>
        <value>/home/hadoop/datanode/data</value>
    </property>
    <property>
        <!-- 集群服务的逻辑名称 -->
        <name>dfs.nameservices</name>
        <value>mycluster</value>
    </property>
    <property>
        <!-- NameNode ID 列表-->
        <name>dfs.ha.namenodes.mycluster</name>
        <value>nn1,nn2</value>
    </property>
    <property>
        <!-- nn1 的 RPC 通信地址 -->
        <name>dfs.namenode.rpc-address.mycluster.nn1</name>
        <value>hadoop001:8020</value>
    </property>
    <property>
        <!-- nn2 的 RPC 通信地址 -->
        <name>dfs.namenode.rpc-address.mycluster.nn2</name>
        <value>hadoop002:8020</value>
    </property>
    <property>
        <!-- nn1 的 http 通信地址 -->
        <name>dfs.namenode.http-address.mycluster.nn1</name>
        <value>hadoop001:50070</value>
    </property>
    <property>
        <!-- nn2 的 http 通信地址 -->
        <name>dfs.namenode.http-address.mycluster.nn2</name>
        <value>hadoop002:50070</value>
    </property>
    <property>
        <!-- NameNode 元数据在 JournalNode 上的共享存储目录 -->
        <name>dfs.namenode.shared.edits.dir</name>
        <value>qjournal://hadoop001:8485;hadoop002:8485;hadoop003:8485/mycluster</value>
    </property>
    <property>
        <!-- Journal Edit Files 的存储目录 -->
        <name>dfs.journalnode.edits.dir</name>
        <value>/home/hadoop/journalnode/data</value>
    </property>
    <property>
        <!-- 配置隔离机制，确保在任何给定时间只有一个 NameNode 处于活动状态 -->
        <name>dfs.ha.fencing.methods</name>
        <value>sshfence</value>
    </property>
    <property>
        <!-- 使用 sshfence 机制时需要 ssh 免密登录 -->
        <name>dfs.ha.fencing.ssh.private-key-files</name>
        <value>/root/.ssh/id_rsa</value>
    </property>
    <property>
        <!-- SSH 超时时间 -->
        <name>dfs.ha.fencing.ssh.connect-timeout</name>
        <value>30000</value>
    </property>
    <property>
        <!-- 访问代理类，用于确定当前处于 Active 状态的 NameNode -->
        <name>dfs.client.failover.proxy.provider.mycluster</name>
        <value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
    </property>
    <property>
        <!-- 开启故障自动转移 -->
        <name>dfs.ha.automatic-failover.enabled</name>
        <value>true</value>
    </property>
</configuration>
```

#### 4. yarn-site.xml

```xml
<configuration>
    <property>
        <!--配置 NodeManager 上运行的附属服务。需要配置成 mapreduce_shuffle 后才可以在 Yarn 上运行 MapReduce 程序。-->
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>
    <property>
        <!-- 是否启用日志聚合 (可选) -->
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>
    <property>
        <!-- 聚合日志的保存时间 (可选) -->
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>86400</value>
    </property>
    <property>
        <!-- 启用 RM HA -->
        <name>yarn.resourcemanager.ha.enabled</name>
        <value>true</value>
    </property>
    <property>
        <!-- RM 集群标识 -->
        <name>yarn.resourcemanager.cluster-id</name>
        <value>my-yarn-cluster</value>
    </property>
    <property>
        <!-- RM 的逻辑 ID 列表 -->
        <name>yarn.resourcemanager.ha.rm-ids</name>
        <value>rm1,rm2</value>
    </property>
    <property>
        <!-- RM1 的服务地址 -->
        <name>yarn.resourcemanager.hostname.rm1</name>
        <value>hadoop002</value>
    </property>
    <property>
        <!-- RM2 的服务地址 -->
        <name>yarn.resourcemanager.hostname.rm2</name>
        <value>hadoop003</value>
    </property>
    <property>
        <!-- RM1 Web 应用程序的地址 -->
        <name>yarn.resourcemanager.webapp.address.rm1</name>
        <value>hadoop002:8088</value>
    </property>
    <property>
        <!-- RM2 Web 应用程序的地址 -->
        <name>yarn.resourcemanager.webapp.address.rm2</name>
        <value>hadoop003:8088</value>
    </property>
    <property>
        <!-- ZooKeeper 集群的地址 -->
        <name>yarn.resourcemanager.zk-address</name>
        <value>hadoop001:2181,hadoop002:2181,hadoop003:2181</value>
    </property>
    <property>
        <!-- 启用自动恢复 -->
        <name>yarn.resourcemanager.recovery.enabled</name>
        <value>true</value>
    </property>
    <property>
        <!-- 用于进行持久化存储的类 -->
        <name>yarn.resourcemanager.store.class</name>
        <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
    </property>
</configuration>
```

#### 5.  mapred-site.xml

```xml
<configuration>
    <property>
        <!--指定 mapreduce 作业运行在 yarn 上-->
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
</configuration>
```

#### 5. slaves

配置所有从属节点的主机名或 IP 地址，每行一个。所有从属节点上的 `DataNode` 服务和 `NodeManager` 服务都会被启动。

```properties
hadoop001
hadoop002
hadoop003
```

### 4.4 分发程序

将 Hadoop 安装包分发到其他两台服务器，分发后建议在这两台服务器上也配置一下 Hadoop 的环境变量。

```shell
# 将安装包分发到hadoop002
scp -r /usr/app/hadoop-2.6.0-cdh5.15.2/  hadoop002:/usr/app/
# 将安装包分发到hadoop003
scp -r /usr/app/hadoop-2.6.0-cdh5.15.2/  hadoop003:/usr/app/
```



## 五、启动集群

### 5.1 启动ZooKeeper

分别到三台服务器上启动 ZooKeeper 服务：

```ssh
 zkServer.sh start
```

### 5.2 启动Journalnode

分别到三台服务器的的 `${HADOOP_HOME}/sbin` 目录下，启动 `journalnode` 进程：

```shell
hadoop-daemon.sh start journalnode
```

### 5.3 初始化NameNode

在 `hadop001` 上执行 `NameNode` 初始化命令：

```
hdfs namenode -format
```

执行初始化命令后，需要将 `NameNode` 元数据目录的内容，复制到其他未格式化的 `NameNode` 上。元数据存储目录就是我们在 `hdfs-site.xml` 中使用 `dfs.namenode.name.dir` 属性指定的目录。这里我们需要将其复制到 `hadoop002` 上：

```shell
 scp -r /home/hadoop/namenode/data hadoop002:/home/hadoop/namenode/
```

### 5.4 初始化HA状态

在任意一台 `NameNode` 上使用以下命令来初始化 ZooKeeper 中的 HA 状态：

```shell
hdfs zkfc -formatZK
```

### 5.5 启动HDFS

进入到 `hadoop001` 的 `${HADOOP_HOME}/sbin` 目录下，启动 HDFS。此时 `hadoop001` 和 `hadoop002` 上的 `NameNode` 服务，和三台服务器上的 `DataNode` 服务都会被启动：

```shell
start-dfs.sh
```

### 5.6 启动YARN

进入到 `hadoop002` 的 `${HADOOP_HOME}/sbin` 目录下，启动 YARN。此时 `hadoop002` 上的 `ResourceManager` 服务，和三台服务器上的 `NodeManager` 服务都会被启动：

```SHEll
start-yarn.sh
```

需要注意的是，这个时候 `hadoop003` 上的 `ResourceManager` 服务通常是没有启动的，需要手动启动：

```shell
yarn-daemon.sh start resourcemanager
```

## 六、查看集群

### 6.1 查看进程

成功启动后，每台服务器上的进程应该如下：

```shell
[root@hadoop001 sbin]# jps
4512 DFSZKFailoverController
3714 JournalNode
4114 NameNode
3668 QuorumPeerMain
5012 DataNode
4639 NodeManager


[root@hadoop002 sbin]# jps
4499 ResourceManager
4595 NodeManager
3465 QuorumPeerMain
3705 NameNode
3915 DFSZKFailoverController
5211 DataNode
3533 JournalNode


[root@hadoop003 sbin]# jps
3491 JournalNode
3942 NodeManager
4102 ResourceManager
4201 DataNode
3435 QuorumPeerMain
```



### 6.2 查看Web UI

HDFS 和 YARN 的端口号分别为 `50070` 和 `8080`，界面应该如下：

此时 hadoop001 上的 `NameNode` 处于可用状态：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群1.png"/> </div>
而 hadoop002 上的 `NameNode` 则处于备用状态：

<br/>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群3.png"/> </div>
<br/>

hadoop002 上的 `ResourceManager` 处于可用状态：

<br/>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群4.png"/> </div>
<br/>

hadoop003 上的 `ResourceManager` 则处于备用状态：

<br/>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群5.png"/> </div>
<br/>

同时界面上也有 `Journal Manager` 的相关信息：

<br/>

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hadoop高可用集群2.png"/> </div>
## 七、集群的二次启动

上面的集群初次启动涉及到一些必要初始化操作，所以过程略显繁琐。但是集群一旦搭建好后，想要再次启用它是比较方便的，步骤如下（首选需要确保 ZooKeeper 集群已经启动）：

在 ` hadoop001` 启动 HDFS，此时会启动所有与 HDFS 高可用相关的服务，包括 NameNode，DataNode 和 JournalNode：

```shell
start-dfs.sh
```

在 `hadoop002` 启动 YARN：

```SHEll
start-yarn.sh
```

这个时候 `hadoop003` 上的 `ResourceManager` 服务通常还是没有启动的，需要手动启动：

```shell
yarn-daemon.sh start resourcemanager
```





## 参考资料

以上搭建步骤主要参考自官方文档：

+ [HDFS High Availability Using the Quorum Journal Manager](https://hadoop.apache.org/docs/r3.1.2/hadoop-project-dist/hadoop-hdfs/HDFSHighAvailabilityWithQJM.html)
+ [ResourceManager High Availability](https://hadoop.apache.org/docs/r3.1.2/hadoop-yarn/hadoop-yarn-site/ResourceManagerHA.html)

关于 Hadoop 高可用原理的详细分析，推荐阅读：

[Hadoop NameNode 高可用 (High Availability) 实现解析](https://www.ibm.com/developerworks/cn/opensource/os-cn-hadoop-name-node/index.html)



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>
