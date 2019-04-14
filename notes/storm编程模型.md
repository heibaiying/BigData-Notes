# Storm 编程模型

## 一、简介

下图为Strom的运行流程图，也是storm的编程模型图，在storm 进行流处理时，我们需要自定义实现自己的spout（数据源）和bolt（处理单元），并通过`TopologyBuilder`将它们之间进行关联，定义好数据处理的流程。

下面小结分别介绍如何按照storm内置接口分别实现spout和bolt，然后将其进行关联，最后将其提交到本地和服务器进行运行。

![spout-bolt](D:\BigData-Notes\pictures\spout-bolt.png)

## 二、IComponent

`IComponent`接口定义了Topology中所有组件（spout/bolt）的公共方法，我们实现spout或bolt都必须直接或者间接实现这个接口。

```java
public interface IComponent extends Serializable {

    /**
     * 声明此拓扑的所有流的输出模式。
     * @param declarer这用于声明输出流id，输出字段以及每个输出流是否是直接流（direct stream）
     */
    void declareOutputFields(OutputFieldsDeclarer declarer);

    /**
     * 声明此组件的配置。
     *
     */
    Map<String, Object> getComponentConfiguration();

}
```

## 三、spout

### 3.1 ISpout接口

实现自定义的spout需要实现`ISpout`，其定义了spout的所有可用方法：

```java
public interface ISpout extends Serializable {
    /**
     * 组件初始化时候被调用
     *
     * @param conf ISpout的配置
     * @param context 应用上下文，可以通过其获取任务ID和组件ID，输入和输出信息等。
     * @param collector  用来发送spout中的tuples，它是线程安全的，建议保存为此spout对象的实例变量
     */
    void open(Map conf, TopologyContext context, SpoutOutputCollector collector);

    /**
     * ISpout将要被关闭的时候调用。但是其不一定会被执行，如果在集群环境中通过kill -9 杀死进程时其就无法被执行。
     */
    void close();
    
    /**
     * 当ISpout从停用状态激活时被调用
     */
    void activate();
    
    /**
     * 当ISpout停用时候被调用
     */
    void deactivate();

    /**
     * 这是一个核心方法，主要通过在此方法中调用collector将tuples发送给下一个接收器，这个方法必须是非阻塞的。              * nextTuple/ack/fail/是在同一个线程中执行的，所以不用考虑线程安全方面。当没有tuples发出时应该
     * 让nextTuple休眠（sleep）一下，以免浪费CPU。
     */
    void nextTuple();

    /**
     * 通过msgId进行tuples处理成功的确认，被确认后的tuples不会再次被发送
     */
    void ack(Object msgId);

    /**
     * 通过msgId进行tuples处理失败的确认，被确认后的tuples会再次被发送进行处理
     */
    void fail(Object msgId);
}
```

### 3.2 BaseRichSpout抽象类

**通常情况下，我们实现自定义的Spout时不会直接去实现`ISpout`接口，而是继承`BaseRichSpout`。**`BaseRichSpout`继承自`BaseCompont`，同时实现了`IRichSpout`接口。

![storm-baseRichSpout](D:\BigData-Notes\pictures\storm-baseRichSpout.png)

`IRichSpout`接口继承自`ISpout`和`IComponent`,自身并没有定义任何方法。

```java
public interface IRichSpout extends ISpout, IComponent {

}
```

BaseComponent 抽象类也仅仅是空实现了`IComponent`的`getComponentConfiguration`方法。

```java
public abstract class BaseComponent implements IComponent {
    @Override
    public Map<String, Object> getComponentConfiguration() {
        return null;
    }    
}
```

`BaseRichSpout`通过继承自`BaseCompont`，同时实现了`IRichSpout`接口，并且空实现了其中部分方法。

```java
public abstract class BaseRichSpout extends BaseComponent implements IRichSpout {
    @Override
    public void close() {}

    @Override
    public void activate() {}

    @Override
    public void deactivate() {}

    @Override
    public void ack(Object msgId) {}

    @Override
    public void fail(Object msgId) {}
}
```

通过这样的设计，我们在继承`BaseRichSpout`实现自己的spout时，就只需要实现三个必须的方法：

+ open ： 来源于ISpout，可以通过此方法获取用来发送tuples的`SpoutOutputCollector`；
+ nextTuple ：来源于ISpout，必须在此方法内部才能调用`SpoutOutputCollector`发送tuple；
+ declareOutputFields ：来源于IComponent，通过此方法声明发送的tuple的名称，这样下一个组件才能知道如何接受数据。



## 四、bolt

通过上小结我们已经了解了storm如何对spout接口进行设计的，bolt接口的设计也是一样的。

### 4.1 IBolt 接口

```java
 /**
  * 在客户端计算机上创建的IBolt对象。会被被序列化到topology中（使用Java序列化）,并提交给集群的主机（Nimbus）。  
  * Nimbus启动workers反序列化对象，调用prepare，然后开始处理tuples。
 */

public interface IBolt extends Serializable {
    /**
     * 组件初始化时候被调用
     *
     * @param conf storm中定义的此bolt的配置
     * @param context 应用上下文，可以通过其获取任务ID和组件ID，输入和输出信息等。
     * @param collector  用来发送spout中的tuples，它是线程安全的，建议保存为此spout对象的实例变量
     */
    void prepare(Map stormConf, TopologyContext context, OutputCollector collector);

    /**
     * 处理单个tuple输入。
     * 
     * @param Tuple对象包含关于它的元数据（如来自哪个组件/流/任务）
     */
    void execute(Tuple input);

    /**
     * IBolt将要被关闭的时候调用。但是其不一定会被执行，如果在集群环境中通过kill -9 杀死进程时其就无法被执行。
     */
    void cleanup();
```



### 4.2 BaseRichBolt抽象类

同样的，在实现我们自己的bolt时，我们也通常是继承`BaseRichBolt`抽象类来实现。`BaseRichBolt`继承自`BaseComponent`抽象类，并实现了`IRichBolt`接口。

![storm-baseRichbolt](D:\BigData-Notes\pictures\storm-baseRichbolt.png)

`IRichBolt`接口继承自`IBolt`和`IComponent`,自身并没有定义任何方法。

```
public interface IRichBolt extends IBolt, IComponent {

}
```

通过这样的设计，我们在继承`BaseRichBolt`实现自己的bolt时，就只需要实现三个必须的方法：

- prepare： 来源于IBolt，可以通过此方法获取用来发送tuples的`SpoutOutputCollector`；
- execute：来源于IBolt，处理tuple和发送处理完成的tuple；
- declareOutputFields ：来源于IComponent，通过此方法声明发送的tuple的名称，这样下一个组件才能知道如何接受数据。



## 五、使用案例



## 六、提交到服务器运行