# Storm 编程模型

<nav>
<a href="#一简介">一、简介</a><br/>
<a href="#二IComponent接口">二、IComponent接口</a><br/>
<a href="#三Spout">三、Spout</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-ISpout接口">3.1 ISpout接口</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-BaseRichSpout抽象类">3.2 BaseRichSpout抽象类</a><br/>
<a href="#四Bolt">四、Bolt</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#41-IBolt-接口">4.1 IBolt 接口</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#42-BaseRichBolt抽象类">4.2 BaseRichBolt抽象类</a><br/>
<a href="#五词频统计案例">五、词频统计案例</a><br/>
<a href="#六提交到服务器集群运行">六、提交到服务器集群运行</a><br/>
<a href="#七通用打包方法">七、通用打包方法</a><br/>
</nav>




## 一、简介

下图为Strom的运行流程图，也是storm的编程模型图，在storm 进行流处理时，我们需要自定义实现自己的spout（数据源）和bolt（处理单元），并通过`TopologyBuilder`将它们之间进行关联，定义好数据处理的流程。

下面小结分别介绍如何按照storm内置接口分别实现spout和bolt，然后将其进行关联，最后将其提交到本地和服务器进行运行。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spout-bolt.png"/> </div>

## 二、IComponent接口

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

## 三、Spout

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
     * 这是一个核心方法，主要通过在此方法中调用collector将tuples发送给下一个接收器，这个方法必须是非阻塞的。              
     * nextTuple/ack/fail/是在同一个线程中执行的，所以不用考虑线程安全方面。当没有tuples发出时应该让nextTuple
     * 休眠(sleep)一下，以免浪费CPU。
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

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-baseRichSpout.png"/> </div>

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



## 四、Bolt

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

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-baseRichbolt.png"/> </div>

`IRichBolt`接口继承自`IBolt`和`IComponent`,自身并没有定义任何方法。

```
public interface IRichBolt extends IBolt, IComponent {

}
```

通过这样的设计，我们在继承`BaseRichBolt`实现自己的bolt时，就只需要实现三个必须的方法：

- prepare： 来源于IBolt，可以通过此方法获取用来发送tuples的`OutputCollector`；
- execute：来源于IBolt，处理tuple和发送处理完成的tuple；
- declareOutputFields ：来源于IComponent，通过此方法声明发送的tuple的名称，这样下一个组件才能知道如何接受数据。



## 五、词频统计案例

### 5.1 案例简介

使用模拟数据进行词频统计。这里我们使用自定义的DataSourceSpout产生模拟数据。实际生产环境中通常是通过日志收集引擎（如Flume、logstash等）将收集到的数据发送到kafka指定的topic，通过storm内置的`KafkaSpout `来监听该topic，并获取数据。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-word-count-p.png"/> </div>

### 5.2 代码实现

#### 1. 项目依赖

```xml
<dependency>
    <groupId>org.apache.storm</groupId>
    <artifactId>storm-core</artifactId>
    <version>1.2.2</version>
</dependency>
```



#### 2. DataSourceSpout

```java
public class DataSourceSpout extends BaseRichSpout {

    private List<String> list = Arrays.asList("Spark", "Hadoop", "HBase", "Storm", "Flink", "Hive");

    private SpoutOutputCollector spoutOutputCollector;

    @Override
    public void open(Map map, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.spoutOutputCollector = spoutOutputCollector;
    }

    @Override
    public void nextTuple() {
        // 模拟产生数据
        String lineData = productData();
        spoutOutputCollector.emit(new Values(lineData));
        Utils.sleep(1000);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields("line"));
    }


    /**
     * 模拟数据
     */
    private String productData() {
        Collections.shuffle(list);
        Random random = new Random();
        int endIndex = random.nextInt(list.size()) % (list.size()) + 1;
        return StringUtils.join(list.toArray(), "\t", 0, endIndex);
    }

}
```

上面类使用`productData`方法来产生模拟数据，产生数据的格式如下：

```properties
Spark	HBase
Hive	Flink	Storm	Hadoop	HBase	Spark
Flink
HBase	Storm
HBase	Hadoop	Hive	Flink
HBase	Flink	Hive	Storm
Hive	Flink	Hadoop
HBase	Hive
Hadoop	Spark	HBase	Storm
```

