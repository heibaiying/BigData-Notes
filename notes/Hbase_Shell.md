# Hbase 常用 Shell 命令
<nav>
<a href="#一基本命令">一、基本命令</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-获取帮助">1.1 获取帮助</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-查看服务器状态">1.2 查看服务器状态</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-查看版本信息">1.3 查看版本信息</a><br/>
<a href="#二关于表的操作">二、关于表的操作</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-查看所有表">2.1 查看所有表</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-创建表">2.2 创建表</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-查看表的基本信息">2.3 查看表的基本信息</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-表的启用禁用">2.4 表的启用/禁用</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-检查表是否存在">2.5 检查表是否存在</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-删除表">2.6 删除表</a><br/>
<a href="#三增删改">三、增删改</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-添加列族">3.1 添加列族</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-删除列族">3.2 删除列族</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-更改列族存储版本的限制">3.3 更改列族存储版本的限制</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-插入数据">3.4 插入数据</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#35-获取指定行指定行中的列族列的信息">3.5 获取指定行、指定行中的列族、列的信息</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#36-删除指定行指定行中的列">3.6 删除指定行、指定行中的列</a><br/>
<a href="#四查询">四、查询</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#41Get查询">4.1Get查询</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#42-查询整表数据">4.2 查询整表数据</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#43-查询指定列簇的数据">4.3 查询指定列簇的数据</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#44--条件查询">4.4  条件查询</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#45--条件过滤">4.5  条件过滤</a><br/>
</nav>


## 一、基本命令

打开 Hbase Shell：

```shell
# hbase shell
```

#### 1.1 获取帮助

```shell
# 获取帮助
help
# 获取命令的详细信息
help 'status'
```

#### 1.2 查看服务器状态

```shell
status
```

#### 1.3 查看版本信息
```shell
version
```



## 二、关于表的操作


#### 2.1 查看所有表

```shell
list
```

#### 2.2 创建表

 **命令格式**： create '表名称', '列族名称 1','列族名称 2','列名称 N'

```shell
# 创建一张名为Student的表,包含基本信息（baseInfo）、学校信息（schoolInfo）两个列族
create 'Student','baseInfo','schoolInfo'
```

#### 2.3 查看表的基本信息

 **命令格式**：desc '表名'

```shell
describe 'Student'
```

#### 2.4 表的启用/禁用

enable 和 disable 可以启用/禁用这个表,is_enabled 和 is_disabled 来检查表是否被禁用

```shell
# 禁用表
disable 'Student'
# 检查表是否被禁用
is_disabled 'Student'
# 启用表
enable 'Student'
# 检查表是否被启用
is_enabled 'Student'
```

#### 2.5 检查表是否存在

```shell
exists 'Student'
```

#### 2.6 删除表

```shell
# 删除表前需要先禁用表
disable 'Student'
# 删除表
drop 'Student'
```



## 三、增删改


#### 3.1 添加列族

 **命令格式**： alter '表名', '列族名'

```shell
alter 'Student', 'teacherInfo'
```

#### 3.2 删除列族

 **命令格式**：alter '表名', {NAME => '列族名', METHOD => 'delete'}

```shell
alter 'Student', {NAME => 'teacherInfo', METHOD => 'delete'}
```

#### 3.3 更改列族存储版本的限制

默认情况下，列族只存储一个版本的数据，如果需要存储多个版本的数据，则需要修改列族的属性。修改后可通过 `desc` 命令查看。

```shell
alter 'Student',{NAME=>'baseInfo',VERSIONS=>3}
```

#### 3.4 插入数据

 **命令格式**：put '表名', '行键','列族:列','值'

**注意：如果新增数据的行键值、列族名、列名与原有数据完全相同，则相当于更新操作**

