# Storm 核心概念详解

<nav>
<a href="#一storm核心概念">一、Storm核心概念</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11--Topologies拓扑">1.1  Topologies（拓扑）</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12--Streams流">1.2  Streams（流）</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-Spouts">1.3 Spouts</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-Bolts"> 1.4 Bolts</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-Stream-groupings分组策略">1.5 Stream groupings（分组策略）</a><br/>
<a href="#二storm架构详解">二、Storm架构详解</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-nimbus进程">2.1 nimbus进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-supervisor进程">2.2 supervisor进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-zookeeper的作用">2.3 zookeeper的作用</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-worker进程">2.4 worker进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-executor线程">2.5 executor线程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-并行度">2.6 并行度</a><br/>
</nav>

## 一、Storm核心概念

下图为Storm为运行流程图：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spout-bolt.png"/> </div>

### 1.1  Topologies（拓扑）

Storm应用程序的逻辑被封装在 Storm topology（拓扑）中，一个拓扑是 Spout 和 Bolt 通过 stream groupings 连接起来的有向无环图，Storm会一直保持Topologies运行，直到你将其杀死（kill）为止。

### 1.2  Streams（流）

stream 是 Storm 中的核心概念，一个 stream 是一个无界的、以分布式方式并行创建和处理的 Tuple 序列。

默认情况下 Tuple 可以包含 integers, longs, shorts, bytes, strings, doubles, floats, booleans, and byte arrays 等数据类型，当然你也可以实现自己的自定义类型。

### 1.3 Spouts

Spout 是一个 topology（拓扑）中 stream的源头， 通常 Spout 会从外部数据源读取 Tuple。

Spout分为 **可靠** 和**不可靠**两种，可靠的 Spout 在 Storm 处理失败的时候能够重新发送 Tuple, 不可靠的 Spout一旦把Tuple 发送出去就不管了。

Spout 可以向不止一个流中发送数据，可以使用`OutputFieldsDeclare` 的 declareStream 方法定义多个流，并在 `SpoutOutputCollector`对象的 emit 方法中指定要发送到的stream 。

```java
public class SpoutOutputCollector implements ISpoutOutputCollector {
    ISpoutOutputCollector _delegate;

    ...
    
    public List<Integer> emit(String streamId, List<Object> tuple, Object messageId) {
        return _delegate.emit(streamId, tuple, messageId);
    }


    public List<Integer> emit(List<Object> tuple, Object messageId) {
        return emit(Utils.DEFAULT_STREAM_ID, tuple, messageId);
    }


    public List<Integer> emit(List<Object> tuple) {
        return emit(tuple, null);
    }


    public List<Integer> emit(String streamId, List<Object> tuple) {
        return emit(streamId, tuple, null);
    }

    public void emitDirect(int taskId, String streamId, List<Object> tuple, Object messageId) {
        _delegate.emitDirect(taskId, streamId, tuple, messageId);
    }

    public void emitDirect(int taskId, List<Object> tuple, Object messageId) {
        emitDirect(taskId, Utils.DEFAULT_STREAM_ID, tuple, messageId);
    }
    

    public void emitDirect(int taskId, String streamId, List<Object> tuple) {
        emitDirect(taskId, streamId, tuple, null);
    }

    public void emitDirect(int taskId, List<Object> tuple) {
        emitDirect(taskId, tuple, null);
    }

}
```

Spout 中的最主要的方法是 `nextTuple`。`nextTuple` 向 topology（拓扑）中发送一个新的 Tuple, 如果没有 Tuple 需要发送就直接返回。对于任何 Spout 实现 `nextTuple` 方法都必须非阻塞的，因为 Storm在一个线程中调用Spout 的所有方法。

Spout 的另外几个重要的方法是 `ack` 和 `fail`。 这些方法在 Storm 检测到 Spout 发送出去的 Tuple 被成功处理或者处理失败的时候调用。 `ack`和`fail`只会在可靠的 Spout 中会被调用。

**IRichSpout**: 创建 Spout 时必须实现的接口,其中定义了Spout 的主要方法。但是在通常情况下，由于我们并不需要实现其中的全部方法，所以我们并不会直接实现IRichSpout，而是继承其抽象子类**BaseRichSpout**。

