# 基于ZooKeeper搭建Hadoop高可用集群

## 一、Hadoop高可用

![HDFS-HA-Architecture-Edureka](D:\BigData-Notes\pictures\HDFS-HA-Architecture-Edureka.png)

> 图片引用自：https://www.edureka.co/blog/how-to-set-up-hadoop-cluster-with-hdfs-high-availability/

## 二、集群规划



## 三、前置条件

+ 所有服务器都安装有JDK，安装步骤可以参见：[Linux下JDK的安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/JDK%E5%AE%89%E8%A3%85.md)；
+ 搭建好ZooKeeper集群，搭建步骤可以参见：[Zookeeper单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper单机环境和集群环境搭建.md)

+ 所有服务器之间都配置好SSH免密登录。



## 四、集群配置

### 4.1 下载并解压

下载Hadoop。这里我下载的是CDH版本Hadoop，下载地址为：http://archive.cloudera.com/cdh5/cdh/5/

```shell
# tar -zvxf hadoop-2.6.0-cdh5.15.2.tar.gz 
```

### 4.2 配置环境变量

编辑`profile`文件：

```shell
# vim /etc/profile
```

增加如下配置：

```
export HADOOP_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2
export  PATH=${HADOOP_HOME}/bin:$PATH
```

执行`source`命令，使得配置立即生效：

```shell
# source /etc/profile
```

### 4.3 修改配置

进入`${HADOOP_HOME}/etc/hadoop`目录下，修改配置文件。各个配置文件内容如下：

#### 1. hadoop-env.sh

```shell
# 指定JDK的安装位置
export JAVA_HOME=/usr/java/jdk1.8.0_201/
```

#### 2.  core-site.xml

```xml
<configuration>
    <property>
        <!--指定namenode的hdfs协议文件系统的通信地址-->
        <name>fs.defaultFS</name>
        <value>hdfs://hadoop001:8020</value>
    </property>
    <property>
        <!--指定hadoop集群存储临时文件的目录-->
        <name>hadoop.tmp.dir</name>
        <value>/home/hadoop/tmp</value>
    </property>
    <property>
        <name>ha.zookeeper.quorum</name>
        <value>hadoop001:2181,hadoop002:2181,hadoop002:2181</value>
    </property>
    <!-- hadoop链接zookeeper的超时时长设置 -->
    <property>
        <name>ha.zookeeper.session-timeout.ms</name>
        <value>1000</value>
    </property>
</configuration>
```

#### 3. hdfs-site.xml

```xml
<configuration>
    <!-- 指定HDFS副本的数量 -->
    <property>
        <name>dfs.replication</name>
        <value>3</value>
    </property>
    <property>
        <name>dfs.namenode.name.dir</name>
        <value>/home/hadoop/namenode/data</value>
    </property>
    <property>
        <name>dfs.datanode.data.dir</name>
        <value>/home/hadoop/datanode/data</value>
    </property>
    <!-- 完全分布式集群名称 -->
    <property>
        <name>dfs.nameservices</name>
        <value>mycluster</value>
    </property>
    <!-- 集群中NameNode节点都有哪些 -->
    <property>
        <name>dfs.ha.namenodes.mycluster</name>
        <value>nn1,nn2</value>
    </property>
    <!-- nn1的RPC通信地址 -->
    <property>
        <name>dfs.namenode.rpc-address.mycluster.nn1</name>
        <value>hadoop001:8020</value>
    </property>
    <!-- nn2的RPC通信地址 -->
    <property>
        <name>dfs.namenode.rpc-address.mycluster.nn2</name>
        <value>hadoop002:8020</value>
    </property>
    <!-- nn1的http通信地址 -->
    <property>
        <name>dfs.namenode.http-address.mycluster.nn1</name>
        <value>hadoop001:50070</value>
    </property>
    <!-- nn2的http通信地址 -->
    <property>
        <name>dfs.namenode.http-address.mycluster.nn2</name>
        <value>hadoop002:50070</value>
    </property>
    <!-- 指定NameNode元数据在JournalNode上的存放位置 -->
    <property>
        <name>dfs.namenode.shared.edits.dir</name>
        <value>qjournal://hadoop001:8485;hadoop002:8485;hadoop003:8485/mycluster</value>
    </property>
    <!-- 声明journalnode服务器存储目录-->
    <property>
        <name>dfs.journalnode.edits.dir</name>
        <value>/home/hadoop/journalnode/data</value>
    </property>
    <!-- 配置隔离机制，即同一时刻只能有一台服务器对外响应 -->
    <property>
        <name>dfs.ha.fencing.methods</name>
        <value>sshfence</value>
    </property>
    <!-- 使用隔离机制时需要ssh无秘钥登录-->
    <property>
        <name>dfs.ha.fencing.ssh.private-key-files</name>
        <value>/root/.ssh/id_rsa</value>
    </property>
    <!-- 配置sshfence隔离机制超时时间 -->
    <property>
        <name>dfs.ha.fencing.ssh.connect-timeout</name>
        <value>30000</value>
    </property>
    <!-- 访问代理类：client，mycluster，active配置失败自动切换实现方式-->
    <property>
        <name>dfs.client.failover.proxy.provider.mycluster</name>
        <value>org.apache.hadoop.hdfs.server.namenode.ha.ConfiguredFailoverProxyProvider</value>
    </property>
    <property>
        <!--配置故障自动转移-->
        <name>dfs.ha.automatic-failover.enabled</name>
        <value>true</value>
    </property>
</configuration>
```

#### 4. yarn-site.xml

