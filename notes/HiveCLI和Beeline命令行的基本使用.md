# Hive CLI和Beeline命令行的基本使用

<nav>
<a href="#一Hive-CLI">一、Hive CLI</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-Help">1.1 Help</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-交互式命令行">1.2 交互式命令行</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-执行SQL命令">1.3 执行SQL命令</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-执行SQL脚本">1.4 执行SQL脚本</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-配置Hive变量">1.5 配置Hive变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#16-配置文件启动">1.6 配置文件启动</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#17-用户自定义变量">1.7 用户自定义变量</a><br/>
<a href="#二Beeline">二、Beeline </a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-HiveServer2">2.1 HiveServer2</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-Beeline">2.1 Beeline</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-常用参数">2.3 常用参数</a><br/>
<a href="#三Hive配置">三、Hive配置</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-配置文件">3.1 配置文件</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-hiveconf">3.2 hiveconf</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-set">3.3 set</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-配置优先级">3.4 配置优先级</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#35-配置参数">3.5 配置参数</a><br/>
</nav>

## 一、Hive CLI

### 1.1 Help

使用 `hive -H` 或者 `hive --help` 命令可以查看所有命令的帮助，显示如下：

```
usage: hive
 -d,--define <key=value>          Variable subsitution to apply to hive 
                                  commands. e.g. -d A=B or --define A=B  --定义用户自定义变量
    --database <databasename>     Specify the database to use  -- 指定使用的数据库
 -e <quoted-query-string>         SQL from command line   -- 执行指定的 SQL
 -f <filename>                    SQL from files   --执行 SQL 脚本
 -H,--help                        Print help information  -- 打印帮助信息
    --hiveconf <property=value>   Use value for given property    --自定义配置
    --hivevar <key=value>         Variable subsitution to apply to hive  --自定义变量
                                  commands. e.g. --hivevar A=B
 -i <filename>                    Initialization SQL file  --在进入交互模式之前运行初始化脚本
 -S,--silent                      Silent mode in interactive shell    --静默模式
 -v,--verbose                     Verbose mode (echo executed SQL to the  console)  --详细模式
```

### 1.2 交互式命令行

直接使用 `Hive` 命令，不加任何参数，即可进入交互式命令行。

### 1.3 执行SQL命令

在不进入交互式命令行的情况下，可以使用 `hive -e ` 执行 SQL 命令。

```sql
hive -e 'select * from emp';
```

<div align="center"> <img width='700px' src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-e.png"/> </div>



### 1.4 执行SQL脚本

用于执行的 sql 脚本可以在本地文件系统，也可以在 HDFS 上。

```shell
# 本地文件系统
hive -f /usr/file/simple.sql;

# HDFS文件系统
hive -f hdfs://hadoop001:8020/tmp/simple.sql;
```

其中 `simple.sql` 内容如下：

```sql
select * from emp;
```

### 1.5 配置Hive变量

可以使用 `--hiveconf` 设置 Hive 运行时的变量。

```sql
hive -e 'select * from emp' \
--hiveconf hive.exec.scratchdir=/tmp/hive_scratch  \
--hiveconf mapred.reduce.tasks=4;
```

> hive.exec.scratchdir：指定 HDFS 上目录位置，用于存储不同 map/reduce 阶段的执行计划和这些阶段的中间输出结果。

### 1.6 配置文件启动

使用 `-i` 可以在进入交互模式之前运行初始化脚本，相当于指定配置文件启动。

```shell
hive -i /usr/file/hive-init.conf;
```

其中 `hive-init.conf` 的内容如下：

```sql
set hive.exec.mode.local.auto = true;
```

> hive.exec.mode.local.auto 默认值为 false，这里设置为 true ，代表开启本地模式。

### 1.7 用户自定义变量

`--define <key=value> ` 和 `--hivevar <key=value>  ` 在功能上是等价的，都是用来实现自定义变量，这里给出一个示例:

定义变量：

```sql
hive  --define  n=ename --hiveconf  --hivevar j=job;
```

在查询中引用自定义变量：

```sql
# 以下两条语句等价
hive > select ${n} from emp;
hive >  select ${hivevar:n} from emp;

# 以下两条语句等价
hive > select ${j} from emp;
hive >  select ${hivevar:j} from emp;
```

结果如下：

<div align="center"> <img width='700px' src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/hive-n-j.png"/> </div>

## 二、Beeline 

### 2.1 HiveServer2

Hive 内置了 HiveServer 和 HiveServer2 服务，两者都允许客户端使用多种编程语言进行连接，但是 HiveServer 不能处理多个客户端的并发请求，所以产生了 HiveServer2。

HiveServer2（HS2）允许远程客户端可以使用各种编程语言向 Hive 提交请求并检索结果，支持多客户端并发访问和身份验证。HS2 是由多个服务组成的单个进程，其包括基于 Thrift 的 Hive 服务（TCP 或 HTTP）和用于 Web UI 的 Jetty Web 服务器。

 HiveServer2 拥有自己的 CLI(Beeline)，Beeline 是一个基于 SQLLine 的 JDBC 客户端。由于 HiveServer2 是 Hive 开发维护的重点 (Hive0.15 后就不再支持 hiveserver)，所以 Hive CLI 已经不推荐使用了，官方更加推荐使用 Beeline。

### 2.1 Beeline

Beeline 拥有更多可使用参数，可以使用 `beeline --help` 查看，完整参数如下：