```java
public interface ISpout extends Serializable {
   
    void open(Map conf, TopologyContext context, SpoutOutputCollector collector);

    void close();
    
    void activate();
    
    void deactivate();

    void nextTuple();

    void ack(Object msgId);

    void fail(Object msgId);
}
```

BaseRichSpout继承自BaseComponent并空实现了ISpout中的部分方法，这样我们在实现自定义Spout的时候就不需要实现其中不必要的方法。

注：BaseComponent是IComponent的抽象实现类，IComponent 中定义了Topologies（拓扑）中所有基本组件（如Spout，Bolts）的常用方法。

```java
public abstract class BaseRichSpout extends BaseComponent implements IRichSpout {
    @Override
    public void close() {
    }

    @Override
    public void activate() {
    }

    @Override
    public void deactivate() {
    }

    @Override
    public void ack(Object msgId) {
    }

    @Override
    public void fail(Object msgId) {
    }
}

```

###  1.4 Bolts

Bolts是实际的stream处理单元，它负责处理数据的处理。Bolts可以执行过滤（filtering），聚合（aggregations），joins，与文件/数据库交互等操作。Bolts从spout/Bolts接收数据，处理后再发射数据到一个或多个Bolts中。

Bolts是stream的的处理单元，对于一个处理单元来说，重要的就只有三点：

+ 如何获取数据？
+ 怎样处理数据？
+ 怎样将处理好的数据发送出去？

**1.获取数据**

Spouts在从外部数据源获得数据后，将数据发送到stream，Bolts想要获得对应的数据，可以通过`shuffleGrouping`方法实现对组件（Spouts/Bolts）特定流的订阅。

**2.处理数据**

Bolt 中最主要的方法是 `execute` 方法, 当有一个新 Tuple 输入的时候就会进入这个方法，我们可以在这个方法中实现具体的处理逻辑。

**3.发送数据**

这个地方与Sprout是相同的，Bolts 可以向不止一个流中发送数据，可以使用`OutputFieldsDeclare` 的 declareStream 方法定义多个流，并在 `SpoutOutputCollector`对象的 emit 方法中指定要发送到的stream 。



### 1.5 Stream groupings（分组策略）

<div align="center"> <img width="400px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/topology-tasks.png"/> </div>

spouts和bolts在集群上执行任务时，是由多个Task并行执行（如上图，每一个圆圈代表一个Task）。当一个tuple需要从Bolt A发送给Bolt B执行的时候，我们怎么知道需要发送给Bolt B的哪一个Task执行？这是由Stream groupings 分组策略来决定的。

Storm 中一共有8个内置的 Stream Grouping。也可以通过实现 `CustomStreamGrouping`接口来自定义 Stream groupings。

1. **Shuffle grouping**: Tuple 随机的分发到 Bolt Task, 每个 Bolt 获取到等量的 Tuple。
2. **Fields grouping**: streams 通过 grouping 指定的字段来分区. 例如流通过 "user-id" 字段分区, 具有相同 "user-id" 的 Tuple 会发送到同一个task, 不同 "user-id" 的 Tuple 可能会流入到不同的 tasks。
3. **Partial Key grouping**: stream 通过 grouping 中指定的 field 来分组, 与 Fields Grouping 相似.。但是对于 2 个下游的 Bolt 来说是负载均衡的, 可以在输入数据不平均的情况下提供更好的优化。
4. **All grouping**: stream 在所有的 Bolt Tasks之间复制. 这个 Grouping 需要谨慎使用。
5. **Global grouping**: 整个 stream 会进入 Bolt 其中一个任务。特别指出, 它会进入 id 最小的 task。
6. **None grouping**:  这个 grouping , 你不需要关心 stream 如何分组. 当前, None grouping 和 Shuffle grouping  等价。同时, Storm可能会将使用 None grouping 的 bolts 和上游订阅的 bolt/spout 运行在同一个线程。
7. **Direct grouping**: 这是一种特殊的 grouping 方式. stream 用这个方式 group 意味着由这个 Tuple 的 **生产者** 来决定哪个**消费者** 来接收它。Direct grouping 只能被用于 direct streams 。
8. **Local or shuffle grouping**: 如果目标 Bolt 有多个 task 和 streams源 在同一个 woker 进程中, Tuple 只会 shuffle 到相同 worker 的任务。否则, 就和 shuffle goruping 一样。



