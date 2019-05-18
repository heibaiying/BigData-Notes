# Storm和流处理简介

<nav>
<a href="#一Storm">一、Storm</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-简介">1.1 简介</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-Storm-与-Hadoop对比">1.2 Storm 与 Hadoop对比</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-Storm-与Spark-Streaming对比">1.3 Storm 与  Spark Streaming对比</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-Storm-与-Flink对比">1.4 Storm 与 Flink对比</a><br/>
<a href="#二流处理">二、流处理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-静态数据处理">2.1 静态数据处理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-流处理">2.2 流处理</a><br/>
</nav>


## 一、Storm

#### 1.1 简介

Storm 是一个开源的分布式实时计算系统，可以简单、可靠进行大数据流的处理。通常用于实时分析，在线机器学习、持续计算、分布式RPC、ETL等场景。Storm具有以下特点：

+ 支持水平横向扩展；
+ 具有高容错性，通过ACK机制每个消息都不丢失；
+ 处理速度非常快，每个节点每秒能处理超过一百万个tuples ；
+ 易于设置和操作，并可以与任何编程语言一起使用；
+ 支持本地模式，这意味着你开发的代码不必每次都要提交到服务器上才能运行，对于开发人员来说非常友好；
+ 支持图形化管理界面。



#### 1.2 Storm 与 Hadoop对比

Hadoop采用HDFS存储数据，采用MapReduce处理数据。MapReduce主要是进行数据的批处理，这使得Hadoop更适合于海量数据的离线处理，却不适合于实时性要求比较高的场景。而Strom的设计目标就是就是对数据进行实时计算，这使得其更适合实时数据分析等场景。



#### 1.3 Storm 与 Spark Streaming对比

Spark Streaming并不是真正意义上的流处理框架。 Spark Streaming接收实时输入的数据流，并将数据拆分为批处理，由Spark引擎处理后批量生成结果流。只不过 Spark Streaming 能够将数据流进行极小粒度的拆分，使得其能够得到接近于流处理的效果，但其本质上还是批处理（或微批处理）。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/streaming-flow.png"/> </div>

#### 1.4 Strom 与 Flink对比

storm和Flink都是真正意义上的实时计算框架。其对比如下：

|          | storm                                                        | flink                                                        |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 状态管理 | 无状态                                                       | 有状态                                                       |
| 窗口支持 | 对事件窗口支持较弱，缓存整个窗口的所有数据，窗口结束时一起计算 | 窗口支持较为完善，自带一些窗口聚合方法，<br>并且会自动管理窗口状态 |
| 消息投递 | At Most  Once<br/>At Least Once                              | At Most  Once<br/>At Least Once<br/>**Exactly Once**         |
| 容错方式 | ACK机制：对每个消息进行全链路跟踪，失败或者超时时候进行重发  | 检查点机制：通过分布式一致性快照机制，<br/>对数据流和算子状态进行保存。在发生错误时，使系统能够进行回滚。 |


> 注  :  一般来说，对于消息投递，一般有以下三种方案：
> + At Most Once : 投递保证每个消息会被投递0次或者1次，在这种机制下消息很有可能会丢失；
> + At Least Once :投递保证了每个消息会被默认投递多次，至少保证有一次被成功接收，信息可能有重复，但是不会丢失；
> + Exactly Once  : 每个消息对于接收者而言正好被接收一次，保证即不会丢失也不会重复。



## 二、流处理

#### 2.1 静态数据处理

在流处理之前，数据通常存储在数据库，文件系统或其他形式的存储系统中。应用程序根据需要查询数据或计算数据。这就是传统的静态数据处理架构。Hadoop采用HDFS进行数据存储，采用MapReduce进行数据查询或分析，这就是典型的静态数据处理架构。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/01_data_at_rest_infrastructure.png"/> </div>



#### 2.2 流处理

而流处理则是直接对运动中的数据的处理，在接收数据时直接计算数据。

大多数数据都是连续的流：传感器事件，网站上的用户活动，金融交易等等 ，所有这些数据都是随着时间的推移而创建的。

接收和发送数据流并执行应用程序或分析逻辑的系统称为**流处理器**。流处理器的基本职责是确保数据有效流动，同时具备可扩展性和容错能力，Storm和Flink就是其代表性的实现。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/02_stream_processing_infrastructure.png"/> </div>



流处理带来了静态数据处理所不具备的众多优点：

 

- **应用程序立即对数据做出反应**：降低了数据的滞后性，使得数据更具有时效性，更能反映对未来的预期；

- **流处理可以处理更大的数据量**：直接处理数据流，并且只保留数据中有意义的子集，并将其传送到下一个处理单元，逐级过滤数据，降低需要处理的数据量，从而能够承受更大的数据量；

- **流处理更贴近现实的数据模型**：在实际的环境中，一切数据都是持续变化的，要想能够通过过去的数据推断未来的趋势，必须保证数据的不断输入和模型的不断修正，典型的就是金融市场、股票市场，流处理能更好的应对这些数据的连续性的特征和及时性的需求；

- **流处理分散和分离基础设施**：流式处理减少了对大型数据库的需求。相反，每个流处理程序通过流处理框架维护了自己的数据和状态，这使得流处理程序更适合微服务架构。





## 参考资料

1.  [What is stream processing?](https://www.ververica.com/what-is-stream-processing)
2. [流计算框架Flink与Storm的性能对比](http://bigdata.51cto.com/art/201711/558416.htm)