#### 3. SplitBolt

```java
public class SplitBolt extends BaseRichBolt {

    private OutputCollector collector;

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        this.collector=collector;
    }

    @Override
    public void execute(Tuple input) {
        String line = input.getStringByField("line");
        String[] words = line.split("\t");
        for (String word : words) {
            collector.emit(new Values(word));
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word"));
    }
}
```

#### 4. CountBolt

```java
public class CountBolt extends BaseRichBolt {

    private Map<String, Integer> counts = new HashMap<>();

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

    }

    @Override
    public void execute(Tuple input) {
        String word = input.getStringByField("word");
        Integer count = counts.get(word);
        if (count == null) {
            count = 0;
        }
        count++;
        counts.put(word, count);
        // 输出
        System.out.print("当前实时统计结果:");
        counts.forEach((key, value) -> System.out.print(key + ":" + value + "; "));
        System.out.println();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}
```

#### 5.  LocalWordCountApp

通过TopologyBuilder将上面定义好的组件进行串联形成 Topology，并提交到本地集群（LocalCluster）运行。在通常开发中，可用本地集群进行，测试完成后再提交到服务器集群运行。

```java
public class LocalWordCountApp {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        
        // 指明将 DataSourceSpout 的数据发送到 SplitBolt 中处理
        builder.setBolt("SplitBolt", new SplitBolt()).shuffleGrouping("DataSourceSpout");
        
        //  指明将 SplitBolt 的数据发送到 CountBolt 中 处理
        builder.setBolt("CountBolt", new CountBolt()).shuffleGrouping("SplitBolt");

        // 创建本地集群用于测试 这种模式不需要本机安装storm,直接运行该Main方法即可
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("LocalWordCountApp",
                new Config(), builder.createTopology());
    }

}
```



#### 6. 运行结果

启动`WordCountApp`的main方法即可运行，采用本地模式storm会自动在本地搭建一个集群，所以启动的过程会稍慢一点，启动成功后即可看到输出日志。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-word-count-console.png"/> </div>



## 六、提交到服务器集群运行

### 6.1 代码更改

提交到服务器的代码和本地代码略有不同，提交到服务器集群时需要使用`StormSubmitter`进行提交。主要代码如下。

> 为了结构清晰，这里新建ClusterWordCountApp类来演示集群提交。实际开发中可以将两种模式的代码写在同一个类中，通过外部传参来决定启动何种模式。

```java
public class ClusterWordCountApp {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        
        // 指明将 DataSourceSpout 的数据发送到 SplitBolt 中处理
        builder.setBolt("SplitBolt", new SplitBolt()).shuffleGrouping("DataSourceSpout");
        
        //  指明将 SplitBolt 的数据发送到 CountBolt 中 处理
        builder.setBolt("CountBolt", new CountBolt()).shuffleGrouping("SplitBolt");

        // 使用StormSubmitter提交Topology到服务器集群
        try {
            StormSubmitter.submitTopology("ClusterWordCountApp",  new Config(), builder.createTopology());
        } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
            e.printStackTrace();
        }
    }

}
```

### 6.2 打包上传

打包后上传到服务器任意位置，这里我打包后的名称为`storm-word-count-1.0.jar`

```shell
# mvn clean package -Dmaven.test.skip=true
```

### 6.3 提交Topology

使用以下命令提交Topology到集群：

```shell
# 命令格式：storm  jar  jar包位置  主类的全路径  ...可选传参
storm jar /usr/appjar/storm-word-count-1.0.jar  com.heibaiying.wordcount.ClusterWordCountApp
```

出现`successfully`则代表提交成功

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-submit-success.png"/> </div>

### 6.4 查看Topology与停止Topology（命令行方式）

```shell
# 查看所有Topology
storm list

# 停止  storm kill topology-name [-w wait-time-secs]
storm kill ClusterWordCountApp -w 3
```

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-list-kill.png"/> </div>

### 6.5 查看Topology与停止Topology（界面方式）

