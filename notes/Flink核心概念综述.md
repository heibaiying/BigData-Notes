# Flink 核心概念综述

## 一、Flink 简介

Apache Flink 诞生于柏林工业大学的一个研究性项目，原名 StratoSphere 。2014 年，由 StratoSphere 项目孵化出 Flink，并于同年捐赠 Apache，之后成为 Apache 的顶级项目。2019 年 1 年，阿里巴巴收购了 Flink 的母公司 Data Artisans，并宣布开源内部的 Blink，Blink 是阿里巴巴基于 Flink 优化后的版本，增加了大量的新功能，并在性能和稳定性上进行了各种优化，经历过阿里内部多种复杂业务的挑战和检验。同时阿里巴巴也表示会逐步将这些新功能和特性 Merge 回社区版本的 Flink 中，因此 Flink 成为目前最为火热的大数据处理框架。

简单来说，Flink 是一个分布式的流处理框架，它能够对有界和无界的数据流进行高效的处理。Flink 的核心是流处理，当然它也能支持批处理，Flink 将批处理看成是流处理的一种特殊情况，即数据流是有明确界限的。这和 Spark Streaming 的思想是完全相反的，Spark Streaming 的核心是批处理，它将流处理看成是批处理的一种特殊情况， 即把数据流进行极小粒度的拆分，拆分为多个微批处理。

Flink 有界数据流和无界数据流：

![flink-bounded-unbounded](D:\BigData-Notes\pictures\flink-bounded-unbounded.png)

Spark Streaming 数据流的拆分：

![streaming-flow](D:\BigData-Notes\pictures\streaming-flow.png)

## 二、Flink 核心架构

Flink 采用分层的架构设计，从而保证各层在功能和职责上的清晰。如下图所示，由上而下分别是 API & Libraries 层、Runtime 核心层以及物理部署层：

![flink-stack](D:\BigData-Notes\pictures\flink-stack.png)

### 2.1 API & Libraries 层

这一层主要提供了编程 API 和 顶层类库：

+ 编程 API : 用于进行流处理的 DataStream API 和用于进行批处理的 DataSet API；
+ 顶层类库：包括用于复杂事件处理的 CEP 库；用于结构化数据查询的 SQL & Table 库，以及基于批处理的机器学习库 FlinkML 和 图形处理库 Gelly。

### 2.2 Runtime 核心层

这一层是 Flink 分布式计算框架的核心实现层，包括作业转换，任务调度，资源分配，任务执行等功能，基于这一层的实现，可以在流式引擎下同时运行流处理程序和批处理程序。

### 2.3 物理部署层

Flink 的物理部署层，用于支持在不同平台上部署运行 Flink 应用。

## 三、Flink 分层 API

在上面介绍的 API & Libraries 这一层，Flink 又进行了更为具体的划分。具体如下：

![flink-api-stack](D:\BigData-Notes\pictures\flink-api-stack.png)

按照如上的层次结构，API 的一致性由下至上依次递增，接口的表现能力由下至上依次递减，各层的核心功能如下：

### 3.1 SQL & Table API

SQL & Table API 同时适用于批处理和流处理，这意味着你可以对有界数据流和无界数据流以相同的语义进行查询，并产生相同的结果。除了基本查询外， 它还支持自定义的标量函数，聚合函数以及表值函数，可以满足多样化的查询需求。 

### 3.2 DataStream & DataSet API

DataStream &  DataSet API 是 Flink 数据处理的核心 API，支持使用 Java 语言或 Scala 语言进行调用，提供了数据读取，数据转换和数据输出等一系列常用操作的封装。

### 3.3 Stateful Stream Processing

Stateful Stream Processing 是最低级别的抽象，它通过 Process Function 函数内嵌到 DataStream API 中。 Process Function 是 Flink 提供的最底层 API，具有最大的灵活性，允许开发者对于时间和状态进行细粒度的控制。

## 四、Flink 集群架构

### 4.1  核心组件

按照上面的介绍，Flink 核心架构的第二层是 Runtime 层， 该层采用标准的 Master - Slave 结构， 其中，Master 部分又包含了三个核心组件：Dispatcher、ResourceManager 和 JobManager，而 Slave 则主要是 TaskManager 进程。它们的功能分别如下：

- **JobManagers** (也称为 *masters*) ： 整个作业 (Job) 的主要管理者，负责调度任务 (tasks)、协调检查点 (checkpoints) 、协调故障恢复等。每个 Job 至少有一个 JobManager；高可用部署下可以有多个 JobManagers，其中一个作为 *leader*，其余的则处于 *standby* 状态。
- **TaskManagers** (也称为 *workers*) ：负责子任务 (subtasks) 的执行。每个 Job 至少会有一个 TaskManager。
- **Dispatcher**：负责接收客户端的作业，并启动 JobManager。
- **ResourceManager** ：负责集群资源的协调和管理。

以 Standalone 模式为例，它们之间的运行流程如下：

![flink-standalone-cluster](D:\BigData-Notes\pictures\flink-standalone-cluster.jpg)

+ 用户通过 Client 将作业 ( Job) 提交给 Master 时，此时需要先经过 Dispatcher；
+ 当 Dispatcher 收到客户端的请求之后，会生成一个 JobManager，接着 JobManager 进程向 ResourceManager 申请资源来启动 TaskManager；
+ TaskManager 启动之后，它需要将自己注册到 ResourceManager 上。注册完成后， JobManager 再将具体的 Task 任务分发给这个 TaskManager 去执行。

