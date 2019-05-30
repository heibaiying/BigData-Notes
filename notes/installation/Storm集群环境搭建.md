# Storm集群环境搭建

<nav>
<a href="#一集群规划">一、集群规划</a><br/>
<a href="#二环境要求">二、环境要求</a><br/>
<a href="#三安装步骤">三、安装步骤</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-下载并解压">1. 下载并解压</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-配置环境变量">2. 配置环境变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-集群配置">3. 集群配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#4-启动服务">4. 启动服务</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#5-查看进程">5. 查看进程</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#6-查看Web-UI界面">6. 查看Web-UI界面</a><br/>
</nav>

## 一、集群规划

这里我们采用三台服务器搭建一个Storm集群，集群由一个1个Nimbus和3个Supervisor组成，因为只有三台服务器，所以hadoop001上既为Nimbus节点，也为Supervisor节点。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-集群规划.png"/> </div>

## 二、环境要求

Storm 运行依赖于Java 7+ 和 Python 2.6.6 +，所以需要预先安装这两个软件。同时为了保证高可用，这里我们不采用Storm内置的Zookeeper，而采用外置的Zookeeper集群。由于这三个软件在多个框架中都有依赖，其安装步骤单独整理至 ：

- [Linux环境下JDK安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)
- [Linux环境下Python安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下Python安装.md)

+ [Zookeeper单机环境和集群环境搭建](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Zookeeper单机环境和集群环境搭建.md)



## 三、安装步骤

### 1. 下载并解压

下载安装包，使用scp命令分发到三台服务器上，之后进行解压。官方下载地址：http://storm.apache.org/downloads.html 

```shell
# 解压
tar -zxvf apache-storm-1.2.2.tar.gz

# 分发
scp -r /usr/app/apache-storm-1.2.2/ root@hadoop002:/usr/app/
scp -r /usr/app/apache-storm-1.2.2/ root@hadoop003:/usr/app/
```

### 2. 配置环境变量

为了方便，三台服务器均配置一下环境变量：

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export STORM_HOME=/usr/app/apache-storm-1.2.2
export PATH=$STORM_HOME/bin:$PATH
```

使得配置的环境变量生效：

```shell
# source /etc/profile
```

### 3. 集群配置

修改每台服务器上的`${STORM_HOME}/conf/storm.yaml`文件，配置均如下：

```yaml
# Zookeeper集群的主机列表
storm.zookeeper.servers:
     - "hadoop001"
     - "hadoop002"
     - "hadoop003"

# Nimbus的节点列表
nimbus.seeds: ["hadoop001"]

# Nimbus和Supervisor需要使用本地磁盘上来存储少量状态（如jar包，配置文件等）
storm.local.dir: "/usr/local/tmp/storm"

# workers进程的端口，每个worker进程会使用一个端口来接收消息
supervisor.slots.ports:
     - 6700
     - 6701
     - 6702
     - 6703
```

`supervisor.slots.ports`参数用来配置workers进程接收消息的端口，默认每个supervisor节点上会启动4个worker，当然你也可以按照自己的需要和服务器性能进行设置，假设只想启动2个worker的话，此处配置2个端口即可。

### 4. 启动服务

先启动Zookeeper集群，之后再启动Storm集群。因为要启动多个进程，所以统一采用后台进程的方式启动，进入到`${STORM_HOME}/bin`目录下，依次执行下面的命令：

**hadoop001**

因为hadoop001是nimbus节点，所以需要启动nimbus服务和ui服务；同时hadoop001也是supervisor节点，所以需要启动supervisor服务和logviewer服务：

```shell
# 启动主节点 nimbus
nohup sh storm nimbus &
# 启动从节点 supervisor 
nohup sh storm supervisor &
# 启动UI界面 ui  
nohup sh storm ui &
# 启动日志查看服务 logviewer 
nohup sh storm logviewer &
```

**hadoop002  &  hadoop003**

hadoop002和hadoop003都只需要启动supervisor服务和logviewer服务：

```shell
# 启动从节点 supervisor 
nohup sh storm supervisor &
# 启动日志查看服务 logviewer 
nohup sh storm logviewer &
```



### 5. 查看进程

使用`jps`查看进程，三台服务器的进程应该分别如下：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-集群-shell.png"/> </div>



### 6. 查看Web-UI界面

访问hadoop001的8080端口，界面应如下图，可以看到有1个Nimbus和3个Supervisor，并且每个Supervisor有四个slots，即四个可用的worker进程，此时代表集群已经搭建成功。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm集群.png"/> </div>