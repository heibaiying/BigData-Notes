# 虚拟机静态IP及多IP配置

>  虚拟机环境：centos 7.6



<nav>
<a href="#一虚拟机静态IP配置">一、虚拟机静态IP配置</a><br/>
<a href="#二虚拟机多个静态IP配置">二、虚拟机多个静态IP配置</a><br/>
</nav>



## 一、虚拟机静态IP配置

### 1. 查看当前网卡名称

​	使用`ifconfig`，本机网卡名称为`enp0s3`

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/en0s3.png"/> </div>

### 2. 编辑网络配置文件

```shell
# vim /etc/sysconfig/network-scripts/ifcfg-enp0s3
```

添加如下网络配置：

+ IPADDR需要和宿主机同一个网段；
+ GATEWAY保持和宿主机一致；

```properties
BOOTPROTO=static
IPADDR=192.168.0.107
NETMASK=255.255.255.0
GATEWAY=192.168.0.1
DNS1=192.168.0.1
ONBOOT=yes
```

我的主机配置：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/ipconfig.png"/> </div>

修改后完整配置如下：

```properties
<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/virtualbox-multi-network.png"/> </div>TYPE=Ethernet
PROXY_METHOD=none
BROWSER_ONLY=no
BOOTPROTO=static
IPADDR=192.168.0.107
NETMASK=255.255.255.0
GATEWAY=192.168.0.1
BROADCAST=192.168.0.255
DNS1=192.168.0.1
DEFROUTE=yes
IPV4_FAILURE_FATAL=no
IPV6INIT=yes
IPV6_AUTOCONF=yes
IPV6_DEFROUTE=yes
IPV6_FAILURE_FATAL=no
IPV6_ADDR_GEN_MODE=stable-privacy
NAME=enp0s3
UUID=03d45df1-8514-4774-9b47-fddd6b9d9fca
DEVICE=enp0s3
ONBOOT=yes
```

### 3. 重启网络服务

```shell
#  systemctl restart network
```



## 二、虚拟机多个静态IP配置

这里说一下多个静态IP的使用场景：主要是针对同一台电脑在经常在不同网络环境使用（办公，家庭，学习等），配置好多个IP后，在`hosts`文件中映射到同一个主机名，这样在不同网络中就可以直接启动Hadoop等软件。

### 1. 配置多网卡

这里我是用的虚拟机是virtualBox，开启多网卡配置方式如下：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/virtualbox-multi-network.png"/> </div>

### 2. 查看网卡名称

使用`ifconfig`，查看第二块网卡名称，这里我的名称为`enp0s8`。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mutli-net-ip.png"/> </div>

### 3. 配置第二块网卡

开启多网卡后并不会自动生成配置文件，需要拷贝`ifcfg-enp0s3`进行修改。

```shell
# cp ifcfg-enp0s3 ifcfg-enp0s8
```

静态IP配置方法如上，这里不再赘述。除了静态IP参数外，以下三个参数还需要修改，UUID必须与`ifcfg-enp0s3`中的不一样：

```shell
NAME=enp0s8
UUID=03d45df1-8514-4774-9b47-fddd6b9d9fcb
DEVICE=enp0s8
```

### 4. 重启网络服务器

```shell
#  systemctl restart network
```

