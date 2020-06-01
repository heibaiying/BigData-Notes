# Azkaban Flow 2.0的使用

<nav>
<a href="#一Flow-20-简介">一、Flow 2.0 简介</a><br/>
<a href="#二YAML语法">二、YAML语法</a><br/>
<a href="#三简单任务调度">三、简单任务调度</a><br/>
<a href="#四多任务调度">四、多任务调度</a><br/>
<a href="#五内嵌流">五、内嵌流</a><br/>
</nav>


## 一、Flow 2.0 简介

### 1.1 Flow 2.0 的产生

Azkaban 目前同时支持 Flow 1.0 和 Flow2.0 ，但是官方文档上更推荐使用 Flow 2.0，因为 Flow 1.0 会在将来的版本被移除。Flow 2.0 的主要设计思想是提供 1.0 所没有的流级定义。用户可以将属于给定流的所有 `job / properties` 文件合并到单个流定义文件中，其内容采用 YAML 语法进行定义，同时还支持在流中再定义流，称为为嵌入流或子流。

### 1.2 基本结构

项目 zip 将包含多个流 YAML 文件，一个项目 YAML 文件以及可选库和源代码。Flow YAML 文件的基本结构如下：

+ 每个 Flow 都在单个 YAML 文件中定义；
+ 流文件以流名称命名，如：`my-flow-name.flow`；
+ 包含 DAG 中的所有节点；
+  每个节点可以是作业或流程；
+  每个节点 可以拥有 name, type, config, dependsOn 和 nodes sections 等属性；
+  通过列出 dependsOn 列表中的父节点来指定节点依赖性；
+ 包含与流相关的其他配置；
+   当前 properties 文件中流的所有常见属性都将迁移到每个流 YAML 文件中的 config 部分。

官方提供了一个比较完善的配置样例，如下：

```yaml
config:
  user.to.proxy: azktest
  param.hadoopOutData: /tmp/wordcounthadoopout
  param.inData: /tmp/wordcountpigin
  param.outData: /tmp/wordcountpigout

# This section defines the list of jobs
# A node can be a job or a flow
# In this example, all nodes are jobs
nodes:
 # Job definition
 # The job definition is like a YAMLified version of properties file
 # with one major difference. All custom properties are now clubbed together
 # in a config section in the definition.
 # The first line describes the name of the job
 - name: AZTest
   type: noop
   # The dependsOn section contains the list of parent nodes the current
   # node depends on
   dependsOn:
     - hadoopWC1
     - NoOpTest1
     - hive2
     - java1
     - jobCommand2

 - name: pigWordCount1
   type: pig
   # The config section contains custom arguments or parameters which are
   # required by the job
   config:
     pig.script: src/main/pig/wordCountText.pig

 - name: hadoopWC1
   type: hadoopJava
   dependsOn:
     - pigWordCount1
   config:
     classpath: ./*
     force.output.overwrite: true
     input.path: ${param.inData}
     job.class: com.linkedin.wordcount.WordCount
     main.args: ${param.inData} ${param.hadoopOutData}
     output.path: ${param.hadoopOutData}

 - name: hive1
   type: hive
   config:
     hive.script: src/main/hive/showdb.q

 - name: NoOpTest1
   type: noop

 - name: hive2
   type: hive
   dependsOn:
     - hive1
   config:
     hive.script: src/main/hive/showTables.sql

 - name: java1
   type: javaprocess
   config:
     Xms: 96M
     java.class: com.linkedin.foo.HelloJavaProcessJob

 - name: jobCommand1
   type: command
   config:
     command: echo "hello world from job_command_1"

 - name: jobCommand2
   type: command
   dependsOn:
     - jobCommand1
   config:
     command: echo "hello world from job_command_2"
```

## 二、YAML语法

想要使用 Flow 2.0 进行工作流的配置，首先需要了解 YAML 。YAML 是一种简洁的非标记语言，有着严格的格式要求的，如果你的格式配置失败，上传到 Azkaban 的时候就会抛出解析异常。

### 2.1 基本规则

