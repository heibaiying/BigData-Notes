# Flume 简介及基本使用

## 一、Flume简介

Apache Flume是一个分布式，高可用的数据收集系统，可以从不同的数据源收集数据，经过聚合后发送到存储系统中。在大数据场景中我们通常使用Flume来进行日志数据收集。

版本说明：

Flume 分为 NG 和 OG (1.0 之前)，NG在OG的基础上进行了完全的重构，是目前使用最为广泛的版本。下面的介绍均以NG版本为基础。

## 二、Flume架构和基本概念



![flume-architecture](D:\BigData-Notes\pictures\flume-architecture.png)

### 2.1 基本架构

上图是flume的基本架构图：

外部数据源以特定格式向Flume发送`events` (事件)。当`source`接收到`events`时，它将其存储到一个或多个`channel`。`channe`会一直保存`events`直到它被`sink`所消费。`sink`的主要功能从`channel`中读取`events`，并将其存入外部存储系统中（如HDFS）或将其转发到下一个Flume的`source`，成功后再从`channel`中移除`events`。



### 2.2 基本概念

以下概念需要记住，在之后的配置文件中会用到：

**1. Event**
Evnet是可由Flume NG传输的单一数据单元。类似于JMS和消息传递系统中的消息。一个事件由标题和正文组成：前者是键/值映射，后者是任意字节数组。

**2. Source** 

数据收集组件，从外部数据源收集数据，并存储到Channel中。

**3. Channel**

Channel是源和接收器之间事件的管道，用于临时存储数据。可以是内存也可以是持久化的文件系统，说明如下。

+ Memory Channel : 使用内存，优点是速度快，但是数据可能会丢失。如在突然宕机的情况下，内存中的数据就有丢失的风险；
+ File Channel : 使用持久化的文件系统，优点是能保证数据不丢失，但是速度慢。

**4. Sink**

Sink的主要功能从Channel中读取Evnet，并将其存入外部存储系统中（如HDFS）或将其转发到下一个Flume的Source，成功后再从channel中移除Event。

**5. Agent**

是一个独立的（JVM）进程，包含组件Source、 Channel、 Sink等组件。



### 2.3 组件种类

1. Flume提供了多达几十种类型的Source，比如`Avro Source`，`Thrift Source`，`Exec Source`，`JMS Source`等，使得我们仅仅通过配置类型和参数的方式就能从不同的数据源获取数据；

2. 与Source相比，Flume也提供了多种Sink，比如`HDFS Sink`，`Hive Sink`，`HBaseSinks`，`Avro Sink`，得益于丰富的Sink，我们也可以仅通过配置就能将收集到的数据输出到指定存储位置；

3. 同样的Flume也支持多种Channel，比如`Memory Channel`，`JDBC Channel`，`Kafka Channel`，`File Channel`等。

