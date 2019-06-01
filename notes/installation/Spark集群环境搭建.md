# 基于ZooKeeper搭建Spark高可用集群

## 一、集群规划

这里搭建一个3节点的Spark集群，其中三台主机上均部署`Worker`服务。同时为了保证高可用，除了在hadoop001上部署主`Master`服务外，还在hadoop002和hadoop003上分别部署备用的`Master`服务，Master服务由Zookeeper集群进行协调管理，如果主`Master`不可用，则备用`Master`会成为新的主`Master`。

![spark-集群规划](D:\BigData-Notes\pictures\spark集群规划.png)

## 二、前置条件

搭建Spark集群前，需要保证JDK环境、Zookeeper集群和Hadoop集群已经搭建，相关步骤可以参阅：

- [Linux环境下JDK安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)
- [Zookeeper单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper单机环境和集群环境搭建.md)
- [Hadoop集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Hadoop集群环境搭建.md)

## 三、Spark集群搭建

### 3.1 下载解压

下载所需版本的Spark，官网下载地址：http://spark.apache.org/downloads.html

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-download.png"/> </div>



下载后进行解压：

```shell
# tar -zxvf  spark-2.2.3-bin-hadoop2.6.tgz
```



### 3.2 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export SPARK_HOME=/usr/app/spark-2.2.3-bin-hadoop2.6
export  PATH=${SPARK_HOME}/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
# source /etc/profile
```

### 3.3 集群配置

进入`${SPARK_HOME}/conf`目录，拷贝配置样本进行修改：

#### 1. spark-env.sh

```she
 cp spark-env.sh.template spark-env.sh
```

```shell
# 配置JDK安装位置
JAVA_HOME=/usr/java/jdk1.8.0_201
# 配置hadoop配置文件的位置
HADOOP_CONF_DIR=/usr/app/hadoop-2.6.0-cdh5.15.2/etc/hadoop
# 配置zookeeper地址
SPARK_DAEMON_JAVA_OPTS="-Dspark.deploy.recoveryMode=ZOOKEEPER -Dspark.deploy.zookeeper.url=hadoop001:2181,hadoop002:2181,hadoop003:2181 -Dspark.deploy.zookeeper.dir=/spark"
```

#### 2. slaves

```
cp slaves.template slaves
```

配置所有Woker节点的位置：

```properties
hadoop001
hadoop002
hadoop003
```

### 3.4 安装包分发

将Spark的安装包分发到其他服务器，分发后建议在这两台服务器上也配置一下Spark的环境变量。

```shell
scp -r /usr/app/spark-2.4.0-bin-hadoop2.6/   hadoop002:usr/app/
scp -r /usr/app/spark-2.4.0-bin-hadoop2.6/   hadoop003:usr/app/
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

### 4.3 启动Spark集群

进入hadoop001的` ${SPARK_HOME}/sbin`目录下，执行下面命令启动集群。执行命令后，会在hadoop001上启动`Maser`服务，会在`slaves`配置文件中配置的所有节点上启动`Worker`服务。

```shell
start-all.sh
```

分别在hadoop002和hadoop003上执行下面的命令，启动备用的`Master`服务：

```shell
# ${SPARK_HOME}/sbin 下执行
start-master.sh
```

### 4.4 查看服务

查看Spark的Web-UI页面，端口为`8080`。此时可以看到hadoop001上的Master节点处于`ALIVE`状态，并有3个可用的`Worker`节点。

![spark-集群搭建1](D:\BigData-Notes\pictures\spark-集群搭建1.png)

而hadoop002和hadoop003上的Master节点均处于`STANDBY`状态，没有可用的`Worker`节点。

![spark-集群搭建2](D:\BigData-Notes\pictures\spark-集群搭建2.png)

![spark-集群搭建3](D:\BigData-Notes\pictures\spark-集群搭建3.png)



## 五、验证集群高可用

此时可以使用`kill`命令杀死hadoop001上的`Master`进程，此时`备用Master`会中会有一个再次成为`主Master`，我这里是hadoop002，可以看到hadoop2上的`Master`经过`RECOVERING`后成为了新的`主Master`，并且获得了全部可以用的`Workers`。

此时如果你再在hadoop001上使用`start-master.sh`启动Master，那么其会作为`备用Master`存在。

![spark-集群搭建4](D:\BigData-Notes\pictures\spark-集群搭建4.png)

Hadoop002上的`Master`成为`主Master`，并获得了全部可以用的`Workers`。

![spark-集群搭建5](D:\BigData-Notes\pictures\spark-集群搭建5.png)

## 六、提交作业

和单机环境下的提交到Yarn上的命令完全一致，这里以Spark内置的计算Pi的样例程序为例，提交命令如下：

```shell
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master yarn \
--deploy-mode client \
--executor-memory 1G \
--num-executors 10 \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100
```