```xml
<configuration>
    
    <property>
        <name>yarn.nodemanager.aux-services</name>
        <value>mapreduce_shuffle</value>
    </property>

    <property>
        <name>yarn.log-aggregation-enable</name>
        <value>true</value>
    </property>

    <property>
        <name>yarn.log-aggregation.retain-seconds</name>
        <value>86400</value>
    </property>

    <!-- 开启RM高可用 -->
    <property>
        <name>yarn.resourcemanager.ha.enabled</name>
        <value>true</value>
    </property>

    <!-- 指定RM的cluster id -->
    <property>
        <name>yarn.resourcemanager.cluster-id</name>
        <value>my-yarn-cluster</value>
    </property>

    <!-- 指定RM的名字 -->
    <property>
        <name>yarn.resourcemanager.ha.rm-ids</name>
        <value>rm1,rm2</value>
    </property>

    <!-- 分别指定RM的地址 -->
    <property>
        <name>yarn.resourcemanager.hostname.rm1</name>
        <value>hadoop002</value>
    </property>

    <property>
        <name>yarn.resourcemanager.hostname.rm2</name>
        <value>hadoop003</value>
    </property>

    <property>
        <name>yarn.resourcemanager.webapp.address.rm1</name>
        <value>hadoop002:8088</value>
    </property>
    <property>
        <name>yarn.resourcemanager.webapp.address.rm2</name>
        <value>hadoop003:8088</value>
    </property>

    <!-- 指定zk集群地址 -->
    <property>
        <name>yarn.resourcemanager.zk-address</name>
        <value>hadoop001:2181,hadoop002:2181,hadoop003:2181</value>
    </property>

    <!-- 启用自动恢复 -->
    <property>
        <name>yarn.resourcemanager.recovery.enabled</name>
        <value>true</value>
    </property>

    <!-- 制定resourcemanager的状态信息存储在zookeeper集群上 -->
    <property>
        <name>yarn.resourcemanager.store.class</name>
        <value>org.apache.hadoop.yarn.server.resourcemanager.recovery.ZKRMStateStore</value>
    </property>
</configuration>
```

#### 5.  mapred-site.xml

```xml
<configuration>
    <property>
        <!--指定mapreduce作业运行在yarn上-->
        <name>mapreduce.framework.name</name>
        <value>yarn</value>
    </property>
</configuration>
```

#### 5. slaves

配置所有从属节点的主机名或IP地址，每行一个。所有从属节点上的`DataNode`服务和`NodeManager`服务都会被启动。

```properties
hadoop001
hadoop002
hadoop003
```

### 4.4 分发程序

将Hadoop安装包分发到其他两台服务器，分发后建议在这两台服务器上也配置一下Hadoop的环境变量。

```shell
# 将安装包分发到hadoop002
scp -r /usr/app/hadoop-2.6.0-cdh5.15.2/  hadoop002:/usr/app/
# 将安装包分发到hadoop003
scp -r /usr/app/hadoop-2.6.0-cdh5.15.2/  hadoop003:/usr/app/
```



## 五、启动集群

### 5.1 启动ZooKeeper

分别到三台服务器上启动ZooKeeper服务：

```ssh
 zkServer.sh start
```

### 5.2 启动Journalnode

分别到三台服务器的的`${HADOOP_HOME}/sbin`目录下，启动`journalnode`进程：

```shell
hadoop-daemon.sh start journalnode
```

### 5.3 初始化NameNode

在`hadop001`上执行`NameNode`初始化命令：

```
hdfs namenode -format
```

执行初始化命令后，需要将`NameNode`元数据目录的内容，复制到其他未格式化的`NameNode`上。元数据存储目录就是我们在`hdfs-site.xml`中使用`dfs.namenode.name.dir`属性指定的目录。这里我们需要将其复制到`hadoop002`上：

```shell
 scp -r /home/hadoop/namenode/data hadoop002:/home/hadoop/namenode/
```

### 5.4 初始化HA状态

在任意一台`NameNode`上使用以下命令初始化HA状态。此时程序会在ZooKeeper上创建一个Znode，用于存储自动故障转移系统的相关数据。这里在`hadoop001`或`hadoop002`上执行都可以，但不要两台机器都执行，因为不能创建同名的Znode。

```shell
hdfs zkfc -formatZK
```

### 5.5 启动HDFS

进入到`Hadoop001`的`${HADOOP_HOME}/sbin`目录下，启动HDFS。此时`hadoop001`和`hadoop002`上的`NameNode`服务，和三台服务器上的`DataNode`服务都会被启动：

```shell
start-dfs.sh
```

### 5.6 启动YARN

进入到`Hadoop001`的`${HADOOP_HOME}/sbin`目录下，启动YARN。此时`hadoop002`上的`ResourceManager`服务，和三台服务器上的`NodeManager`服务都会被启动。

```SHEll
start-yarn.sh
```

需要注意的是，这个时候`hadoop003`上的`ResourceManager`服务通常是没有启动的，需要手动启动：

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

HDFS和YARN的端口号分别为`50070`和`8080`，界面应该如下：

此时hadoop001上的`NameNode`处于可用状态：

![hadoop高可用集群1](D:\BigData-Notes\pictures\hadoop高可用集群1.png)

而hadoop002上的`NameNode`则处于备用状态：

<br/>

![hadoop高可用集群1](D:\BigData-Notes\pictures\hadoop高可用集群3.png)

<br/>

hadoop002上的`ResourceManager`处于可用状态：

<br/>

![hadoop高可用集群1](D:\BigData-Notes\pictures\hadoop高可用集群4.png)

<br/>

hadoop003上的`ResourceManager`则处于备用状态：

<br/>

![hadoop高可用集群1](D:\BigData-Notes\pictures\hadoop高可用集群5.png)

<br/>

同时界面上也有`Journal Manager`的相关信息：

<br/>

![hadoop高可用集群1](D:\BigData-Notes\pictures\hadoop高可用集群2.png)

