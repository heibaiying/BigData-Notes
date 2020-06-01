# Zookeeper ACL

<nav>
<a href="#一前言">一、前言</a><br/>
<a href="#二使用Shell进行权限管理">二、使用Shell进行权限管理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-设置与查看权限">2.1 设置与查看权限</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-权限组成">2.2 权限组成</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-添加认证信息">2.3 添加认证信息</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-权限设置示例">2.4 权限设置示例</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#1-world模式">1. world模式</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#2-auth模式">2. auth模式</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#3-digest模式">3. digest模式</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#4-ip模式">4. ip模式</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#5-super模式">5. super模式</a><br/>
<a href="#三使用Java客户端进行权限管理">三、使用Java客户端进行权限管理</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-主要依赖">3.1 主要依赖</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-权限管理API">3.2 权限管理API</a><br/>
</nav>


## 一、前言

为了避免存储在 Zookeeper 上的数据被其他程序或者人为误修改，Zookeeper 提供了 ACL(Access Control Lists) 进行权限控制。只有拥有对应权限的用户才可以对节点进行增删改查等操作。下文分别介绍使用原生的 Shell 命令和 Apache Curator 客户端进行权限设置。

## 二、使用Shell进行权限管理

### 2.1 设置与查看权限

想要给某个节点设置权限 (ACL)，有以下两个可选的命令：

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

### 2.2 权限组成

Zookeeper 的权限由[scheme : id :permissions]三部分组成，其中 Schemes 和 Permissions 内置的可选项分别如下：

**Permissions 可选项**：

- **CREATE**：允许创建子节点；
- **READ**：允许从节点获取数据并列出其子节点；
- **WRITE**：允许为节点设置数据；
- **DELETE**：允许删除子节点；
- **ADMIN**：允许为节点设置权限。  

**Schemes 可选项**：

- **world**：默认模式，所有客户端都拥有指定的权限。world 下只有一个 id 选项，就是 anyone，通常组合写法为 `world:anyone:[permissons]`；
- **auth**：只有经过认证的用户才拥有指定的权限。通常组合写法为 `auth:user:password:[permissons]`，使用这种模式时，你需要先进行登录，之后采用 auth 模式设置权限时，`user` 和 `password` 都将使用登录的用户名和密码；
- **digest**：只有经过认证的用户才拥有指定的权限。通常组合写法为 `auth:user:BASE64(SHA1(password)):[permissons]`，这种形式下的密码必须通过 SHA1 和 BASE64 进行双重加密；
- **ip**：限制只有特定 IP 的客户端才拥有指定的权限。通常组成写法为 `ip:182.168.0.168:[permissions]`；
- **super**：代表超级管理员，拥有所有的权限，需要修改 Zookeeper 启动脚本进行配置。



### 2.3 添加认证信息

可以使用如下所示的命令为当前 Session 添加用户认证信息，等价于登录操作。

```shell
# 格式
addauth scheme auth 

#示例：添加用户名为heibai,密码为root的用户认证信息
addauth digest heibai:root 
```



### 2.4 权限设置示例

#### 1. world模式

world 是一种默认的模式，即创建时如果不指定权限，则默认的权限就是 world。

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
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=   #用户名和密码 (密码经过加密处理)，注意返回的权限类型是 digest
: cdrwa

#用户名和密码都是使用登录的用户名和密码，即使你在创建权限时候进行指定也是无效的
[zk: localhost:2181(CONNECTED) 39] setAcl /hadoop auth:root:root:cdrwa    #指定用户名和密码为 root
[zk: localhost:2181(CONNECTED) 40] getAcl /hadoop
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=  #无效，使用的用户名和密码依然还是 heibai
: cdrwa

