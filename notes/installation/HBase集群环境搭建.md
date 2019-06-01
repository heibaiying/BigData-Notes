# HBase集群环境配置

## 一、集群规划

这里搭建一个3节点的HBase集群，其中三台主机上均为`Regin Server`。同时为了保证高可用，除了在hadoop001上部署主`Master`服务外，还在hadoop002上署备用的`Master`服务，Master服务由Zookeeper集群进行协调管理，如果主`Master`不可用，则备用`Master`会成为新的主`Master`。

![hbase集群规划](D:\BigData-Notes\pictures\hbase集群规划.png)

## 二、前置条件

HBase的运行需要依赖JDK和Hadoop，HBase 2.0+需要安装JDK 1.8+ 。同时为了保证高可用，这里我们不采用HBase内置的Zookeeper，而采用外置的Zookeeper集群。相关搭建步骤可以参阅：

- [Linux环境下JDK安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)
- [Zookeeper单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper单机环境和集群环境搭建.md)
- [Hadoop集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Hadoop集群环境搭建.md)



## 三、集群搭建

### 3.1 下载并解压

下载并解压，官方下载地址：https://hbase.apache.org/downloads.html

```shell
# tar -zxvf hbase-2.1.4-bin.tar.gz
```

### 3.2 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export HBASE_HOME=/usr/app/hbase-2.1.4
export PATH=$HBASE_HOME/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
# source /etc/profile
```

### 3.3 集群配置

进入`${HBASE_HOME}/conf`目录下，修改配置：

#### 1. hbase-env.sh 

```shell
# 配置JDK安装位置
export JAVA_HOME=/usr/java/jdk1.8.0_201
# 不使用内置的zookeeper服务
export HBASE_MANAGES_ZK=false
```

#### 2. hbase-site.xml

```xml
<configuration>
    <property>
        <!-- 指定hbase以分布式集群的方式运行 -->
        <name>hbase.cluster.distributed</name>
        <value>true</value>
    </property>
    <property>
        <!-- 指定hbase在HDFS上的存储位置 -->
        <name>hbase.rootdir</name>
        <value>hdfs://hadoop001:8020/hbase</value>
    </property>
    <property>
        <!-- 指定zookeeper的地址-->
        <name>hbase.zookeeper.quorum</name>
        <value>hadoop001:2181,hadoop002:2181,hadoop003:2181</value>
    </property>
</configuration>
```

#### 3. regionservers

```
hadoop001
hadoop002
hadoop003
```

#### 4. backup-masters

```
hadoop002
```

` backup-masters`这个文件是不存在的，需要新建，主要用来指明备用的master节点，可以是多个，这里我们以1个为例。

### 3.4 HDFS客户端配置

这里有一个可选的配置：如果您在Hadoop集群上进行了HDFS客户端配置的更改，比如将副本系数`dfs.replication`设置成5，则必须使用以下方法之一来使HBase知道，否则HBase将依旧使用默认的副本系数3来创建文件：

> 1. Add a pointer to your `HADOOP_CONF_DIR` to the `HBASE_CLASSPATH` environment variable in *hbase-env.sh*.
> 2. Add a copy of *hdfs-site.xml* (or *hadoop-site.xml*) or, better, symlinks, under *${HBASE_HOME}/conf*, or
> 3. if only a small set of HDFS client configurations, add them to *hbase-site.xml*.

以上是官方文档的说明，这里解释一下：

**第一种** ：将Hadoop配置文件的位置信息添加到`hbase-env.sh`的`HBASE_CLASSPATH` 属性，示例如下：

```shell
export HBASE_CLASSPATH=usr/app/hadoop-2.6.0-cdh5.15.2/etc/hadoop
```

**第二种** ：将Hadoop的` hdfs-site.xml`或`hadoop-site.xml` 拷贝到  `${HBASE_HOME}/conf `目录下，或者通过符号链接的方式。如果采用这种方式的话，建议将两者都拷贝或建立符号链接，示例如下：

```shell
# 拷贝
cp core-site.xml hdfs-site.xml /usr/app/hbase-1.2.0-cdh5.15.2/conf/
# 使用符号链接
ln -s   /usr/app/hadoop-2.6.0-cdh5.15.2/etc/hadoop/core-site.xml
ln -s   /usr/app/hadoop-2.6.0-cdh5.15.2/etc/hadoop/hdfs-site.xml
```

> 注：`hadoop-site.xml`这个配置文件现在叫做`core-site.xml`

**第三种** ：如果你只有少量更改，那么直接配置到`hbase-site.xml`中即可。



### 3.5 安装包分发

将HBase的安装包分发到其他服务器，分发后建议在这两台服务器上也配置一下HBase的环境变量。

```shell
scp -r /usr/app/hbase-1.2.0-cdh5.15.2/  hadoop002:usr/app/
scp -r /usr/app/hbase-1.2.0-cdh5.15.2/  hadoop003:usr/app/
```



## 四、启动集群

### 4.1 启动ZooKeeper集群

分别到三台服务器上启动ZooKeeper服务：

```shell
 zkServer.sh start
```

### 4.2 启动Hadoop集群

```shell
# 启动dfs服务
start-dfs.sh
# 启动yarn服务
start-yarn.sh
```

### 4.3 启动HBase集群

进入hadoop001的`${HBASE_HOME}/bin`，使用以下命令启动HBase集群。执行此命令后，会在hadoop001上启动`Master`服务，在hadoop002上启动备用`Master`服务，在`regionservers`文件中配置的所有节点启动`region server`服务。

```shell
start-hbase.sh
```



### 4.5 查看服务

访问HBase的Web-UI界面，这里我安装的HBase版本为1.2，访问端口为`60010`，如果你安装的是2.0以上的版本，则访问端口号为`16010`。可以看到`Master`在hadoop001上，三个`Regin Servers`分别在hadoop001，hadoop002，和hadoop003上，并且还有一个`Backup Matser` 服务在 hadoop002上。

![hbase-集群搭建1](D:\BigData-Notes\pictures\hbase-集群搭建1.png)

<br/>

hadoop002 上的 HBase出于备用状态：

<br/>

![hbase-集群搭建2](D:\BigData-Notes\pictures\hbase-集群搭建2.png)