# Azkaban Flow 2.0的使用

## 一、Flow 2.0 简介

### 1.1 Flow 2.0 的产生

Azkaban 目前同时支持Flow 1.0和Flow2.0,但是官方文档上更推荐使用2.0，因为Flow 1.0 会在将来的版本被移除。

>This section covers how to create your Azkaban flows using Azkaban Flow 2.0.Flow 1.0 will be deprecated in the future.

Flow 2.0设计的主要思想是提供1.0版本没有的流级定义。用户可以将属于给定流的所有.job / .properties文件合并到单个流定义文件中，而不是创建多个.job / .properties文件。配置文件以YAML格式定义，每个项目zip将包含多个流YAML文件和一个项目YAML文件。同时可以在YAML文件中的流内定义流，称为为嵌入流或子流。

### 1.2 基本结构

项目zip将包含多个流YAML文件，一个项目YAML文件以及可选库和源代码。Flow YAML文件的基本结构如下：

+ 每个Flow都在单个YAML文件中定义
+ 流文件以流名称命名。如：my-flow-name.flow
+ 包含DAG中的所有节点
+  每个节点可以是作业或流程
+  每个节点 可以拥有 name, type, config, dependsOn and nodes sections等属性
+  通过列出dependsOn列表中的父节点来指定节点依赖性
+ 包含与流相关的其他配置
+   当前.properties文件中流的所有常见属性都将迁移到每个流YAML文件中的config部分

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

想要进行Flow流的配置，首先需要了解YAML ，YAML 是一种简洁的非标记语言，有着严格的格式要求的，如果你的格式配置失败，上传到Azkaban的时候就会抛出解析异常。

### 2.1 基本规则

1. 大小写敏感 
2. 使用缩进表示层级关系 
3. 缩进长度没有限制，只要元素对齐就表示这些元素属于一个层级。 
4. 使用#表示注释 
5. 字符串默认不用加单双引号，但单引号和双引号都可以使用，双引号不会对特殊字符转义。
6. YAML中提供了多种常量结构，包括：整数，浮点数，字符串，NULL，日期，布尔，时间。

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

单引号和双引号都可以使用，双引号不会对特殊字符转义。

```yaml
s1: '内容\n字符串'
s2: "内容\n字符串"

转换后：
{ s1: '内容\\n字符串', s2: '内容\n字符串' }
```

### 2.6 特殊符号

`---`  YAML可以在同一个文件中，使用`---`表示一个文档的开始。

### 2.7 配置引用

在Azkaban中可以使用`${}`引用定义的配置，同时也建议将公共的参数抽取到config中，并使用`${}`进行引用。



## 三、简单任务调度

### 3.1 任务配置

新建`flow`配置文件

```yaml
nodes:
  - name: jobA
    type: command
    config:
      command: echo "Hello Azkaban Flow 2.0."
```

在当前的版本中，由于Azkaban是同时支持Flow 1.0 和 Flow 2.0的，如果你想让Azkaban知道你是希望以2.0方式运行，则需要新建一个`project`文件，指明是使用的Flow 2.0

```shell
azkaban-flow-version: 2.0
```

### 3.2 打包上传

![azkaban-simple](D:\BigData-Notes\pictures\azkaban-simple.png)



### 3.3 执行结果

由于在1.0 版本中已经介绍过web ui的使用，这里就不再赘述，对于1.0和2.0版本，只有配置的方式是不同的，其他上传执行的操作方式都是相同的。执行结果如下：

![azkaban-simle-result](D:\BigData-Notes\pictures\azkaban-simle-result.png)

## 四、多任务调度

和1.0给的案例一样，这里假设我们有五个任务（jobA——jobE）,D任务需要在A，B，C任务执行完成后才能执行，而E任务则需要在D任务执行完成后才能执行。`Flow`配置如下。可以看到在1.0中我们需要分别定义五个配置文件，而在2.0中我们只需要一个配置文件即可完成配置。

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

Flow2.0 支持在一个Flow中定义另一个Flow，称为内嵌流或者子流。这里给出一个内嵌流的示例，其`Flow`配置如下：

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

内嵌流的DAG图如下：

![azkaban-embeded-flow](D:\BigData-Notes\pictures\azkaban-embeded-flow.png)

执行情况如下：

![azkaban-embeded-success](D:\BigData-Notes\pictures\azkaban-embeded-success.png)



## 参考资料

1. [Azkaban Flow 2.0 Design](https://github.com/azkaban/azkaban/wiki/Azkaban-Flow-2.0-Design)
2. [Getting started with Azkaban Flow 2.0](https://github.com/azkaban/azkaban/wiki/Getting-started-with-Azkaban-Flow-2.0)

