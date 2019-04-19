# 基于Zookeeper搭建Kafka高可用集群

## 一、Zookeeper集群搭建

## 二、Kafka集群搭建

### 2.1 下载解压

Kafka安装包官方下载地址：http://kafka.apache.org/downloads ，本用例下载的版本为`2.2.0`，下载命令：

```shell
# 下载
wget https://www-eu.apache.org/dist/kafka/2.2.0/kafka_2.12-2.2.0.tgz
```

这里说明一下kafka安装包的命名规则：以`kafka_2.12-2.2.0.tgz`为例，前面的2.12代表Scala的版本号（Kafka是采用Scala语言开发的），后面的2.2.0代表Kafka的版本号。

```shell
# 解压
tar -xzf kafka_2.12-2.2.0.tgz
```

### 2.2 拷贝配置文件

进入解压目录的` config`目录下 ，拷贝三份配置文件

```shell
# cp server.properties server-1.properties
# cp server.properties server-2.properties
# cp server.properties server-3.properties
```

### 2.3 修改配置

分别修改三份配置文件中的部分配置，如下：

server-1.properties：

```properties
# The id of the broker. 集群中每个节点的唯一标识
broker.id=0
# 监听地址
listeners=PLAINTEXT://hadoop001:9092
# 日志文件存放位置 
log.dirs=/usr/local/kafka-logs/00
# Zookeeper连接地址
zookeeper.connect=hadoop001:2181,hadoop001:2182,hadoop001:2183
```

server-2.properties：

```properties
broker.id=1
listeners=PLAINTEXT://hadoop001:9093
log.dirs=/usr/local/kafka-logs/01
zookeeper.connect=hadoop001:2181,hadoop001:2182,hadoop001:2183
```

server-3.properties：

```properties
broker.id=2
listeners=PLAINTEXT://hadoop001:9094
log.dirs=/usr/local/kafka-logs/02
zookeeper.connect=hadoop001:2181,hadoop001:2182,hadoop001:2183
```

### 2.4 启动集群

分别指定不同配置文件，启动三个Kafka节点：

```shell
bin/kafka-server-start.sh config/server-1.properties
bin/kafka-server-start.sh config/server-2.properties
bin/kafka-server-start.sh config/server-3.properties
```

启动后使用jps查看进程，此时应该有三个zookeeper进程和三个kafka进程：

```shell
[root@hadoop001 kafka_2.12-2.2.0]# jps
14288 QuorumPeerMain
18385 Jps
16228 Kafka
17653 Kafka
14374 QuorumPeerMain
16826 Kafka
14446 QuorumPeerMain
```

### 2.5 创建测试主题

创建测试主题：

```shell
bin/kafka-topics.sh --create --bootstrap-server hadoop001:9092 --replication-factor 3 --partitions 1 --topic my-replicated-topic
```

查看主题信息：

```shell
bin/kafka-topics.sh --describe --bootstrap-server hadoop001:9092 --topic my-replicated-topic
```

![kafka-cluster-shell](D:\BigData-Notes\pictures\kafka-cluster-shell.png)

还可以进一步创建一个消费者和一个生产者进行测试：

```shell
# 创建生产者
bin/kafka-console-producer.sh --broker-list hadoop001:9093 --topic my-replicated-topic
```

```shell
# 创建消费者
bin/kafka-console-consumer.sh --bootstrap-server hadoop001:9094 --from-beginning --topic my-replicated-topic
```

