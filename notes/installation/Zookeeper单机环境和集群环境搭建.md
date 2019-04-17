# Zookeeper单机环境和集群环境搭建

<nav>
<a href="#一单机环境搭建">一、单机环境搭建</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-下载">1.1 下载</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-解压">1.2 解压</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-配置环境变量">1.3 配置环境变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-修改配置">1.4 修改配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-启动">1.5 启动</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#16-验证">1.6 验证</a><br/>
<a href="#二集群环境搭建">二、集群环境搭建</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-修改配置">2.1 修改配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-标识节点">2.2 标识节点</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-启动集群">2.3 启动集群</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-集群验证">2.4 集群验证</a><br/>
</nav>


## 一、单机环境搭建

#### 1.1 下载

下载对应版本Zookeeper，这里我下载的版本`3.4.14`。官方下载地址：https://archive.apache.org/dist/zookeeper/

```shell
# wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz
```

#### 1.2 解压

```shell
# tar -zxvf zookeeper-3.4.14.tar.gz
```

#### 1.3 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export ZOOKEEPER_HOME=/usr/app/zookeeper-3.4.14
export PATH=$ZOOKEEPER_HOME/bin:$PATH
```

使得配置的环境变量生效：

```shell
# source /etc/profile
```

#### 1.4 修改配置

进入安装目录的`conf/`目录下，拷贝配置样本并进行修改

```
# cp zoo_sample.cfg  zoo.cfg
```

指定数据存储目录和日志文件目录（目录不用预先创建，程序会自动创建），修改后完整配置如下：

```properties
# The number of milliseconds of each tick
tickTime=2000
# The number of ticks that the initial
# synchronization phase can take
initLimit=10
# The number of ticks that can pass between
# sending a request and getting an acknowledgement
syncLimit=5
# the directory where the snapshot is stored.
# do not use /tmp for storage, /tmp here is just
# example sakes.
dataDir=/usr/local/zookeeper/data
dataLogDir=/usr/local/zookeeper/log
# the port at which the clients will connect
clientPort=2181
# the maximum number of client connections.
# increase this if you need to handle more clients
#maxClientCnxns=60
#
# Be sure to read the maintenance section of the
# administrator guide before turning on autopurge.
#
# http://zookeeper.apache.org/doc/current/zookeeperAdmin.html#sc_maintenance
#
# The number of snapshots to retain in dataDir
#autopurge.snapRetainCount=3
# Purge task interval in hours
# Set to "0" to disable auto purge feature
#autopurge.purgeInterval=1
```

>配置参数说明：
>
>- **tickTime**：用于计算的基础时间单元。比如session超时：N*tickTime；
>- **initLimit**：用于集群，允许从节点连接并同步到 master节点的初始化连接时间，以tickTime的倍数来表示；
>- **syncLimit**：用于集群， master主节点与从节点之间发送消息，请求和应答时间长度（心跳机制）；
>- **dataDir**：数据存储位置；
>- **dataLogDir**：日志目录；
>- **clientPort**：用于客户端连接的端口，默认2181



#### 1.5 启动

由于已经配置过环境变量，直接使用下面命令启动即可

```
zkServer.sh start
```

#### 1.6 验证

使用JPS验证进程是否已经启动，出现`QuorumPeerMain`则代表启动成功

```shell
[root@hadoop001 bin]# jps
3814 QuorumPeerMain
```



## 二、集群环境搭建

为保证集群高可用，Zookeeper集群的节点数最好是奇数，最少有三个节点，所以这里演示搭建一个三个节点的集群。

> 以下演示为单机上搭建集群，对于Zookeeper，多机集群搭建步骤和单机一致。

#### 2.1 修改配置

拷贝三份zookeeper安装包，分别修改其配置文件，主要是修改`dataDir`、`dataLogDir`以及配置集群信息。

如果是多台服务器，则集群中每个节点通讯端口和选举端口可相同，IP地址修改为每个节点所在主机IP即可。

zookeeper01配置：

```shell
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/01
dataLogDir=/usr/local/zookeeper-cluster/log/01
clientPort=2181

# server.1 这个1是服务器的标识，可以是任意有效数字，标识这是第几个服务器节点，这个标识要写到dataDir目录下面myid文件里
# 指名集群间通讯端口和选举端口
server.1=127.0.0.1:2287:3387
server.2=127.0.0.1:2288:3388
server.3=127.0.0.1:2289:3389
```

zookeeper02配置，与zookeeper01相比，只有`dataLogDir`和`dataLogDir`不同：

```shell
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/02
dataLogDir=/usr/local/zookeeper-cluster/log/02
clientPort=2181

server.1=127.0.0.1:2287:3387
server.2=127.0.0.1:2288:3388
server.3=127.0.0.1:2289:3389
```

zookeeper03配置，与zookeeper01，02相比，只有`dataLogDir`和`dataLogDir`不同：

```shell
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/03
dataLogDir=/usr/local/zookeeper-cluster/log/03
clientPort=2181

server.1=127.0.0.1:2287:3387
server.2=127.0.0.1:2288:3388
server.3=127.0.0.1:2289:3389
```

> 为节省篇幅，以上配置文件均略去英文注释

#### 2.2 标识节点

分别在三个节点的数据存储目录下新建`myid`文件,并写入对应的节点标识。

Zookeeper集群通过`myid`文件识别集群节点，并通过上文配置的节点通信端口和选举端口来进行节点通信，并选举出leader节点，从而搭建出集群。

创建存储目录：

```shell
# dataDir
mkdir -vp  /usr/local/zookeeper-cluster/data/01
# dataDir
mkdir -vp  /usr/local/zookeeper-cluster/data/02
# dataDir
mkdir -vp  /usr/local/zookeeper-cluster/data/03
```

创建并写入节点标识到`myid`文件：

```shell
#server1
echo "1" > /usr/local/zookeeper-cluster/data/01/myid
#server2
echo "2" > /usr/local/zookeeper-cluster/data/02/myid
#server3
echo "3" > /usr/local/zookeeper-cluster/data/03/myid
```

#### 2.3 启动集群

分别启动三个节点：

```shell
# 启动节点1
/usr/app/zookeeper-cluster/zookeeper01/bin/zkServer.sh start
# 启动节点2
/usr/app/zookeeper-cluster/zookeeper02/bin/zkServer.sh start
# 启动节点3
/usr/app/zookeeper-cluster/zookeeper03/bin/zkServer.sh start
```

#### 2.4 集群验证

使用jps查看进程，并且使用`zkServer.sh status`查看集群各个节点状态。如图三个节点进程均启动成功，并且两个节点为follower节点，一个节点为leader节点。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/zookeeper-cluster.png"/> </div>

