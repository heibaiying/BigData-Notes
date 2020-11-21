# Linux环境下Hive的安装

<nav>
<a href="#一安装Hive">一、安装Hive</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-下载并解压">1.1 下载并解压</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-配置环境变量">1.2 配置环境变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-修改配置">1.3 修改配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-拷贝数据库驱动">1.4 拷贝数据库驱动</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-初始化元数据库">1.5 初始化元数据库</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#16-启动">1.6 启动</a><br/>
<a href="#二HiveServer2beeline">二、HiveServer2/beeline</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-修改Hadoop配置">2.1 修改Hadoop配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-启动hiveserver2">2.2 启动hiveserver2</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-使用beeline">2.3 使用beeline</a><br/>
</nav>

## 一、安装Hive

### 1.1 下载并解压

下载所需版本的 Hive，这里我下载版本为 `cdh5.15.2`。下载地址：http://archive.cloudera.com/cdh5/cdh/5/

```shell
# 下载后进行解压
 tar -zxvf hive-1.1.0-cdh5.15.2.tar.gz
```

### 1.2 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export HIVE_HOME=/usr/app/hive-1.1.0-cdh5.15.2
export PATH=$HIVE_HOME/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
# source /etc/profile
```

### 1.3 修改配置

**1. hive-env.sh**

进入安装目录下的 `conf/` 目录，拷贝 Hive 的环境配置模板 `hive-env.sh.template`

```shell
cp hive-env.sh.template hive-env.sh
```

修改 `hive-env.sh`，指定 Hadoop 的安装路径：

```shell
HADOOP_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2
```

**2. hive-site.xml**

新建 hive-site.xml 文件，内容如下，主要是配置存放元数据的 MySQL 的地址、驱动、用户名和密码等信息：

```xml
<?xml version="1.0"?>
<?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

<configuration>
  <property>
    <name>javax.jdo.option.ConnectionURL</name>
    <value>jdbc:mysql://hadoop001:3306/hadoop_hive?createDatabaseIfNotExist=true</value>
  </property>
  
  <property>
    <name>javax.jdo.option.ConnectionDriverName</name>
    <value>com.mysql.jdbc.Driver</value>
  </property>
  
  <property>
    <name>javax.jdo.option.ConnectionUserName</name>
    <value>root</value>
  </property>
  
  <property>
    <name>javax.jdo.option.ConnectionPassword</name>
    <value>root</value>
  </property>

</configuration>
```



### 1.4 拷贝数据库驱动

将 MySQL 驱动包拷贝到 Hive 安装目录的 `lib` 目录下, MySQL 驱动的下载地址为：https://dev.mysql.com/downloads/connector/j/  , 在本仓库的[resources](https://github.com/heibaiying/BigData-Notes/tree/master/resources) 目录下我也上传了一份，有需要的可以自行下载。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-mysql.png"/> </div>



### 1.5 初始化元数据库

+ 当使用的 hive 是 1.x 版本时，可以不进行初始化操作，Hive 会在第一次启动的时候会自动进行初始化，但不会生成所有的元数据信息表，只会初始化必要的一部分，在之后的使用中用到其余表时会自动创建；

+ 当使用的 hive 是 2.x 版本时，必须手动初始化元数据库。初始化命令：

  ```shell
  # schematool 命令在安装目录的 bin 目录下，由于上面已经配置过环境变量，在任意位置执行即可
  schematool -dbType mysql -initSchema
  ```

这里我使用的是 CDH 的 `hive-1.1.0-cdh5.15.2.tar.gz`，对应 `Hive 1.1.0` 版本，可以跳过这一步。

### 1.6 启动

由于已经将 Hive 的 bin 目录配置到环境变量，直接使用以下命令启动，成功进入交互式命令行后执行 `show databases` 命令，无异常则代表搭建成功。

```shell
# hive
```

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-install-2.png"/> </div>

在 Mysql 中也能看到 Hive 创建的库和存放元数据信息的表

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-mysql-tables.png"/> </div>



## 二、HiveServer2/beeline

Hive 内置了 HiveServer 和 HiveServer2 服务，两者都允许客户端使用多种编程语言进行连接，但是 HiveServer 不能处理多个客户端的并发请求，因此产生了 HiveServer2。HiveServer2（HS2）允许远程客户端可以使用各种编程语言向 Hive 提交请求并检索结果，支持多客户端并发访问和身份验证。HS2 是由多个服务组成的单个进程，其包括基于 Thrift 的 Hive 服务（TCP 或 HTTP）和用于 Web UI 的 Jetty Web 服务。

 HiveServer2 拥有自己的 CLI 工具——Beeline。Beeline 是一个基于 SQLLine 的 JDBC 客户端。由于目前 HiveServer2 是 Hive 开发维护的重点，所以官方更加推荐使用 Beeline 而不是 Hive CLI。以下主要讲解 Beeline 的配置方式。



### 2.1 修改Hadoop配置

修改 hadoop 集群的 core-site.xml 配置文件，增加如下配置，指定 hadoop 的 root 用户可以代理本机上所有的用户。

```xml
<property>
 <name>hadoop.proxyuser.root.hosts</name>
 <value>*</value>
</property>
<property>
 <name>hadoop.proxyuser.root.groups</name>
 <value>*</value>
</property>
```

之所以要配置这一步，是因为 hadoop 2.0 以后引入了安全伪装机制，使得 hadoop 不允许上层系统（如 hive）直接将实际用户传递到 hadoop 层，而应该将实际用户传递给一个超级代理，由该代理在 hadoop 上执行操作，以避免任意客户端随意操作 hadoop。如果不配置这一步，在之后的连接中可能会抛出 `AuthorizationException` 异常。

>关于 Hadoop 的用户代理机制，可以参考：[hadoop 的用户代理机制](https://blog.csdn.net/u012948976/article/details/49904675#官方文档解读) 或 [Superusers Acting On Behalf Of Other Users](http://hadoop.apache.org/docs/current/hadoop-project-dist/hadoop-common/Superusers.html)



### 2.2 启动hiveserver2

由于上面已经配置过环境变量，这里直接启动即可：

```shell
# nohup hiveserver2 &
```



### 2.3 使用beeline

可以使用以下命令进入 beeline 交互式命令行，出现 `Connected` 则代表连接成功。

```shell
# beeline -u jdbc:hive2://hadoop001:10000 -n root
```

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-beeline-cli.png"/> </div>


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>
