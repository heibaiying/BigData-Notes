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

### 1.1 下载

下载对应版本 Zookeeper，这里我下载的版本 `3.4.14`。官方下载地址：https://archive.apache.org/dist/zookeeper/

```shell
# wget https://archive.apache.org/dist/zookeeper/zookeeper-3.4.14/zookeeper-3.4.14.tar.gz
```

### 1.2 解压

```shell
# tar -zxvf zookeeper-3.4.14.tar.gz
```

### 1.3 配置环境变量

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

### 1.4 修改配置

进入安装目录的 `conf/` 目录下，拷贝配置样本并进行修改：

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
>- **tickTime**：用于计算的基础时间单元。比如 session 超时：N*tickTime；
>- **initLimit**：用于集群，允许从节点连接并同步到 master 节点的初始化连接时间，以 tickTime 的倍数来表示；
>- **syncLimit**：用于集群， master 主节点与从节点之间发送消息，请求和应答时间长度（心跳机制）；
>- **dataDir**：数据存储位置；
>- **dataLogDir**：日志目录；
>- **clientPort**：用于客户端连接的端口，默认 2181



### 1.5 启动

由于已经配置过环境变量，直接使用下面命令启动即可：

```
zkServer.sh start
```

### 1.6 验证

使用 JPS 验证进程是否已经启动，出现 `QuorumPeerMain` 则代表启动成功。

```shell
[root@hadoop001 bin]# jps
3814 QuorumPeerMain
```



## 二、集群环境搭建

为保证集群高可用，Zookeeper 集群的节点数最好是奇数，最少有三个节点，所以这里演示搭建一个三个节点的集群。这里我使用三台主机进行搭建，主机名分别为 hadoop001，hadoop002，hadoop003。

### 2.1 修改配置

解压一份 zookeeper 安装包，修改其配置文件 `zoo.cfg`，内容如下。之后使用 scp 命令将安装包分发到三台服务器上：

```shell
tickTime=2000
initLimit=10
syncLimit=5
dataDir=/usr/local/zookeeper-cluster/data/
dataLogDir=/usr/local/zookeeper-cluster/log/
clientPort=2181

# server.1 这个1是服务器的标识，可以是任意有效数字，标识这是第几个服务器节点，这个标识要写到dataDir目录下面myid文件里
# 指名集群间通讯端口和选举端口
server.1=hadoop001:2287:3387
server.2=hadoop002:2287:3387
server.3=hadoop003:2287:3387
```

### 2.2 标识节点

分别在三台主机的 `dataDir` 目录下新建 `myid` 文件,并写入对应的节点标识。Zookeeper 集群通过 `myid` 文件识别集群节点，并通过上文配置的节点通信端口和选举端口来进行节点通信，选举出 Leader 节点。

创建存储目录：

```shell
# 三台主机均执行该命令
mkdir -vp  /usr/local/zookeeper-cluster/data/
```

创建并写入节点标识到 `myid` 文件：

```shell
# hadoop001主机
echo "1" > /usr/local/zookeeper-cluster/data/myid
# hadoop002主机
echo "2" > /usr/local/zookeeper-cluster/data/myid
# hadoop003主机
echo "3" > /usr/local/zookeeper-cluster/data/myid
```

### 2.3 启动集群

分别在三台主机上，执行如下命令启动服务：

```shell
/usr/app/zookeeper-cluster/zookeeper/bin/zkServer.sh start
```

### 2.4 集群验证

启动后使用 `zkServer.sh status` 查看集群各个节点状态。如图所示：三个节点进程均启动成功，并且 hadoop002 为 leader 节点，hadoop001 和 hadoop003 为 follower 节点。

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/zookeeper-hadoop001.png"/> </div>

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/zookeeper-hadoop002.png"/> </div>

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/zookeeper-hadoop003.png"/> </div>


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>