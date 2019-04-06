# Linux环境下Hive的安装

> Hive 版本 ： hive-1.1.0-cdh5.15.2.tar.gz
>
> 系统环境：Centos 7.6

### 1.1 下载并解压

下载所需版本的Hive，这里我下载的是`cdh5.15.2`版本的Hive。下载地址为：http://archive.cloudera.com/cdh5/cdh/5/

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

进入安装目录下的`conf/`目录，拷贝Hive的环境配置模板`flume-env.sh.template`

```shell
cp hive-env.sh.template hive-env.sh
```

修改`hive-env.sh`,指定Hadoop的安装路径：

```shell
HADOOP_HOME=/usr/app/hadoop-2.6.0-cdh5.15.2
```

**2. hive-site.xml**

新建hive-site.xml 文件，内容如下，主要是配置存放元数据的MySQL数据库的地址、驱动、用户名和密码等信息：

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

将MySQL驱动拷贝到Hive安装目录的`lib`目录下, MySQL驱动的下载地址为https://dev.mysql.com/downloads/connector/j/  , 在本仓库的resources目录下我也上传了一份，有需要的可以自行下载。

![hive-mysql](D:\BigData-Notes\pictures\hive-mysql.png)



### 1.5 初始化元数据库

+ 当使用的 hive 是1.x版本时，可以不进行初始化操作，Hive会在第一次启动的时候会自动进行初始化，但不会生成所有的元数据信息表，只会初始化必要的一部分，在之后的使用中用到其余表时会自动创建；

+ 当使用的 hive 是2.x版本时，必须手动初始化元数据库。初始化命令：

  ```shell
  # schematool 命令在安装目录的bin目录下，由于上面已经配置过环境变量，在任意位置执行即可
  schematool -dbType mysql -initSchema
  ```

本用例使用的CDH版本是`hive-1.1.0-cdh5.15.2.tar.gz`,对应`Hive 1.1.0` 版本，可以跳过这一步。

### 1.6 启动

由于已经将Hive的bin目录配置到环境变量，直接使用以下命令启动，成功进入交互式命令行后执行`show databases`命令，无异常则代表搭建成功。

```shell
# Hive
```

![hive-install](D:\BigData-Notes\pictures\hive-install-2.png)

在Mysql中也能看到Hive创建的库和存放元数据信息的表

![hive-mysql-tables](D:\BigData-Notes\pictures\hive-mysql-tables.png)