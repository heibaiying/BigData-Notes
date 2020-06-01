## Linux下Python安装

>**系统环境**：centos 7.6
>
>**Python 版本**：Python-3.6.8

### 1. 环境依赖

Python3.x 的安装需要依赖这四个组件：gcc， zlib，zlib-devel，openssl-devel；所以需要预先安装，命令如下：

```shell
yum install gcc -y
yum install zlib -y
yum install zlib-devel -y
yum install openssl-devel -y
```

### 2. 下载编译

Python 源码包下载地址： https://www.python.org/downloads/

```shell
# wget https://www.python.org/ftp/python/3.6.8/Python-3.6.8.tgz
```

### 3. 解压编译

```shell
# tar -zxvf Python-3.6.8.tgz
```

进入根目录进行编译，可以指定编译安装的路径，这里我们指定为 `/usr/app/python3.6` ：

```shell
# cd Python-3.6.8
# ./configure --prefix=/usr/app/python3.6
# make && make install
```

### 4. 环境变量配置

```shell
vim  /etc/profile
```

```shell
export PYTHON_HOME=/usr/app/python3.6
export  PATH=${PYTHON_HOME}/bin:$PATH
```

使得配置的环境变量立即生效：

```shell
source /etc/profile
```

### 5. 验证安装是否成功

输入 `python3` 命令，如果能进入 python 交互环境，则代表安装成功：

```shell
[root@hadoop001 app]# python3
Python 3.6.8 (default, Mar 29 2019, 10:17:41)
[GCC 4.8.5 20150623 (Red Hat 4.8.5-36)] on linux
Type "help", "copyright", "credits" or "license" for more information.
>>> 1+1
2
>>> exit()
[root@hadoop001 app]#
```



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>