```

#### 3. digest模式

```shell
[zk:44] create /spark "spark" digest:heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=:cdrwa  #指定用户名和加密后的密码
[zk:45] getAcl /spark  #获取权限
'digest,'heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=   # 返回的权限类型是 digest
: cdrwa
```

到这里你可以发现使用 `auth` 模式设置的权限和使用 `digest` 模式设置的权限，在最终结果上，得到的权限模式都是 `digest`。某种程度上，你可以把 `auth` 模式理解成是 `digest` 模式的一种简便实现。因为在 `digest` 模式下，每次设置都需要书写用户名和加密后的密码，这是比较繁琐的，采用 `auth` 模式就可以避免这种麻烦。

#### 4. ip模式

限定只有特定的 ip 才能访问。

```shell
[zk: localhost:2181(CONNECTED) 46] create  /hive "hive" ip:192.168.0.108:cdrwa  
[zk: localhost:2181(CONNECTED) 47] get /hive
Authentication is not valid : /hive  # 当前主机已经不能访问
```

这里可以看到当前主机已经不能访问，想要能够再次访问，可以使用对应 IP 的客户端，或使用下面介绍的 `super` 模式。

#### 5. super模式

需要修改启动脚本 `zkServer.sh`，并在指定位置添加超级管理员账户和密码信息：

```shell
"-Dzookeeper.DigestAuthenticationProvider.superDigest=heibai:sCxtVJ1gPG8UW/jzFHR0A1ZKY5s=" 
```

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/zookeeper-super.png"/> </div>

修改完成后需要使用 `zkServer.sh restart` 重启服务，此时再次访问限制 IP 的节点：

```shell
[zk: localhost:2181(CONNECTED) 0] get /hive  #访问受限
Authentication is not valid : /hive
[zk: localhost:2181(CONNECTED) 1] addauth digest heibai:heibai  # 登录 (添加认证信息)
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

## 三、使用Java客户端进行权限管理

### 3.1 主要依赖

这里以 Apache Curator 为例，使用前需要导入相关依赖，完整依赖如下：

```xml
<dependencies>
    <!--Apache Curator 相关依赖-->
    <dependency>
        <groupId>org.apache.curator</groupId>
        <artifactId>curator-framework</artifactId>
        <version>4.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.curator</groupId>
        <artifactId>curator-recipes</artifactId>
        <version>4.0.0</version>
    </dependency>
    <dependency>
        <groupId>org.apache.zookeeper</groupId>
        <artifactId>zookeeper</artifactId>
        <version>3.4.13</version>
    </dependency>
    <!--单元测试相关依赖-->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
    </dependency>
</dependencies>
```

### 3.2 权限管理API

 Apache Curator 权限设置的示例如下：

```java
public class AclOperation {

    private CuratorFramework client = null;
    private static final String zkServerPath = "192.168.0.226:2181";
    private static final String nodePath = "/hadoop/hdfs";

    @Before
    public void prepare() {
        RetryPolicy retryPolicy = new RetryNTimes(3, 5000);
        client = CuratorFrameworkFactory.builder()
                .authorization("digest", "heibai:123456".getBytes()) //等价于 addauth 命令
                .connectString(zkServerPath)
                .sessionTimeoutMs(10000).retryPolicy(retryPolicy)
                .namespace("workspace").build();
        client.start();
    }

    /**
     * 新建节点并赋予权限
     */
    @Test
    public void createNodesWithAcl() throws Exception {
        List<ACL> aclList = new ArrayList<>();
        // 对密码进行加密
        String digest1 = DigestAuthenticationProvider.generateDigest("heibai:123456");
        String digest2 = DigestAuthenticationProvider.generateDigest("ying:123456");
        Id user01 = new Id("digest", digest1);
        Id user02 = new Id("digest", digest2);
        // 指定所有权限
        aclList.add(new ACL(Perms.ALL, user01));
        // 如果想要指定权限的组合，中间需要使用 | ,这里的|代表的是位运算中的 按位或
        aclList.add(new ACL(Perms.DELETE | Perms.CREATE, user02));

        // 创建节点
        byte[] data = "abc".getBytes();
        client.create().creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(aclList, true)
                .forPath(nodePath, data);
    }


    /**
     * 给已有节点设置权限,注意这会删除所有原来节点上已有的权限设置
     */
    @Test
    public void SetAcl() throws Exception {
        String digest = DigestAuthenticationProvider.generateDigest("admin:admin");
        Id user = new Id("digest", digest);
        client.setACL()
                .withACL(Collections.singletonList(new ACL(Perms.READ | Perms.DELETE, user)))
                .forPath(nodePath);
    }

    /**
     * 获取权限
     */
    @Test
    public void getAcl() throws Exception {
        List<ACL> aclList = client.getACL().forPath(nodePath);
        ACL acl = aclList.get(0);
        System.out.println(acl.getId().getId() 
                           + "是否有删读权限:" + (acl.getPerms() == (Perms.READ | Perms.DELETE)));
    }

    @After
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}
```

> 完整源码见本仓库： https://github.com/heibaiying/BigData-Notes/tree/master/code/Zookeeper/curator


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>