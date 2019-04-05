# Linux下Flume的安装


## 一、前置条件

Flume需要依赖JDK环境，JDK安装方式见本仓库：

> [Linux环境下JDK安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/Linux下JDK安装.md)



## 二 、安装步骤

### 2.1 下载并解压

下载所需版本的Flume，这里我下载的是`cdh5.15.2`版本的Flume。下载地址为：http://archive.cloudera.com/cdh5/cdh/5/

```shell
# 下载后进行解压
tar -zxvf  flume-ng-1.6.0-cdh5.15.2.tar.gz
```

### 2.2 配置环境变量

```shell
# vim /etc/profile
```

添加环境变量：

```shell
export FLUME_HOME=/usr/app/apache-flume-1.6.0-cdh5.15.2-bin
export PATH=$FLUME_HOME/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
# source /etc/profile
```

### 2.3 修改配置

进入安装目录下的`conf/`目录，拷贝flume的环境配置模板`flume-env.sh.template`

```shell
# cp flume-env.sh.template flume-env.sh
```

修改安装目录下的`flume-env.sh`,指定JDK的安装路径：

```shell
# Enviroment variables can be set here.
export JAVA_HOME=/usr/java/jdk1.8.0_201
```

### 2.4 验证

由于已经将Flume的bin目录配置到环境变量，直接使用以下命令验证是否配置成功

```shell
# flume-ng version
```

出现对应的版本信息则代表配置成功

<div align="center"> ![flume-version](https://github.com/heibaiying/BigData-Notes/blob/master/pictures/flume-version.png)</div>