```shell
put 'Student', 'rowkey1','baseInfo:name','tom'
put 'Student', 'rowkey1','baseInfo:birthday','1990-01-09'
put 'Student', 'rowkey1','baseInfo:age','29'
put 'Student', 'rowkey1','schoolInfo:name','Havard'
put 'Student', 'rowkey1','schoolInfo:localtion','Boston'

put 'Student', 'rowkey2','baseInfo:name','jack'
put 'Student', 'rowkey2','baseInfo:birthday','1998-08-22'
put 'Student', 'rowkey2','baseInfo:age','21'
put 'Student', 'rowkey2','schoolInfo:name','yale'
put 'Student', 'rowkey2','schoolInfo:localtion','New Haven'

put 'Student', 'rowkey3','baseInfo:name','maike'
put 'Student', 'rowkey3','baseInfo:birthday','1995-01-22'
put 'Student', 'rowkey3','baseInfo:age','24'
put 'Student', 'rowkey3','schoolInfo:name','yale'
put 'Student', 'rowkey3','schoolInfo:localtion','New Haven'

put 'Student', 'wrowkey4','baseInfo:name','maike-jack'
```

#### 3.5 获取指定行、指定行中的列族、列的信息

```shell
# 获取指定行中所有列的数据信息
get 'Student','rowkey3'
# 获取指定行中指定列族下所有列的数据信息
get 'Student','rowkey3','baseInfo'
# 获取指定行中指定列的数据信息
get 'Student','rowkey3','baseInfo:name'
```

#### 3.6 删除指定行、指定行中的列

```shell
# 删除指定行
delete 'Student','rowkey3'
# 删除指定行中指定列的数据
delete 'Student','rowkey3','baseInfo:name'
```



## 四、查询

hbase 中访问数据有两种基本的方式：

+ 按指定 rowkey 获取数据：get 方法；

+ 按指定条件获取数据：scan 方法。

`scan` 可以设置 begin 和 end 参数来访问一个范围内所有的数据。get 本质上就是 begin 和 end 相等的一种特殊的 scan。

#### 4.1Get查询

```shell
# 获取指定行中所有列的数据信息
get 'Student','rowkey3'
# 获取指定行中指定列族下所有列的数据信息
get 'Student','rowkey3','baseInfo'
# 获取指定行中指定列的数据信息
get 'Student','rowkey3','baseInfo:name'
```

#### 4.2 查询整表数据

```shell
scan 'Student'
```

#### 4.3 查询指定列簇的数据

```shell
scan 'Student', {COLUMN=>'baseInfo'}
```

#### 4.4  条件查询

```shell
# 查询指定列的数据
scan 'Student', {COLUMNS=> 'baseInfo:birthday'}
```

除了列 `（COLUMNS）` 修饰词外，HBase 还支持 `Limit`（限制查询结果行数），`STARTROW`（`ROWKEY` 起始行，会先根据这个 `key` 定位到 `region`，再向后扫描）、`STOPROW`(结束行)、`TIMERANGE`（限定时间戳范围）、`VERSIONS`（版本数）、和 `FILTER`（按条件过滤行）等。

如下代表从 `rowkey2` 这个 `rowkey` 开始，查找下两个行的最新 3 个版本的 name 列的数据：

```shell
scan 'Student', {COLUMNS=> 'baseInfo:name',STARTROW => 'rowkey2',STOPROW => 'wrowkey4',LIMIT=>2, VERSIONS=>3}
```

#### 4.5  条件过滤

Filter 可以设定一系列条件来进行过滤。如我们要查询值等于 24 的所有数据：

```shell
scan 'Student', FILTER=>"ValueFilter(=,'binary:24')"
```

值包含 yale 的所有数据：

```shell
scan 'Student', FILTER=>"ValueFilter(=,'substring:yale')"
```

列名中的前缀为 birth 的：

```shell
scan 'Student', FILTER=>"ColumnPrefixFilter('birth')"
```

FILTER 中支持多个过滤条件通过括号、AND 和 OR 进行组合：

```shell
# 列名中的前缀为birth且列值中包含1998的数据
scan 'Student', FILTER=>"ColumnPrefixFilter('birth') AND ValueFilter ValueFilter(=,'substring:1998')"
```

`PrefixFilter` 用于对 Rowkey 的前缀进行判断：

```shell
scan 'Student', FILTER=>"PrefixFilter('wr')"
```







<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>