4. 其实对于Flume的使用，除非有特别的需求，否则通过简单的配置组合Flume内置Source，Sink，Channel就能满足我们大多数的需求，所以对于Flume的基本使用主要是写配置文件为主。

   在 Flume 的[官网](http://flume.apache.org/releases/content/1.9.0/FlumeUserGuide.html)上对所有类型的组件的配置参数均以表格的方式做了详尽的介绍，并且都有配置样例。同时不同版本的参数可能略有所不同，所以建议在使用时，选取官网对应版本的User Guide作为主要参考资料。

   

## 三、Flume架构模式

Flume 支持多种架构模式，分别介绍如下

### 3.1 multi-agent flow



![flume-multi-agent-flow](D:\BigData-Notes\pictures\flume-multi-agent-flow.png)

Flume支持跨越多个Agent的数据传递，这要求前一个Agent的Sink和下一个Agent的source都必须是`Avro`类型，Sink指向Source所在主机名（或IP地址）和端口。

### 3.2 Consolidation

![flume-consolidation](D:\BigData-Notes\pictures\flume-consolidation.png)



日志收集中常常存在大量的客户端（比如分布式web服务），Flume 支持在使用多个Agent分别收集日志，然后通过一个或者多个Agent聚合后再存储到文件系统中。

### 3.3 Multiplexing the flow

![flume-multiplexing-the-flow](D:\BigData-Notes\pictures\flume-multiplexing-the-flow.png)

Flume支持从一个source源向多个channnel，也就是向多个sink传递事件，这个操作称之为`fan out`(扇出)。默认情况下`fan out`是向所有的channel复制`event`,即所有channel收到的数据都是相同的，当然Flume也支持在source上自定义一个复用选择器(multiplexing selecto) 来实现我们自己的路由规则。



## 四、Flume配置格式

### 4.1 一个Agent定义一个数据流

在一个Agent定义一个Flow需要以下两个步骤：

1. 分别定义好Agent的sources，sinks，channels，然后将sources和sinks与通道进行绑定。需要注意的是一个source可以配置多个channel，但一个sink只能配置一个channel。

```shell
<Agent>.sources = <Source>
<Agent>.sinks = <Sink>
<Agent>.channels = <Channel1> <Channel2>

# set channel for source
<Agent>.sources.<Source>.channels = <Channel1> <Channel2> ...

# set channel for sink
<Agent>.sinks.<Sink>.channel = <Channel1>
```

2. 分别定义source，sink，channel的具体属性

```shell
<Agent>.sources.<Source>.<someProperty> = <someValue>

# properties for channels
<Agent>.channel.<Channel>.<someProperty> = <someValue>

# properties for sinks
<Agent>.sources.<Sink>.<someProperty> = <someValue>
```

### 4.2 一个Agent中定义多个数据流

一个Agent可以包含多个独立的Flow。您可以在配置中列出多个sources，sinks，channels。

```shell
# list the sources, sinks and channels for the agent
<Agent>.sources = <Source1> <Source2>
<Agent>.sinks = <Sink1> <Sink2>
<Agent>.channels = <Channel1> <Channel2>
```



## 五、Flume使用案例

介绍几个Flume的使用案例：

+ 案例一：使用监听Flume文件内容变动，将新增加的内容输出到控制台
+ 案例二：监听指定目录，将目录下新增加的文件存储到HDFS
+ 案例三：监听对应的端口，将数据发送到远程服务器，远程服务器接收输出，并输出到控制台

### 5.1 案例一

需求： 监听文件内容变动，将新增加的内容输出到控制台

实现： 使用`Exec Source`配合`tail`命令实现

#### 1. 配置

新建配置文件`exec-memory-logger.properties`,其内容如下：

```properties
#指定agent的sources,sinks,channels
a1.sources = s1  
a1.sinks = k1  
a1.channels = c1  
   
#配置sources属性
a1.sources.s1.type = exec
a1.sources.s1.command = tail -F /tmp/log.txt
a1.sources.s1.shell = /bin/bash -c

#将sources与channels进行绑定
a1.sources.s1.channels = c1
   
#配置sink 
a1.sinks.k1.type = logger

#将sinks与channels进行绑定  
a1.sinks.k1.channel = c1  
   
#配置channel类型
a1.channels.c1.type = memory
```

#### 2. 启动　

```shell
flume-ng agent \
--conf conf \
--conf-file /usr/app/apache-flume-1.6.0-cdh5.15.2-bin/examples/exec-memory-logger.properties \
--name a1 \
-Dflume.root.logger=INFO,console
```

#### 3. 测试

向文件中追加数据

![flume-example-1](D:\BigData-Notes\pictures\flume-example-1.png)

追加内容在日志中显示

![flume-example-2](D:\BigData-Notes\pictures\flume-example-2.png)



### 5.2 案例二

需求： 监听指定目录，将目录下新增加的文件存储到HDFS

实现：使用`Spooling Directory Source`和`HDFS Sink`

#### 1. 配置

```properties
#指定agent的sources,sinks,channels
a1.sources = s1  
a1.sinks = k1  
a1.channels = c1  
   
#配置sources属性
a1.sources.s1.type =spooldir  
a1.sources.s1.spoolDir =/tmp/logs
a1.sources.s1.basenameHeader = true
a1.sources.s1.basenameHeaderKey = fileName 
#将sources与channels进行绑定  
a1.sources.s1.channels =c1 

   
#配置sink 
a1.sinks.k1.type = hdfs
a1.sinks.k1.hdfs.path = /flume/events/%y-%m-%d/%H/
a1.sinks.k1.hdfs.filePrefix = %{fileName}
#生成的文件类型，默认是Sequencefile，可用DataStream，则为普通文本
a1.sinks.k1.hdfs.fileType = DataStream  
a1.sinks.k1.hdfs.useLocalTimeStamp = true
#将sinks与channels进行绑定  
a1.sinks.k1.channel = c1
   
#配置channel类型
a1.channels.c1.type = memory
```

#### 2. 启动

```shell
flume-ng agent \
--conf conf \
--conf-file /usr/app/apache-flume-1.6.0-cdh5.15.2-bin/examples/spooling-memory-hdfs.properties \
--name a1 -Dflume.root.logger=INFO,console
```

#### 3. 测试

拷贝任意文件到监听目录下。可以从日志看到文件上传到HDFS的路径

```shell
# cp log.txt logs/
```

![flume-example-3](D:\BigData-Notes\pictures\flume-example-3.png)

查看上传到HDFS上的文件内容与本地是否一致

```shell
# hdfs dfs -cat /flume/events/19-04-09/13/log.txt.1554788567801
```

![flume-example-4](D:\BigData-Notes\pictures\flume-example-4.png)





```properties
#指定agent的sources,sinks,channels
a1.sources = s1  
a1.sinks = k1  
a1.channels = c1  
   
#配置sources属性
a1.sources.s1.type = netcat
a1.sources.s1.bind = hadoop001
a1.sources.s1.port = 44444
a1.sources.s1.channels = c1
   
#配置sink 
a1.sinks.k1.type = avro
a1.sinks.k1.hostname = hadoop001
a1.sinks.k1.port = 4545
a1.sinks.k1.batch-size = 1 
a1.sinks.k1.channel = c1  
   
#配置channel类型
a1.channels.c1.type = memory
a1.channels.c1.capacity = 1000
a1.channels.c1.transactionCapacity = 100




#指定agent的sources,sinks,channels
a2.sources = s2  
a2.sinks = k2  
a2.channels = c2  
   
#配置sources属性
a2.sources.r2.type = avro
a2.sources.r2.bind = hadoop001
a2.sources.r2.port = 6666

#将sources与channels进行绑定
a2.sources.s1.channels = c2
   
#配置sink 
a2.sinks.k2.type = logger

#将sinks与channels进行绑定  
a2.sinks.k2.channel = c2 
   
#配置channel类型
a2.channels.c2.type = memory
a1.channels.c2.capacity = 1000
a1.channels.c2.transactionCapacity = 100




flume-ng agent \
--conf conf \
--conf-file /usr/app/apache-flume-1.6.0-cdh5.15.2-bin/examples/netcat-memory-avro.properties \
--name a1 -Dflume.root.logger=INFO,console


flume-ng agent \
--conf conf \
--conf-file /usr/app/apache-flume-1.6.0-cdh5.15.2-bin/examples/avro-memory-logger.properties \
--name a2 -Dflume.root.logger=INFO,console
```

