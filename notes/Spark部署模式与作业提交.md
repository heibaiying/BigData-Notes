# Spark部署模式与作业提交

<nav>
<a href="#一作业提交">一、作业提交</a><br/>
<a href="#二Local模式">二、Local模式</a><br/>
<a href="#三Standalone模式">三、Standalone模式</a><br/>
<a href="#三Spark-on-Yarn模式">三、Spark on Yarn模式</a><br/>
</nav>


## 一、作业提交

### 1.1  spark-submit

Spark所有模式均通过使用`spark-submit`提交作业，其命令格式如下：

```shell
./bin/spark-submit \
  --class <main-class> \        # 应用程序主入口类
  --master <master-url> \       # 集群的Master Url
  --deploy-mode <deploy-mode> \ # 部署模式
  --conf <key>=<value> \        # 可选配置       
  ... # other options    
  <application-jar> \           # Jar包路径 
  [application-arguments]       #传递给主入口类的参数  
```

需要注意的是：在集群环境下，`application-jar`必须能被集群中所有节点都能访问，可以是HDFS上的路径；也可以是本地文件系统路径，如果是本地文件系统路径，则要求集群中每一个节点上的相同路径都存在该Jar包。

### 1.2 deploy-mode

deploy-mode有`cluster`和`client`两个可选参数，默认为`client`。这里以Spark On Yarn模式对两者的区别进行说明 ：

+ 在cluster模式下，Spark Drvier在应用程序Master进程内运行，该进程由群集上的YARN管理，提交作业的客户端可以在启动应用程序后关闭；
+ 在client模式下，Spark Drvier在提交作业的客户端进程中运行，应用程序Master服务器仅用于从YARN请求资源。

### 1.3 master-url

master-url的所有可选参数如下表所示：

| Master URL                        | Meaning                                                      |
| --------------------------------- | ------------------------------------------------------------ |
| `local`                           | 使用一个线程本地运行Spark                                    |
| `local[K]`                        | 使用 K 个 worker 线程本地运行 Spark                          |
| `local[K,F]`                      | 使用 K 个 worker 线程本地运行 , 第二个参数为Task的失败重试次数 |
| `local[*]`                        | 使用与CPU核心数一样的线程数在本地运行Spark                   |
| `local[*,F]`                      | 使用与CPU核心数一样的线程数在本地运行Spark<br/>第二个参数为Task的失败重试次数 |
| `spark://HOST:PORT`               | 连接至指定的standalone 集群的 master节点。端口号默认是 7077。 |
| `spark://HOST1:PORT1,HOST2:PORT2` | 如果standalone集群采用Zookeeper实现高可用，则必须包含由zookeeper设置的所有master主机地址。 |
| `mesos://HOST:PORT`               | 连接至给定的Mesos集群。端口默认是 5050。对于使用了 ZooKeeper 的 Mesos cluster 来说，使用 `mesos://zk://...`来指定地址，使用 `--deploy-mode cluster`模式来提交。 |
| `yarn`                            | 连接至一个YARN 集群，集群由配置的 `HADOOP_CONF_DIR` 或者 `YARN_CONF_DIR` 来决定。使用`--deploy-mode`参数来配置`client` 或`cluster` 模式。 |

接下来主要介绍三种常用部署模式的配置及作业的提交。

## 二、Local模式

Local模式下提交作业最为简单，不需要进行任何配置，提交命令如下：

```shell
# 本地模式提交应用
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master local[2] \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100   # 传给SparkPi的参数
```

这里的`spark-examples_2.11-2.4.0.jar`在Spark安装包里默认就有，是Spark官方提供的测试用例，`SparkPi`用于计算Pi值，执行成功后可以在输出中看到计算出的Pi值。

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-pi.png"/> </div>



## 三、Standalone模式

Standalone是Spark提供的一种内置的集群模式，采用内置的资源管理器进行管理。

下面按照如图所示演示1个Mater和2个Worker节点的集群配置，这里使用两台主机进行演示：

+ hadoop001： 由于只有两台主机，所以hadoop001既是Master节点，也是Worker节点;
+ hadoop002 ： Worker节点。





<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-集群模式.png"/> </div>

### 3.1 环境配置

首先需要保证Spark已经解压在两台主机的相同路径上。然后进入hadoop001的`${SPARK_HOME}/conf/`目录下，拷贝配置样本并进行相关配置：

```shell
# cp spark-env.sh.template spark-env.sh
```

在`spark-env.sh`中配置JDK的目录，完成后将该配置使用scp命令分发到hadoop002上：

```shell
# JDK安装位置
JAVA_HOME=/usr/java/jdk1.8.0_201
```

### 3.2 集群配置

在`${SPARK_HOME}/conf/`目录下，拷贝集群配置样本并进行相关配置：

```
# cp slaves.template slaves
```

指定所有Worker节点的主机名：

```shell
# A Spark Worker will be started on each of the machines listed below.
hadoop001
hadoop002
```

这里需要注意以下三点：

+ 主机名与IP地址的映射必须在`/etc/hosts`文件中已经配置，否则就直接使用IP地址；
+ 每个主机名必须独占一行；
+ Spark的Master主机是通过ssh访问所有的Worker节点，所以需要预先配置免密登录。或者通过设置环境变量`SPARK_SSH_FOREGROUND`并为每个Worker节点指定密码。