```properties
Usage: java org.apache.hive.cli.beeline.BeeLine
   -u <database url>               the JDBC URL to connect to
   -r                              reconnect to last saved connect url (in conjunction with !save)
   -n <username>                   the username to connect as
   -p <password>                   the password to connect as
   -d <driver class>               the driver class to use
   -i <init file>                  script file for initialization
   -e <query>                      query that should be executed
   -f <exec file>                  script file that should be executed
   -w (or) --password-file <password file>  the password file to read password from
   --hiveconf property=value       Use value for given property
   --hivevar name=value            hive variable name and value
                                   This is Hive specific settings in which variables
                                   can be set at session level and referenced in Hive
                                   commands or queries.
   --property-file=<property-file> the file to read connection properties (url, driver, user, password) from
   --color=[true/false]            control whether color is used for display
   --showHeader=[true/false]       show column names in query results
   --headerInterval=ROWS;          the interval between which heades are displayed
   --fastConnect=[true/false]      skip building table/column list for tab-completion
   --autoCommit=[true/false]       enable/disable automatic transaction commit
   --verbose=[true/false]          show verbose error messages and debug info
   --showWarnings=[true/false]     display connection warnings
   --showNestedErrs=[true/false]   display nested errors
   --numberFormat=[pattern]        format numbers using DecimalFormat pattern
   --force=[true/false]            continue running script even after errors
   --maxWidth=MAXWIDTH             the maximum width of the terminal
   --maxColumnWidth=MAXCOLWIDTH    the maximum width to use when displaying columns
   --silent=[true/false]           be more silent
   --autosave=[true/false]         automatically save preferences
   --outputformat=[table/vertical/csv2/tsv2/dsv/csv/tsv]  format mode for result display
   --incrementalBufferRows=NUMROWS the number of rows to buffer when printing rows on stdout,
                                   defaults to 1000; only applicable if --incremental=true
                                   and --outputformat=table
   --truncateTable=[true/false]    truncate table column when it exceeds length
   --delimiterForDSV=DELIMITER     specify the delimiter for delimiter-separated values output format (default: |)
   --isolation=LEVEL               set the transaction isolation level
   --nullemptystring=[true/false]  set to true to get historic behavior of printing null as empty string
   --maxHistoryRows=MAXHISTORYROWS The maximum number of rows to store beeline history.
   --convertBinaryArrayToString=[true/false]    display binary column data as string or as byte array
   --help                          display this message

```

### 2.3 常用参数

在 Hive CLI 中支持的参数，Beeline 都支持，常用的参数如下。更多参数说明可以参见官方文档 [Beeline Command Options](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients#HiveServer2Clients-Beeline%E2%80%93NewCommandLineShell)

| 参数 | 说明 |
| -------------------------------------- | ------------------------------------------------------------ |
| **-u \<database URL>**              | 数据库地址                                             |
| **-n \<username>**                | 用户名                                                       |
| **-p \<password>**                  | 密码 |
| **-d \<driver class>**              | 驱动 (可选)                                                   |
| **-e \<query>**                     | 执行 SQL 命令 |
| **-f \<file>**                      | 执行 SQL 脚本 |
| **-i  (or)--init  \<file or files>** | 在进入交互模式之前运行初始化脚本                             |
| **--property-file \<file>** | 指定配置文件 |
| **--hiveconf** *property**=**value* | 指定配置属性 |
| **--hivevar** *name**=**value* | 用户自定义属性，在会话级别有效 |

示例： 使用用户名和密码连接 Hive

```shell
$ beeline -u jdbc:hive2://localhost:10000  -n username -p password 
```

​         

## 三、Hive配置

可以通过三种方式对 Hive 的相关属性进行配置，分别介绍如下：

### 3.1 配置文件

方式一为使用配置文件，使用配置文件指定的配置是永久有效的。Hive 有以下三个可选的配置文件：

+ hive-site.xml ：Hive 的主要配置文件；

+ hivemetastore-site.xml： 关于元数据的配置；
+ hiveserver2-site.xml：关于 HiveServer2 的配置。

示例如下,在 hive-site.xml 配置 `hive.exec.scratchdir`：

```xml
 <property>
    <name>hive.exec.scratchdir</name>
    <value>/tmp/mydir</value>
    <description>Scratch space for Hive jobs</description>
  </property>
```

### 3.2 hiveconf

方式二为在启动命令行 (Hive CLI / Beeline) 的时候使用 `--hiveconf` 指定配置，这种方式指定的配置作用于整个 Session。

```
hive --hiveconf hive.exec.scratchdir=/tmp/mydir
```

### 3.3 set

方式三为在交互式环境下 (Hive CLI / Beeline)，使用 set 命令指定。这种设置的作用范围也是 Session 级别的，配置对于执行该命令后的所有命令生效。set 兼具设置参数和查看参数的功能。如下：

```shell
0: jdbc:hive2://hadoop001:10000> set hive.exec.scratchdir=/tmp/mydir;
No rows affected (0.025 seconds)
0: jdbc:hive2://hadoop001:10000> set hive.exec.scratchdir;
+----------------------------------+--+
|               set                |
+----------------------------------+--+
| hive.exec.scratchdir=/tmp/mydir  |
+----------------------------------+--+
```

### 3.4 配置优先级

配置的优先顺序如下 (由低到高)：  
`hive-site.xml` - >` hivemetastore-site.xml `- > `hiveserver2-site.xml` - >` -- hiveconf`- > `set`

### 3.5 配置参数

Hive 可选的配置参数非常多，在用到时查阅官方文档即可[AdminManual Configuration](https://cwiki.apache.org/confluence/display/Hive/AdminManual+Configuration)



## 参考资料

1. [HiveServer2 Clients](https://cwiki.apache.org/confluence/display/Hive/HiveServer2+Clients)
2. [LanguageManual Cli](https://cwiki.apache.org/confluence/display/Hive/LanguageManual+Cli)
3. [AdminManual Configuration](https://cwiki.apache.org/confluence/display/Hive/AdminManual+Configuration)


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>