### 4.2  Task & SubTask

在上面我们提到 JobManagers 负责管理的是 Task ，而 TaskManagers 负责执行的则是 SubTask，这里解释一下任务 Task 和子任务 SubTask 两者间的关系：

在执行分布式计算时，Flink 将可以链接的操作 (operators) 链接到一起，这就是 Task。之所以这样做， 是为了减少线程间切换和缓冲而导致的开销，在降低延迟的同时可以提高整体的吞吐量。 但不是所有的 operator 都可以被链接，如下 keyBy 等操作会导致网络 shuffle 和重分区，因此其就不能被链接，只能被单独作为一个 Task。  简单来说，一个 Task 就是一个可以链接的最小的操作链 (Operator Chains) 。如下图，source 和 map 算子被链接到一块，因此整个作业就只有三个 Task：

![flink-task-subtask](D:\BigData-Notes\pictures\flink-task-subtask.png)

解释完 Task ，我们在解释一下什么是 SubTask，其准确的翻译是：  *A subtask is one parallel slice of a task*，即一个 Task 可以按照其并行度拆分为多个 SubTask。如上图，source & map 具有两个并行度，KeyBy 具有两个并行度，Sink 具有一个并行度，因此整个虽然只有 3 个 Task，但是却有 5 个 SubTask，每个 SubTask 都是一个单独的线程。

Jobmanager 负责定义和拆分这些 SubTask，并将其交给 Taskmanagers 来执行。想要成功执行这些任务，Taskmanagers 还必须要具备运行这些 SubTask 所需要的计算资源和内存资源。

### 4.3  资源管理

ResourceManager 对资源的管理是以 Slot 为单位的，Slot 是一组固定大小的资源的合集。 在上面的过程中，JobManager 进程向 ResourceManager 申请资源来启动 TaskManager，申请的就是 Slot 资源，此时上面 5 个 SubTasks 与 Slots 的对应情况则可能如下：

![flink-tasks-slots](D:\BigData-Notes\pictures\flink-tasks-slots.png)

这时每个 SubTask 线程运行在一个独立的 TaskSlot， 它们共享所属的 TaskManager 进程的TCP 连接（通过多路复用技术）和心跳信息 (heartbeat messages)，从而可以降低整体的性能开销。此时看似是最好的情况，但是每个操作需要的资源都是不尽相同的，这里假设该作业 keyBy 操作所需资源的数量比 Sink 多很多 ，那么此时 Sink 所在 Slot 的资源就没有得到有效的利用。

基于这个原因，Flink 允许多个 subtasks 共享 slots，即使它们是不同 tasks 的 subtasks，但只要它们来自同一个 Job 就可以。假设上面 souce & map 和 keyBy 的并行度调整为 6，而 Slot 的数量不变，此时情况如下：

![flink-subtask-slots](D:\BigData-Notes\pictures\flink-subtask-slots.png)

可以看到一个 Task Slot 中运行了多个 SubTask 子任务，此时每个子任务仍然在一个独立的线程中执行，只不过共享一组 Sot 资源而已。那么 Flink 到底如何确定一个 Job 至少需要多少个 Slot 呢？Flink 对于这个问题的处理很简单，默认情况一个 Job 所需要的 Slot 的数量就等于其 Operation 操作的最高并行度。如下， A，B，D 操作的并行度为 4，而 C，E 操作的并行度为 2，那么此时整个 Job 就需要至少四个 Slots 来完成。通过这个机制，Flink 就可以不必去关心一个 Job 到底会被拆分为多少个 Tasks 和 SubTasks。

![flink-task-parallelism](D:\BigData-Notes\pictures\flink-task-parallelism.png)

 

### 4.4 组件通讯

Flink 的所有组件都基于 Actor System 来进行通讯。Actor system是多种角色的 actor 的容器，它提供调度，配置，日志记录等多种服务，并包含一个可以启动所有 actor 的线程池，如果 actor 是本地的，则消息通过共享内存进行共享，但如果 actor 是远程的，则通过 RPC 的调用来传递消息。

![flink-process](D:\BigData-Notes\pictures\flink-process.png)

## 五、Flink 的优点

最后来介绍一下 Flink 的优点：

+ Flink 是基于事件驱动 (Event-driven) 的应用，能够同时支持流处理和批处理；
+ 基于内存的计算，能够保证高吞吐和低延迟，具有优越的性能表现；
+ 支持精确一次 (Exactly-once) 语意，能够完美地保证一致性和正确性；
+ 分层 API ，能够满足各个层次的开发需求；
+ 支持高可用配置，支持保存点机制，能够提供安全性和稳定性上的保证；
+ 多样化的部署方式，支持本地，远端，云端等多种部署方案；
+ 具有横向扩展架构，能够按照用户的需求进行动态扩容；
+ 活跃度极高的社区和完善的生态圈的支持。



## 参考资料

+ [Dataflow Programming Model](https://ci.apache.org/projects/flink/flink-docs-release-1.9/concepts/programming-model.html)
+ [Distributed Runtime Environment](https://ci.apache.org/projects/flink/flink-docs-release-1.9/concepts/runtime.html)
+  [Component Stack](https://ci.apache.org/projects/flink/flink-docs-release-1.9/internals/components.html)
+ [Flink on Yarn/K8s 原理剖析及实践]( https://www.infoq.cn/article/UFeFrdqSlqI3HyRHbPNm )
+ Fabian Hueske , Vasiliki Kalavri . 《Stream Processing with Apache Flink》.  O'Reilly Media .  2019-4-30 




