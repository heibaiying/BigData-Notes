# Zookeeper常用Shell命令

## 一、节点增删改查

### 1.1 启动服务和连接服务

```shell
# 启动服务
bin/zkServer.sh start

#连接服务 不指定服务地址则默认连接到localhost:2181
zkCli.sh -server hadoop001:2181
```

### 1.2 help命令

使用help命令可以查看所有支持的命令及其格式。

### 1.3 查看节点列表

查看节点列表有`ls path`和 `ls2 path`两个命令，后者是前者的增强，不仅可以查看指定路径下所有节点的列表，还可以查看当前节点的信息。

```shell
[zk: localhost:2181(CONNECTED) 0] ls /
[cluster, controller_epoch, brokers, storm, zookeeper, admin,  ...]
[zk: localhost:2181(CONNECTED) 1] ls2 /
[cluster, controller_epoch, brokers, storm, zookeeper, admin, ....]
cZxid = 0x0
ctime = Thu Jan 01 08:00:00 CST 1970
mZxid = 0x0
mtime = Thu Jan 01 08:00:00 CST 1970
pZxid = 0x130
cversion = 19
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 0
numChildren = 11
```

### 1.4 新增节点

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

### 1.5 查看节点

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

### 1.6 更新节点

更新节点的命令是`set`，可以直接进行修改，如下：

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

### 1.7 删除节点

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

要想删除某个节点及其所有后代节点，可以使用递归删除，命令为`rmr path`。

## 二、监听器

### 2.1 get path [watch]

使用`get path [watch]`注册的监听器能够在节点内容发生改变的时候，向客户端发出通知。需要注意的是zookeeper的触发器是一次性的(One-time trigger)，即触发一次后就会立即失效。

```shell
[zk: localhost:2181(CONNECTED) 4] get /hadoop  watch
[zk: localhost:2181(CONNECTED) 5] set /hadoop 45678
WATCHER::
WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop  #节点值改变
```

### 2.2 stat path [watch]

使用`stat path [watch]`注册的监听器能够在节点状态发生改变的时候，向客户端发出通知。

```shell
[zk: localhost:2181(CONNECTED) 7] stat /hadoop watch
[zk: localhost:2181(CONNECTED) 8] set /hadoop 112233
WATCHER::
WatchedEvent state:SyncConnected type:NodeDataChanged path:/hadoop  #节点值改变
```

### 2.3 ls\ls2 path  [watch]

使用`ls path [watch]`或`ls2 path [watch]`注册的监听器能够监听该节点下所有**子节点**的增加和删除操作。

```shell
[zk: localhost:2181(CONNECTED) 9] ls /hadoop watch
[]
[zk: localhost:2181(CONNECTED) 10] create  /hadoop/yarn "aaa"
WATCHER::
WatchedEvent state:SyncConnected type:NodeChildrenChanged path:/hadoop
```



## 三、权限管理

### 3.1 设置与查看权限

想要给某个节点设置权限(ACL)，有以下两个可选的命令：

```shell
 # 1.给已有节点赋予权限
 setAcl path acl
 
 # 2.在创建节点时候指定权限
 create [-s] [-e] path data acl
```

查看指定节点的权限命令如下：

```shell
getAcl path
```

### 3.2 权限组成

Zookeeper的权限由[scheme : id :permissions]三部分组成，其中Schemes和Permissions内置的可选项分别如下：

Permissions可选项：

- CREATE：允许创建子节点；
- READ：允许从节点获取数据并列出其子节点；
- WRITE：允许为节点设置数据；
- DELETE：允许删除子节点；
- ADMIN：允许为节点设置权限。  

Schemes可选项：

- world：默认模式, 所有客户端都拥有指定权限。world下只有一个id选项，就是anyone，通常组合写法为`world:anyone:[permissons]`；
- auth：只有经过认证的用户, 才拥有指定的权限。通常组合写法为`auth:user:password:[permissons]`，使用这种模式时，你需要先进行登录，之后采用auth模式时，user和password都将使用登录的用户名和密码；
- digest：只有经过认证的用户, 才拥有指定的权限。通常组合写法为`auth:user:BASE64(SHA1(password)):[permissons]`,这种形式下的密码必须通过SHA1和BASE64进行双重加密；
- ip：限制只有特定IP的客户端才拥有指定的权限。通常组成写法为`ip:182.168.0.168:[permissions]`；
- super：代表超级管理员，拥有所有的权限，需要修改Zookeeper启动脚本进行配置。