使用UI界面同样也可进行同样的操作，进入WEB UI界面（8080端口），在`Topology Summary`中点击对应Topology 即可进入详情页面进行操作。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-ui-actions.png"/> </div>









## 七、通用打包方法

### 1. mvn package的局限性

上面我们直接使用`mvn package`进行项目打包，这对于没有使用外部依赖包的项目是可行的。但如果项目中使用了第三方JAR包，就会出现问题，因为`package`打包后的JAR中是不含有依赖包的，如果此时你提交到服务器上运行，就会出现找不到第三方依赖的异常。

这时候可能大家会有疑惑，在我们的项目中不是使用了`storm-core`这个依赖吗？其实上面之所以我们能运行成功，是因为在Storm的集群环境中提供了这个JAR包，在安装目录的lib目录下：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-lib.png"/> </div>

为了说明这个问题我在Maven中引入了一个第三方的JAR包，并修改产生数据的方法：

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.8.1</version>
</dependency>
```

`StringUtils.join()`这个方法在`commons.lang3`和`storm-core`中都有，原来的代码无需任何更改，只需要在`import`时指明使用`commons.lang3`。

```java
import org.apache.commons.lang3.StringUtils;

private String productData() {
    Collections.shuffle(list);
    Random random = new Random();
    int endIndex = random.nextInt(list.size()) % (list.size()) + 1;
    return StringUtils.join(list.toArray(), "\t", 0, endIndex);
}
```

此时直接使用`mvn clean package`打包上传到服务器运行，就会抛出下图异常。

其实官方文档里面并没有推荐使用这种打包方法，而是网上很多词频统计的Demo使用了。所以在此说明一下：这种打包方式并不适用于实际的开发，因为实际开发中通常都是需要第三方的JAR包的。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-package-error.png"/> </div>



### 2. 官方推荐的的打包方法

>If you're using Maven, the [Maven Assembly Plugin](http://maven.apache.org/plugins/maven-assembly-plugin/) can do the packaging for you. Just add this to your pom.xml:
>
>```xml
>  <plugin>
>    <artifactId>maven-assembly-plugin</artifactId>
>    <configuration>
>      <descriptorRefs>  
>        <descriptorRef>jar-with-dependencies</descriptorRef>
>      </descriptorRefs>
>      <archive>
>        <manifest>
>          <mainClass>com.path.to.main.Class</mainClass>
>        </manifest>
>      </archive>
>    </configuration>
>  </plugin>
>```
>
>Then run mvn assembly:assembly to get an appropriately packaged jar. Make sure you [exclude](http://maven.apache.org/plugins/maven-assembly-plugin/examples/single/including-and-excluding-artifacts.html) the Storm jars since the cluster already has Storm on the classpath.

其实就是两点：

+ 使用maven-assembly-plugin进行打包，因为maven-assembly-plugin会把所有的依赖一并打包到最后的JAR中；
+ 排除掉Storm集群环境中已经提供的Storm jars。

按照官方文档的说明，修改我们的POM文件，如下：

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptors>
                    <descriptor>src/main/resources/assembly.xml</descriptor>
                </descriptors>
                <archive>
                    <manifest>
                        <mainClass>com.heibaiying.wordcount.ClusterWordCountApp</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

其中`assembly.xml`的文件内容如下：

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    
    <id>jar-with-dependencies</id>

    <!--指明打包方式-->
    <formats>
        <format>jar</format>
    </formats>

    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
            <!--排除storm环境中已经提供的storm-core-->
            <excludes>
                <exclude>org.apache.storm:storm-core</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
</assembly>
```

打包命令为：

```shell
# mvn clean assembly:assembly -Dmaven.test.skip=true
```

打包后会同时生成两个JAR包，其中后缀为`jar-with-dependencies`是含有第三方依赖的JAR包，通过压缩工具可以看到内部已经打入了依赖包。另外后缀是由`assembly.xml`中`<id>`标签指定的，你可以自定义修改。提交该JAR到集群环境即可。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-jar.png"/> </div>



## 参考资料

1. [Running Topologies on a Production Cluster](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Running-topologies-on-a-production-cluster.html)
2. [Pre-defined Descriptor Files](http://maven.apache.org/plugins/maven-assembly-plugin/descriptor-refs.html)
