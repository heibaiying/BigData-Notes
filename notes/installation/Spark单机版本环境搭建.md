# Spark单机版本环境搭建



>**系统环境**：centos 7.6
>
>**Spark版本**：spark-2.2.3-bin-hadoop2.6



### 1. Spark安装包下载

官网下载地址：http://spark.apache.org/downloads.html

因为Spark常常和Hadoop联合使用，所以下载时候需要选择Spark版本和对应的Hadoop版本后再下载

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-download.png"/> </div>



### 2.  解压安装包

```shell
# tar -zxvf  spark-2.2.3-bin-hadoop2.6.tgz
```



### 3.  配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export SPARK_HOME=/usr/app/spark-2.2.3-bin-hadoop2.6
export  PATH=${SPARK_HOME}/bin:$PATH
```

使得配置的环境变量生效：

```shell
# source /etc/profile
```



### 4. Standalone模式启动Spark

进入`${SPARK_HOME}/conf/`目录下，拷贝配置样本并进行相关配置：

```shell
# cp spark-env.sh.template spark-env.sh
```

在`spark-env.sh`中增加如下配置：

```shell
# 主机节点地址
SPARK_MASTER_HOST=hadoop001
# Worker节点的最大并发task数
SPARK_WORKER_CORES=2
# Worker节点使用的最大内存数
SPARK_WORKER_MEMORY=1g
# 每台机器启动Worker实例的数量
SPARK_WORKER_INSTANCES=1
# JDK安装位置
JAVA_HOME=/usr/java/jdk1.8.0_201
```

进入`${SPARK_HOME}/sbin/`目录下,启动服务：

```shell
# ./start-all.sh
```



### 5. 验证启动是否成功

访问8080端口，查看Spark的Web-UI界面

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-web-ui.png"/> </div>





## 附：一个简单的词频统计例子，感受spark的魅力

#### 1. 准备一个词频统计的文件样本wc.txt,内容如下：

```txt
hadoop,spark,hadoop
spark,flink,flink,spark
hadoop,hadoop
```

#### 2. 指定spark master 节点地址，启动spark-shell

```shell
# spark-shell --master spark://hadoop001:7077
```

#### 3. 在scala交互式命令行中执行如下命名

```scala
val file = spark.sparkContext.textFile("file:///usr/app//wc.txt")
val wordCounts = file.flatMap(line => line.split(",")).map((word => (word, 1))).reduceByKey(_ + _)
wordCounts.collect
```

执行过程如下：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-shell.png"/> </div>

通过spark shell web-ui可以查看作业的执行情况，访问端口为4040

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/spark-shell-web-ui.png"/> </div>