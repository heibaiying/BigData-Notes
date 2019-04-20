# Kafka核心概念介绍

<nav>
<a href="#一Kafka简介">一、Kafka简介</a><br/>
<a href="#二Kafka核心概念">二、Kafka核心概念</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-Messages-And-Batches">2.1 Messages And Batches</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-Topics-And-Partitions">2.2 Topics And Partitions</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-Producers-And-Consumers">2.3 Producers And Consumers</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-Brokers-And-Clusters">2.4 Brokers And Clusters </a><br/>
</nav>


## 一、Kafka简介

ApacheKafka是一个分布式的流处理平台。它具有以下特点：

+ 支持消息的发布和订阅，类似于RabbtMQ、ActiveMQ等消息队列；
+ 支持消息的持久化存储，并通过多副本分布式的存储方案来保证消息的容错性；
+ 支持数据在线实时处理；
+ 高吞吐率，单broker可以轻松处理数千个分区以及每秒百万级的消息量；
+ 能保证消息的可靠性投递。

## 二、Kafka核心概念

### 2.1 Messages And Batches

Kafka的基本数据单元被称为message(消息)，为减少网络开销，提高效率，多个消息会被放入同一批次(Batch)中后再写入。

### 2.2 Topics And Partitions

kafka 的消息通过Topics(主题)进行分类，可以把Topics理解为关系型数据库中的表。一个主题可以被分为若干个Partitions(分区)，一个分区就是一个提交日志(commit log)。

消息以追加的方式写入分区，然后以先入先出的顺序读取。kafka通过分区来实现数据的冗余和伸缩性，分区可以分布在不同的服务器上，这意味着一个Topic可以横跨多个服务器，以提供比单个服务器更强大的性能。

由于一个Topic包含多个分区，因此无法在整个Topic范围内保证消息的顺序性，但可以保证消息在单个分区内的顺序性。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/kafka-topic.png"/> </div>

### 2.3 Producers And Consumers

#### 1. 生产者

生产者负责创建消息。一般情况下，生产者在把消息均衡地分布到在主题的所有分区上，而并不关心消息会被写到哪个分区。如果我们想要把消息写到指定的分区，通过分区器对消息键进行散列来实现。

#### 2. 消费者

消费者是消费者群组的一部分，消费者负责消费消息。消费者可以订阅一个或者多个主题，并按照消息生成的顺序来读取它们。消费者通过检查消息的偏移量(offset)来区分读取过的消息。

偏移量是一个不断递增的数值，在创建消息时，Kafka会把它添加到其中，在给定的分区里，每个消息的偏移量都是唯一的。消费者把每个分区最后读取的偏移量保存在Zookeeper或Kafka上，如果消费者关闭或者重启，它还可以重新获取该偏移量，以保证读取状态不会丢失。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/kafka-producer-consumer.png"/> </div>

一个分区只能被同一个消费者群组里面的一个消费者读取，但可以被不同消费者群组中所组成的多个消费者共同读取。多个消费者群组中消费者共同读取同一个主题时，彼此之间互不影响。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/kafka消费者.png"/> </div>

### 2.4 Brokers And Clusters 

一个独立的kafka服务器被称为broker。broker 接收来自生产者的消息，为消息设置偏移量，并提交消息到磁盘保存。broker为消费者提供服务，对读取分区的请求做出响应，返回已经提交到磁盘的消息。

broker是集群(Cluster)的组成部分。每一个集群都有一个broker同时充当了集群控制器(controller)的角色（自动从集群的活跃成员中选举出来），控制器负责管理工作，包括将分区分配给broker和监控broker。

在集群中，一个分区(Partition)从属一个broker,该broker被称为分区的首领（leader）。一个分区可以分配给多个broker,这个时候会发生分区复制。这种复制机制为分区提供了消息冗余，如果有一个broker失效，其他broker可以接管领导权。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/kafka-cluster.png"/> </div>