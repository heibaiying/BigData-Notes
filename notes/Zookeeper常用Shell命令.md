# Zookeeper常用Shell命令

## 一、节点增删改查

### 1.1 help命令

使用help命令可以查看所有支持的命令及其格式。

### 1.2 查看节点列表

```shell
ls path
```

### 1.3 新增节点

```shell
create [-s] [-e] path data acl   #其中-s为有序节点，-e临时节点
```

创建节点并将数据绑定到该节点：

```shell
create /hadoop 123456
```

创建有序节点，此时创建的节点名为指定节点名+自增序号：

```shell
[zk: localhost:2181(CONNECTED) 23] create -s /a  "aaa"
Created /a0000000022
[zk: localhost:2181(CONNECTED) 24] create -s /b  "bbb"
Created /b0000000023
[zk: localhost:2181(CONNECTED) 25] create -s /c  "ccc"
Created /c0000000024
```

创建临时节点，临时节点会在会话结束后被删除：

```shell
[zk: localhost:2181(CONNECTED) 26] create -e /tmp  "tmp"
Created /tmp
```

### 1.4 查看节点

#### 1. 获取节点数据

```shell
# 格式
get path [watch] 
```

```shell
[zk: localhost:2181(CONNECTED) 31] get /hadoop
123456   #节点数据
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14b
mtime = Fri May 24 17:03:06 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
```

节点各个属性如下表，其中一个重要的概念是Zxid(ZooKeeper Transaction  Id)，ZooKeeper节点的每个更改都具有唯一的zxid，如果zxid1小于zxid2，则zxid1更改发生在zxid2更改之前。

| **状态属性**   | **说明**                                                     |
| -------------- | ------------------------------------------------------------ |
| cZxid          | 数据节点创建时的事务ID                                       |
| ctime          | 数据节点创建时的时间                                         |
| mZxid          | 数据节点最后一次更新时的事务ID                               |
| mtime          | 数据节点最后一次更新时的时间                                 |
| pZxid          | 数据节点的子节点最后一次被修改时的事务ID                     |
| cversion       | 子节点的版本号                                               |
| dataVersion    | 数据节点的版本号                                             |
| aclVersion     | 数据节点的ACL版本号                                          |
| ephemeralOwner | 如果节点是临时节点，则表示创建该节点的会话的SessionID；如果节点是持久节点，则该属性值为0 |
| dataLength     | 数据内容的长度                                               |
| numChildren    | 数据节点当前的子节点个数                                     |

#### 2. 查看节点状态

可以使用stat命令查看节点状态，它的返回值和get命令类似，但不会返回节点数据。

```shell
[zk: localhost:2181(CONNECTED) 32] stat /hadoop
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14b
mtime = Fri May 24 17:03:06 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 6
numChildren = 0
```

### 1.5 更新节点

更改节点和新增节点都是`set命令`，可以直接进行修改，如下：

```shell
[zk: localhost:2181(CONNECTED) 33] set /hadoop 345
cZxid = 0x14b
ctime = Fri May 24 17:03:06 CST 2019
mZxid = 0x14c
mtime = Fri May 24 17:13:05 CST 2019
pZxid = 0x14b
cversion = 0
dataVersion = 1  # 注意更改后此时版本号为1，默认创建时为0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 3
numChildren = 0
```

也可以基于版本号进行更改，这种实现类似于乐观锁机制，当你传入的数据版本号(dataVersion)和当前节点的数据版本号不符合时，zookeeper会拒绝本次修改：

```shell
[zk: localhost:2181(CONNECTED) 34] set /hadoop 678 0
version No is not valid : /hadoop    #无效的版本号
```

### 1.6 删除节点

删除节点的语法如下：

```shell
delete path [version]
```

和更新节点数据一样，也可以传入版本号，当你传入的数据版本号(dataVersion)和当前节点的数据版本号不符合时，zookeeper不会执行删除。

```shell
[zk: localhost:2181(CONNECTED) 36] delete /hadoop 0
version No is not valid : /hadoop   #无效的版本号
[zk: localhost:2181(CONNECTED) 37] delete /hadoop 1
[zk: localhost:2181(CONNECTED) 38]
```







## 五、 zookeeper 四字命令

| 命令 | 功能描述                                                     |
| ---- | ------------------------------------------------------------ |
| conf | 打印服务配置的详细信息。                                     |
| cons | 列出连接到此服务器的所有客户端的完整连接/会话详细信息。包括接收/发送的数据包数量，会话ID，操作延迟，上次执行的操作等信息。 |
| dump | 列出未完成的会话和临时节点。这只适用于leader节点。           |
| envi | 打印服务环境的详细信息。                                     |
| ruok | 测试服务是否处于正确状态。如果正确则返回“imok”，否则不做任何相应。 |
| stat | 列出服务器和连接客户端的简要详细信息。                       |
| wchs | 列出所有watch的简单信息。                                    |
| wchc | 按会话列出服务器watch的详细信息。                            |
| wchp | 按路径列出服务器watch的详细信息。                            |

> 更多四字命令可以参阅官方文档：https://zookeeper.apache.org/doc/current/zookeeperAdmin.html

使用前需要使用 `yum install nc` 安装 nc 命令：

```shell
[root@hadoop001 bin]# echo stat | nc localhost 2181
Zookeeper version: 3.4.13-2d71af4dbe22557fda74f9a9b4309b15a7487f03, 
built on 06/29/2018 04:05 GMT
Clients:
 /0:0:0:0:0:0:0:1:50584[1](queued=0,recved=371,sent=371)
 /0:0:0:0:0:0:0:1:50656[0](queued=0,recved=1,sent=0)
Latency min/avg/max: 0/0/19
Received: 372
Sent: 371
Connections: 2
Outstanding: 0
Zxid: 0x150
Mode: standalone
Node count: 167
```