### 3.3 添加认证信息

可以使用如下所示的命令为当前Session添加用户认证信息，等价于登录操作。

```shell
# 格式
addauth scheme auth 

#示例：添加用户名为heibai,密码为root的用户认证信息
addauth digest heibai:root 
```



### 3.4 权限设置示例

#### 1. world模式

world是一种默认的模式，即创建时如果不指定权限，则默认的权限就是world。

```shell
[zk: localhost:2181(CONNECTED) 32] create /hadoop 123
Created /hadoop
[zk: localhost:2181(CONNECTED) 33] getAcl /hadoop
'world,'anyone    #默认的权限
: cdrwa
[zk: localhost:2181(CONNECTED) 34] setAcl /hadoop world:anyone:cwda   # 修改节点，不允许所有客户端读
....
[zk: localhost:2181(CONNECTED) 35] get /hadoop
Authentication is not valid : /hadoop     # 权限不足

```

#### 2. auth模式

```shell
[zk: localhost:2181(CONNECTED) 36] addauth digest heibai:heibai  # 登录
[zk: localhost:2181(CONNECTED) 37] setAcl /hadoop auth::cdrwa    # 设置权限
[zk: localhost:2181(CONNECTED) 38] getAcl /hadoop                # 获取权限
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=   #用户名和密码(密码经过加密处理)，注意返回的权限类型是digest
: cdrwa

#用户名和密码都是使用登录的用户名和密码，即使你在创建权限时候进行指定也是无效的
[zk: localhost:2181(CONNECTED) 39] setAcl /hadoop auth:root:root:cdrwa    #指定用户名和密码为root
[zk: localhost:2181(CONNECTED) 40] getAcl /hadoop
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=  #无效，使用的用户名和密码依然还是heibai
: cdrwa

```

#### 3. digest模式

```shell
[zk:44] create /spark "spark" digest:heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=:cdrwa  #指定用户名和加密后的密码
[zk:45] getAcl /spark  #获取权限
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=   # 返回的权限类型是digest
: cdrwa
```

到这里你可以发现使用auth模式设置的权限和使用digest模式设置的权限，在最终结果上，得到的权限模式都是`digest`。某种程度上，你可以把auth模式理解成是digest模式的一种简便实现。因为在digest模式下，每次设置都需要书写用户名和加密后的密码，这是比较繁琐的，采用auth模式，则可以在登录一次后就可以不用重复书写了。

#### 4. ip模式

限定只有特定的ip才能访问。

```shell
[zk: localhost:2181(CONNECTED) 46] create  /hive "hive" ip:192.168.0.108:cdrwa  
[zk: localhost:2181(CONNECTED) 47] get /hive
Authentication is not valid : /hive  # 当前主机已经不能访问
```

这里可以使用限定IP的主机客户端进行访问，也可以使用下面的super模式配置超级管理员进行访问。

#### 5. super模式

需要修改启动脚本`zkServer.sh`,在指定位置添加管理员账户和密码信息：

```shell
"-Dzookeeper.DigestAuthenticationProvider.superDigest=heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=" 
```

![zookeeper-super](D:\BigData-Notes\pictures\zookeeper-super.png)

修改完成后需要使用`zkServer.sh restart`重启服务，此时再次访问限制IP的节点：

```shell
[zk: localhost:2181(CONNECTED) 0] get /hive  #访问受限
Authentication is not valid : /hive
[zk: localhost:2181(CONNECTED) 1] addauth digest heibai:heibai  # 登录(添加认证信息)
[zk: localhost:2181(CONNECTED) 2] get /hive  #成功访问
hive
cZxid = 0x158
ctime = Sat May 25 09:11:29 CST 2019
mZxid = 0x158
mtime = Sat May 25 09:11:29 CST 2019
pZxid = 0x158
cversion = 0
dataVersion = 0
aclVersion = 0
ephemeralOwner = 0x0
dataLength = 4
numChildren = 0
```

## 四、 zookeeper 四字命令

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

使用前需要使用 `yum install nc` 安装 nc 命令，使用示例如下：

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