### 3.3 启动

使用`start-all.sh`代表启动Master和所有Worker服务。

```shell
./sbin/start-master.sh 
```

访问8080端口，查看Spark的Web-UI界面,，此时应该显示有两个有效的工作节点：

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-Standalone-web-ui.png"/> </div>

### 3.4 提交作业

```shell
# 以client模式提交到standalone集群 
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master spark://hadoop001:7077 \
--executor-memory 2G \
--total-executor-cores 10 \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100

# 以cluster模式提交到standalone集群 
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master spark://207.184.161.138:7077 \
--deploy-mode cluster \
--supervise \  # 配置此参数代表开启监督，如果主应用程序异常退出，则自动重启Driver
--executor-memory 2G \
--total-executor-cores 10 \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100
```

### 3.5 可选配置

在虚拟机上提交作业时经常出现一个的问题是作业无法申请到足够的资源：

```properties
Initial job has not accepted any resources; 
check your cluster UI to ensure that workers are registered and have sufficient resources
```

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-内存不足2.png"/> </div>

这时候可以查看Web UI，我这里是内存空间不足：提交命令中要求作业的`executor-memory`是2G，但是实际的工作节点的`Memory`只有1G，这时候你可以修改`--executor-memory`，也可以修改 Woker 的`Memory`，其默认值为主机所有可用内存值减去1G。

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-内存不足.png"/> </div>   

关于Master和Woker节点的所有可选配置如下，可以在`spark-env.sh`中进行对应的配置：    

| Environment Variable（环境变量） | Meaning（含义）                                              |
| -------------------------------- | ------------------------------------------------------------ |
| `SPARK_MASTER_HOST`              | master 节点地址                                              |
| `SPARK_MASTER_PORT`              | master 节点地址端口（默认：7077）                            |
| `SPARK_MASTER_WEBUI_PORT`        | master 的 web UI 的端口（默认：8080）                        |
| `SPARK_MASTER_OPTS`              | 仅用于 master 的配置属性，格式是 "-Dx=y"（默认：none）,所有属性可以参考官方文档：[spark-standalone-mode](https://spark.apache.org/docs/latest/spark-standalone.html#spark-standalone-mode) |
| `SPARK_LOCAL_DIRS`               | spark 的临时存储的目录，用于暂存map的输出和持久化存储RDDs。多个目录用逗号分隔 |
| `SPARK_WORKER_CORES`             | spark worker节点可以使用CPU Cores的数量。（默认：全部可用）  |
| `SPARK_WORKER_MEMORY`            | spark worker节点可以使用的内存数量（默认：全部的内存减去1GB）； |
| `SPARK_WORKER_PORT`              | spark worker节点的端口（默认： random（随机））              |
| `SPARK_WORKER_WEBUI_PORT`        | worker 的 web UI 的 Port（端口）（默认：8081）               |
| `SPARK_WORKER_DIR`               | worker运行应用程序的目录，这个目录中包含日志和暂存空间（default：SPARK_HOME/work） |
| `SPARK_WORKER_OPTS`              | 仅用于 worker 的配置属性，格式是 "-Dx=y"（默认：none）。所有属性可以参考官方文档：[spark-standalone-mode](https://spark.apache.org/docs/latest/spark-standalone.html#spark-standalone-mode) |
| `SPARK_DAEMON_MEMORY`            | 分配给 spark master 和 worker 守护进程的内存。（默认： 1G）  |
| `SPARK_DAEMON_JAVA_OPTS`         | spark master 和 worker 守护进程的 JVM 选项，格式是 "-Dx=y"（默认：none） |
| `SPARK_PUBLIC_DNS`               | spark master 和 worker 的公开 DNS 名称。（默认：none）       |



## 三、Spark on Yarn模式

Spark支持将作业提交到Yarn上运行，此时不需要启动Master节点，也不需要启动Worker节点。

### 3.1 配置

在`spark-env.sh`中配置hadoop的配置目录的位置，可以使用`YARN_CONF_DIR`或`HADOOP_CONF_DIR`进行指定：

```properties
YARN_CONF_DIR=/usr/app/hadoop-2.6.0-cdh5.15.2/etc/hadoop
# JDK安装位置
JAVA_HOME=/usr/java/jdk1.8.0_201
```

### 3.2 启动

必须要保证Hadoop已经启动，这里包括Yarn和HDFS都需要启动，因为在计算过程中Spark会使用HDFS存储临时文件，如果HDFS没有启动，则会抛出异常。

```shell
# start-yarn.sh
# start-dfs.sh
```

### 3.3 提交应用

```shell
#  以client模式提交到yarn集群 
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master yarn \
--deploy-mode client \
--executor-memory 2G \
--num-executors 10 \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100

#  以cluster模式提交到yarn集群 
spark-submit \
--class org.apache.spark.examples.SparkPi \
--master yarn \
--deploy-mode cluster \
--executor-memory 2G \
--num-executors 10 \
/usr/app/spark-2.4.0-bin-hadoop2.6/examples/jars/spark-examples_2.11-2.4.0.jar \
100
```



