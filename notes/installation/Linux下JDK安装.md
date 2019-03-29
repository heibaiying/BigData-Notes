# Linux下JDK的安装



>**系统环境**：centos 7.6
>
>**JDK版本**：jdk 1.8.0_20



### 1. 下载jdk安装包

在[官网](https://www.oracle.com/technetwork/java/javase/downloads/index.html)下载所需版本的jdk，上传至服务器对应位置（这里我们下载的版本为[jdk1.8](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) ,上传至服务器的/usr/java/目录下）



### 2. 解压安装包

```shell
[root@ java]# tar -zxvf jdk-8u201-linux-x64.tar.gz
```



### 3. 设置环境变量

```shell
[root@ java]# vi /etc/profile
```

在文件末尾添加：

```shell
export JAVA_HOME=/usr/java/jdk1.8.0_201  
export JRE_HOME=${JAVA_HOME}/jre  
export CLASSPATH=.:${JAVA_HOME}/lib:${JRE_HOME}/lib  
export  PATH=${JAVA_HOME}/bin:$PATH
```



### 4. 执行source命令,使得配置立即生效

```shell
[root@ java]# source /etc/profile
```



### 5. 检查是否安装成功

```shell
[root@ java]# java -version
```

显示出对应的版本信息则代表安装成功。

```shell
java version "1.8.0_201"
Java(TM) SE Runtime Environment (build 1.8.0_201-b09)
Java HotSpot(TM) 64-Bit Server VM (build 25.201-b09, mixed mode)

```