# Sqoop 简介与安装

<nav>
<a href="#一Sqoop-简介">一、Sqoop 简介</a><br/>
<a href="#二安装">二、安装</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-下载并解压">2.1 下载并解压</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-配置环境变量">2.2 配置环境变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-修改配置">2.3 修改配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-拷贝数据库驱动">2.4 拷贝数据库驱动</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-验证">2.5 验证</a><br/>
</nav>


## 一、Sqoop 简介

Sqoop 是一个常用的数据迁移工具，主要用于在不同存储系统之间实现数据的导入与导出：

+ 导入数据：从 MySQL，Oracle 等关系型数据库中导入数据到 HDFS、Hive、HBase 等分布式文件存储系统中；

+ 导出数据：从 分布式文件系统中导出数据到关系数据库中。

其原理是将执行命令转化成 MapReduce 作业来实现数据的迁移，如下图：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/sqoop-tool.png"/> </div>

## 二、安装

版本选择：目前 Sqoop 有 Sqoop 1 和 Sqoop 2 两个版本，但是截至到目前，官方并不推荐使用 Sqoop 2，因为其与 Sqoop 1 并不兼容，且功能还没有完善，所以这里优先推荐使用 Sqoop 1。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/sqoop-version-selected.png"/> </div>



### 2.1 下载并解压

下载所需版本的 Sqoop ，这里我下载的是 `CDH` 版本的 Sqoop 。下载地址为：http://archive.cloudera.com/cdh5/cdh/5/

```shell
# 下载后进行解压
tar -zxvf  sqoop-1.4.6-cdh5.15.2.tar.gz
```

### 2.2 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export SQOOP_HOME=/usr/app/sqoop-1.4.6-cdh5.15.2
export PATH=$SQOOP_HOME/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
# source /etc/profile
```

### 2.3 修改配置

进入安装目录下的 `conf/` 目录，拷贝 Sqoop 的环境配置模板 `sqoop-env.sh.template`

```shell
# cp sqoop-env-template.sh sqoop-env.sh
```

修改 `sqoop-env.sh`，内容如下 (以下配置中 `HADOOP_COMMON_HOME` 和 `HADOOP_MAPRED_HOME` 是必选的，其他的是可选的)：

```shell
# Set Hadoop-specific environment variables here.
#Set path to where bin/hadoop is available
export HADOOP_COMMON_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2

#Set path to where hadoop-*-core.jar is available
export HADOOP_MAPRED_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2

#set the path to where bin/hbase is available
export HBASE_HOME=/usr/app/hbase-1.2.0-cdh5.15.2

#Set the path to where bin/hive is available
export HIVE_HOME=/usr/app/hive-1.1.0-cdh5.15.2

#Set the path for where zookeper config dir is
export ZOOCFGDIR=/usr/app/zookeeper-3.4.13/conf

```

### 2.4 拷贝数据库驱动

将 MySQL 驱动包拷贝到 Sqoop 安装目录的 `lib` 目录下, 驱动包的下载地址为 https://dev.mysql.com/downloads/connector/j/  。在本仓库的[resources](https://github.com/heibaiying/BigData-Notes/tree/master/resources) 目录下我也上传了一份，有需要的话可以自行下载。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/sqoop-mysql-jar.png"/> </div>



### 2.5 验证

由于已经将 sqoop 的 `bin` 目录配置到环境变量，直接使用以下命令验证是否配置成功：

```shell
# sqoop version
```

出现对应的版本信息则代表配置成功：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/sqoop-version.png"/> </div>

这里出现的两个 `Warning` 警告是因为我们本身就没有用到 `HCatalog` 和 `Accumulo`，忽略即可。Sqoop 在启动时会去检查环境变量中是否有配置这些软件，如果想去除这些警告，可以修改 `bin/configure-sqoop`，注释掉不必要的检查。

```shell
# Check: If we can't find our dependencies, give up here.
if [ ! -d "${HADOOP_COMMON_HOME}" ]; then
  echo "Error: $HADOOP_COMMON_HOME does not exist!"
  echo 'Please set $HADOOP_COMMON_HOME to the root of your Hadoop installation.'
  exit 1
fi
if [ ! -d "${HADOOP_MAPRED_HOME}" ]; then
  echo "Error: $HADOOP_MAPRED_HOME does not exist!"
  echo 'Please set $HADOOP_MAPRED_HOME to the root of your Hadoop MapReduce installation.'
  exit 1
fi

## Moved to be a runtime check in sqoop.
if [ ! -d "${HBASE_HOME}" ]; then
  echo "Warning: $HBASE_HOME does not exist! HBase imports will fail."
  echo 'Please set $HBASE_HOME to the root of your HBase installation.'
fi

## Moved to be a runtime check in sqoop.
if [ ! -d "${HCAT_HOME}" ]; then
  echo "Warning: $HCAT_HOME does not exist! HCatalog jobs will fail."
  echo 'Please set $HCAT_HOME to the root of your HCatalog installation.'
fi

if [ ! -d "${ACCUMULO_HOME}" ]; then
  echo "Warning: $ACCUMULO_HOME does not exist! Accumulo imports will fail."
  echo 'Please set $ACCUMULO_HOME to the root of your Accumulo installation.'
fi
if [ ! -d "${ZOOKEEPER_HOME}" ]; then
  echo "Warning: $ZOOKEEPER_HOME does not exist! Accumulo imports will fail."
  echo 'Please set $ZOOKEEPER_HOME to the root of your Zookeeper installation.'
fi
```



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>