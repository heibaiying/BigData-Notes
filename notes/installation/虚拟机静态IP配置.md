# 虚拟机静态IP配置

>  虚拟机环境：centos 7.6



### 1. 查看当前网卡名称

​	本机网卡名称为`enp0s3`

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/en0s3.png"/> </div>

### 2. 编辑网络配置文件

```shell
# vim /etc/sysconfig/network-scripts/ifcfg-enp0s3
```

添加如下网络配置，指明静态IP和DNS：

```shell
BOOTPROTO=static
IPADDR=192.168.200.226
NETMASK=255.255.255.0
GATEWAY=192.168.200.254
DNS1=114.114.114.114
```

修改后完整配置如下：

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/ifconfig.png"/> </div>

### 3. 重启网络服务

```shell
#  systemctl restart network
```

