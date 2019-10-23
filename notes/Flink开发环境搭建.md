# Flink 开发环境搭建

## 一、安装 Scala 插件

Flink 分别提供了基于 Java 语言和 Scala 语言的 API ，如果想要使用 Scala 语言来开发 Flink 程序，可以通过在 IDEA 中安装 Scala 插件来提供语法提示，代码高亮等功能。打开 IDEA , 依次点击 `File => settings => plugins` 打开插件安装页面，搜索 Scala 插件并进行安装，安装完成后，重启 IDEA 即可生效。  

![scala-plugin](D:\BigData-Notes\pictures\scala-plugin.png)

## 二、Flink 项目初始化

### 2.1 官方项目初始化方式

Flink 官方支持使用 Maven 和 Gradle 两种构建工具来构建基于 Java 语言的 Flink 项目，支持使用 SBT 和 Maven 两种构建工具来构建基于 Scala 语言的 Flink 项目。 这里以 Maven 为例进行说明，因为其可以同时支持 Java 语言和 Scala 语言项目的构建。

需要注意的是 Flink 1.9 只支持 Maven 3.0.4 以上的版本，所以需要预先进行安装。安装完成后，可以通过以下两种方式来构建项目：

**1. 直接基于 Maven Archetype 构建**

直接使用下面的 maven 语句来进行构建，然后根据交互信息的提示，依次输入 groupId , artifactId 以及包名等信息后等待初始化的完成： 

```bash
$ mvn archetype:generate                               \
      -DarchetypeGroupId=org.apache.flink              \
      -DarchetypeArtifactId=flink-quickstart-java      \
      -DarchetypeVersion=1.9.0
```

> 注：如果想要创建基于 Scala 语言的项目，只需要将 flink-quickstart-java 换成 flink-quickstart-scala 即可，后文亦同。

**2. 使用官方脚本快速构建**

为了更方便的初始化项目，官方提供了快速构建脚本，可以通过以下命令来直接进行调用：

```shell
$ curl https://flink.apache.org/q/quickstart.sh | bash -s 1.9.0
```

该方式其实也是通过执行 maven archetype 命令来进行初始化，其脚本内容如下：

```shell
PACKAGE=quickstart

mvn archetype:generate								\
  -DarchetypeGroupId=org.apache.flink				\
  -DarchetypeArtifactId=flink-quickstart-java		\
  -DarchetypeVersion=${1:-1.8.0}							\
  -DgroupId=org.myorg.quickstart					\
  -DartifactId=$PACKAGE								\
  -Dversion=0.1										\
  -Dpackage=org.myorg.quickstart					\
  -DinteractiveMode=false
```

可以看到相比于第一种方式，该种方式只是直接指定好了 groupId ，artifactId ，version 等信息而已。

### 2.2 使用 IDEA 快速构建

如果你使用的是开发工具是 IDEA ，可以直接在项目创建页面选择 Maven Flink Archetype 进行项目初始化：

![flink-maven](D:\BigData-Notes\pictures\flink-maven.png)

如果你的 IDEA 没有上述 Archetype， 可以通过点击右上角的 `ADD ARCHETYPE` ，来进行添加，依次填入所需信息，这些信息都可以从上述的 `archetype:generate ` 语句中获取。点击  `OK` 保存后，该 Archetype 就会一直存在于你的 IDEA 中，之后每次创建项目时，只需要直接选择该 Archetype 即可。

![flink-maven-new](D:\BigData-Notes\pictures\flink-maven-new.png)

选中 Flink Archetype ，然后点击 `NEXT` 按钮，之后的所有步骤都和正常的 Maven 工程相同。创建完成后的项目结构如下：

![flink-basis-project](D:\BigData-Notes\pictures\flink-basis-project.png)

## 三、词频统计案例

### 3.1 案例代码

创建完成后，可以先书写一个简单的词频统计的案例来尝试运行 Flink 项目，这里以 Scala 语言为例，代码如下：

```scala
package com.heibaiying

import org.apache.flink.api.scala._

object WordCountBatch {

  def main(args: Array[String]): Unit = {
    val benv = ExecutionEnvironment.getExecutionEnvironment
    val text = benv.readTextFile("D:\\wordcount.txt")
    val counts = text.flatMap { _.toLowerCase.split(",") filter { _.nonEmpty } }.map { (_, 1) }.groupBy(0).sum(1)
    counts.print()
  }
}
```

其中 `wordcount.txt` 中的内容如下：

```shell
a,a,a,a,a
b,b,b
c,c
d,d
```

本机不需要安装其他任何的 Flink 环境，直接运行 Main 方法即可，结果如下：

![flink-word-count](D:\BigData-Notes\pictures\flink-word-count.png)

### 3.1 常见异常

这里常见的一个启动异常是如下，之所以出现这样的情况，是因为 Maven 提供的 Flink Archetype 默认是以生产环境为标准的，因为 Flink 的安装包中默认就有 Flink 相关的 JAR 包，所以在 Maven 中这些 JAR 都被标识为 `<scope>provided</scope>`  , 只需要去掉该标签即可。

```shell
Caused by: java.lang.ClassNotFoundException: org.apache.flink.api.common.typeinfo.TypeInformation
```
## 四、使用 Scala 命令行

 https://flink.apache.org/downloads.html 

start-scala-shell.sh

```shell
[root@hadoop001 bin]# ./start-scala-shell.sh
错误: 找不到或无法加载主类 org.apache.flink.api.scala.FlinkShell
```









