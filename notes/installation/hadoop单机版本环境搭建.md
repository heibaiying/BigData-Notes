# Hadoop单机版环境搭建




<nav>
<a href="#一安装JDK">一、安装JDK</a><br/>
<a href="#二配置-SSH-免密登录">二、配置 ssh 免密登录</a><br/>
<a href="#三HadoopHDFS安装">三、Hadoop(HDFS)安装</a><br/>
</nav>



## 一、安装JDK

Hadoop 需要在java环境下运行，所以需要先安装jdk,安装步骤见[Linux下JDK的安装](https://github.com/heibaiying/BigData-Notes/blob/master/notes/installation/JDK%E5%AE%89%E8%A3%85.md)



## 二、配置 SSH 免密登录

Hadoop 组件之间的各个节点需要进行通讯，所以需要配置ssh 免密登录。

#### 2.1 配置ip地址和主机名映射，在配置文件末尾添加ip地址和主机名映射

```shell
vim /etc/hosts
# 文件末尾增加
192.168.43.202  hadoop001
```

####  2.2  执行下面命令行，一路回车，生成公匙和私匙

```
ssh-keygen -t rsa
```

#### 3.3 进入`~/.ssh`目录下，查看生成的公匙和私匙，并将公匙写入到授权文件

```shell
[root@@hadoop001 sbin]#  cd ~/.ssh
[root@@hadoop001 .ssh]# ll
-rw-------. 1 root root 1675 3月  15 09:48 id_rsa
-rw-r--r--. 1 root root  388 3月  15 09:48 id_rsa.pub
```

```shell
# 写入公匙到授权文件
[root@hadoop001 .ssh]# cat id_rsa.pub >> authorized_keys
[root@hadoop001 .ssh]# chmod 600 authorized_keys
```



## 三、Hadoop(HDFS)安装



#### 3.1 下载CDH 版本的Hadoop

从[CDH官方下载地址](http://archive.cloudera.com/cdh5/cdh/5/)下载所需版本的hadoop（本用例下载的版本为hadoop-2.6.0-cdh5.15.2.tar.gz ）,上传至服务器对应文件夹（这里我传至新建的/usr/app/ 目录）；



#### 3.2 解压软件压缩包

```shell
tar -zvxf hadoop-2.6.0-cdh5.15.2.tar.gz 
```



#### 3.3 修改Hadoop相关配置文件

cd 到 安装目录的/etc/hadoop/ 文件夹下：

```shell
[root@hadoop001 hadoop-2.6.0-cdh5.15.2]# cd etc/hadoop
```

1. 修改 `hadoop-env.sh` , 指定jdk 安装路径

```shell
# The java implementation to use.
export  JAVA_HOME=/usr/java/jdk1.8.0_201/
```

2. 修改`core-site.xml`，添加如下配置，指定hdfs地址：

```xml
<configuration>
        <property>
            <name>fs.defaultFS</name>
            <value>hdfs://hadoop001:8020</value>
        </property>
</configuration>
```

3. 修改`hdfs-site.xml`添加如下配置，指定副本系数和临时文件存储位置，由于这里我们搭建是单机版本，所以指定dfs的副本系数为1。

```xml
<configuration>
        <property>
                <name>dfs.replication</name>
                 <value>1</value>
        </property>
        <property>
                 <name>hadoop.tmp.dir</name>
                 <value>/usr/app/tmp</value>
        </property>
</configuration>
```

4. 修改`slaves`文件，由于是单机版本，所以指定本机为从节点，修改后`slaves`文件内容如下：

```shell
[root@hadoop001 hadoop]# cat slaves
hadoop001
```



#### 3.4 关闭防火墙

由于防火墙可能会影响节点间通讯，所以建议关闭，执行命令：

```shell
# 查看防火墙状态
sudo firewall-cmd --state
# 关闭防火墙:
sudo systemctl stop firewalld.service
```



#### 3.5 启动HDFS

1. 第一次执行的时候一定要格式化文件系统，执行以下命令格式化文件系统（hdfs命令位于安装目录的bin目录下）。

```shell
[root@hadoop001 bin]# ./hdfs namenode -format
```

2. 执行sbin目录下的`start-dfs.sh`脚本，启动hdfs

```shell
[root@hadoop001 sbin]# ./start-dfs.sh
```



#### 3.6 验证是否启动成功

方式一：执行jps 查看 NameNode和DataNode的进程是否已经存在

```shell
[root@hadoop001 hadoop-2.6.0-cdh5.15.2]# jps
5379 Jps
11413 NameNode
11529 DataNode
11789 SecondaryNameNode
```



方式二：访问50070端口 http://192.168.43.202:50070 。如果jps查看进程均以启动，但是无法访问页面，则需要关闭防火墙。

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop安装验证.png"/> </div>
