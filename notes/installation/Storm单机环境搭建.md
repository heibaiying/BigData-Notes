# Storm单机版本环境搭建

### 1. 安装环境要求

> you need to install Storm's dependencies on Nimbus and the worker machines. These are:
>
> 1. Java 7+ (Apache Storm 1.x is tested through travis ci against both java 7 and java 8 JDKs)
> 2. Python 2.6.6 (Python 3.x should work too, but is not tested as part of our CI enviornment)

按照[官方文档](http://storm.apache.org/releases/1.2.2/Setting-up-a-Storm-cluster.html) 的说明：storm 运行依赖于 Java 7+ 和 Python 2.6.6 +，所以需要预先安装这两个软件。由于这两个软件在多个框架中都有依赖，其安装步骤单独整理至  ：

+ [Linux 环境下 JDK 安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)

+ [Linux 环境下 Python 安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下Python安装.md)

  

### 2. 下载并解压

下载并解压，官方下载地址：http://storm.apache.org/downloads.html 

```shell
# tar -zxvf apache-storm-1.2.2.tar.gz
```

### 3. 配置环境变量

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



### 4. 启动相关进程

因为要启动多个进程，所以统一采用后台进程的方式启动。进入到 `${STORM_HOME}/bin` 目录下，依次执行下面的命令：

```shell
# 启动zookeeper
nohup sh storm dev-zookeeper &
# 启动主节点 nimbus
nohup sh storm nimbus &
# 启动从节点 supervisor 
nohup sh storm supervisor &
# 启动UI界面 ui  
nohup sh storm ui &
# 启动日志查看服务 logviewer 
nohup sh storm logviewer &
```



### 5. 验证是否启动成功

验证方式一：jps 查看进程：

```shell
[root@hadoop001 app]# jps
1074 nimbus
1283 Supervisor
620 dev_zookeeper
1485 core
9630 logviewer
```

验证方式二： 访问 8080 端口，查看 Web-UI 界面：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm-web-ui.png"/> </div>


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>