## 二、Storm架构详解

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/Internal-Working-of-Apache-Storm.png"/> </div>

### 2.1 nimbus进程

 也叫做 master node，是storm集群工作的全局指挥官。 

1. 通过thrift接口，监听并接收client对topology的submit， 将topology代码保存到本地目录/nimbus/stormdist/下 ;
2. 为client提交的topology计算任务分配，根据集群worker资源情况，计算出topology的spoult和bolt的task应该如何在worker间分配，任务分配结果写入zookeeper ;
3. 通过thrift接口，监听supervisor的下载topology代码的请求，并提供下载 ;
4. 通过thrift接口，监听ui对统计信息的读取，从zookeeper上读取统计信息，返回给ui ;
5. 若进程退出后，立即在本机重启，则不影响集群运行。 



### 2.2 supervisor进程

也叫做 worker node ,  storm集群的资源管理者，按需启动worker进程。 

1. 定时从zookeeper 检查是否有代码未下载到本地的新topology ，定时删除旧topology代码 ;
2. 根据nimbus的任务分配结果，在本机按需启动1个或多个worker进程，监控守护所有的worker进程;
3. 若进程退出，立即在本机重启，则不影响集群运行。 



### 2.3 zookeeper的作用

Nimbus和Supervisor进程都被设计为**快速失败**（遇到任何意外情况时进程自毁）和**无状态**（所有状态保存在Zookeeper或磁盘上）。  因此，如果Nimbus或Supervisor守护进程死亡，它们会重新启动，并从zookeeper上获取之前的状态数据，就像什么都没发生一样。



### 2.4 worker进程

torm集群的任务构造者 ，构造spoult或bolt的task实例，启动executor线程。 

1. 根据zookeeper上分配的task，在本进程中启动1个或多个executor线程，将构造好的task实例交给executor去运行（死循环调用spoult.nextTuple()或bolt.execute()方法）；
2. 向zookeeper写入心跳 ；
3. 维持传输队列，发送tuple到其他的worker ；
4. 若进程退出，立即在本机重启，则不影响集群运行。 

### 2.5 executor线程

storm集群的任务执行者 ，循环执行task代码。

1. 执行1个或多个task（每个task对应spout或bolt的1个并行度），将输出加入到worker里的tuple队列 ；
2. 执行storm内部线程acker，负责发送消息处理状态给对应spoult所在的worker。



### 2.6 并行度

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/relationships-worker-processes-executors-tasks.png"/> </div>

1个worker进程执行的是1个topology的子集（注：不会出现1个worker为多个topology服务）。1个worker进程会启动1个或多个executor线程来执行1个topology的component(spout或bolt)。因此1个运行中的topology就是由集群中多台物理机上的多个worker进程组成的。

executor是1个被worker进程启动的单独线程。每个executor会运行1个component(spout或bolt)的中的一个或者多个task。

task是最终运行spout或bolt中代码的单元。topology启动后，1个component(spout或bolt)的task数目是固定不变的，但该component使用的executor线程数可以动态调整（例如：1个executor线程可以执行该component的1个或多个task实例）。这意味着，对于1个component：`#threads<=#tasks`（即：线程数小于等于task数目）这样的情况是存在的。默认情况下task的数目等于executor线程数目，即1个executor线程只运行1个task。  

**默认情况下**：

+ 每个worker进程默认启动一个executor线程
+ 每个executor默认启动一个task线程


## 参考资料

1. [storm documentation -> Concepts](http://storm.apache.org/releases/1.2.2/Concepts.html)

2. [Internal Working of Apache Storm](https://www.spritle.com/blogs/2016/04/04/apache-storm/)
3. [Understanding the Parallelism of a Storm Topology](http://storm.apache.org/releases/1.2.2/Understanding-the-parallelism-of-a-Storm-topology.html)
4. [Storm nimbus 单节点宕机的处理](https://blog.csdn.net/daiyutage/article/details/52049519)