1. 大小写敏感 ；
2. 使用缩进表示层级关系 ；
3. 缩进长度没有限制，只要元素对齐就表示这些元素属于一个层级； 
4. 使用#表示注释 ；
5. 字符串默认不用加单双引号，但单引号和双引号都可以使用，双引号表示不需要对特殊字符进行转义；
6. YAML 中提供了多种常量结构，包括：整数，浮点数，字符串，NULL，日期，布尔，时间。

### 2.2 对象的写法

```yaml
# value 与 ： 符号之间必须要有一个空格
key: value
```

### 2.3 map的写法

```yaml
# 写法一 同一缩进的所有键值对属于一个map
key: 
    key1: value1
    key2: value2

# 写法二
{key1: value1, key2: value2}
```

### 2.3 数组的写法

```yaml
# 写法一 使用一个短横线加一个空格代表一个数组项
- a
- b
- c

# 写法二
[a,b,c]
```

### 2.5 单双引号

支持单引号和双引号，但双引号不会对特殊字符进行转义：

```yaml
s1: '内容\n 字符串'
s2: "内容\n 字符串"

转换后：
{ s1: '内容\\n 字符串', s2: '内容\n 字符串' }
```

### 2.6 特殊符号

一个 YAML 文件中可以包括多个文档，使用 `---` 进行分割。

### 2.7 配置引用

Flow 2.0 建议将公共参数定义在 `config` 下，并通过 `${}` 进行引用。



## 三、简单任务调度

### 3.1 任务配置

新建 `flow` 配置文件：

```yaml
nodes:
  - name: jobA
    type: command
    config:
      command: echo "Hello Azkaban Flow 2.0."
```

在当前的版本中，Azkaban 同时支持 Flow 1.0 和 Flow 2.0，如果你希望以 2.0 的方式运行，则需要新建一个 `project` 文件，指明是使用的是 Flow 2.0：

```shell
azkaban-flow-version: 2.0
```

### 3.2 打包上传

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/azkaban-simple.png"/> </div>



### 3.3 执行结果

由于在 1.0 版本中已经介绍过 Web UI 的使用，这里就不再赘述。对于 1.0 和 2.0 版本，只有配置方式有所不同，其他上传执行的方式都是相同的。执行结果如下：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/azkaban-simle-result.png"/> </div>

## 四、多任务调度

和 1.0 给出的案例一样，这里假设我们有五个任务（jobA——jobE）, D 任务需要在 A，B，C 任务执行完成后才能执行，而 E 任务则需要在 D 任务执行完成后才能执行，相关配置文件应如下。可以看到在 1.0 中我们需要分别定义五个配置文件，而在 2.0 中我们只需要一个配置文件即可完成配置。

```yaml
nodes:
  - name: jobE
    type: command
    config:
      command: echo "This is job E"
    # jobE depends on jobD
    dependsOn: 
      - jobD
    
  - name: jobD
    type: command
    config:
      command: echo "This is job D"
    # jobD depends on jobA、jobB、jobC
    dependsOn:
      - jobA
      - jobB
      - jobC

  - name: jobA
    type: command
    config:
      command: echo "This is job A"

  - name: jobB
    type: command
    config:
      command: echo "This is job B"

  - name: jobC
    type: command
    config:
      command: echo "This is job C"
```

## 五、内嵌流

Flow2.0 支持在一个 Flow 中定义另一个 Flow，称为内嵌流或者子流。这里给出一个内嵌流的示例，其 `Flow` 配置如下：

```yaml
nodes:
  - name: jobC
    type: command
    config:
      command: echo "This is job C"
    dependsOn:
      - embedded_flow

  - name: embedded_flow
    type: flow
    config:
      prop: value
    nodes:
      - name: jobB
        type: command
        config:
          command: echo "This is job B"
        dependsOn:
          - jobA

      - name: jobA
        type: command
        config:
          command: echo "This is job A"
```

内嵌流的 DAG 图如下：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/azkaban-embeded-flow.png"/> </div>

执行情况如下：

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/azkaban-embeded-success.png"/> </div>



## 参考资料

1. [Azkaban Flow 2.0 Design](https://github.com/azkaban/azkaban/wiki/Azkaban-Flow-2.0-Design)
2. [Getting started with Azkaban Flow 2.0](https://github.com/azkaban/azkaban/wiki/Getting-started-with-Azkaban-Flow-2.0)



<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>