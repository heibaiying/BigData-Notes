# Scala简介及开发环境配置

## 一、Scala简介

### 1.1 概念

Scala全称为scalable language，即“可伸缩的语言”，之所以这样命名，是因为它的设计目标是随着用户的需求一起成长。从技术上讲，Scala是一门综合了面向对象和函数式编程概念的静态类型的编程语言，它运行在标准的Java平台上，可以与所有的Java类库无缝协作。



### 1.2 特点

#### 1.  Scala是面向对象的

Scala是一种面向对象的语言，每个值都是对象，每个方法都是调用。举例来说，如果你执行1+2，则对于Scala而言，实际是在调用Int类里定义的名为+的方法。

#### 2. Scala是函数式的

Scala不只是一门纯的面对对象的语言，它也是功能完整的函数式编程语言。函数式编程以两大核心理念为指导：

+ 函数是一等公民；
+ 程序中的操作应该将输入值映射成输出值，而不是当场修改数据。即方法不应该有副作用。



### 3. Scala的优点

#### 1. 与Java的兼容

Scala可以与Java无缝对接，其在执行时会被编译成JVM字节码，这使得其性能与Java相当。Scala可以直接调用Java中的方法、访问Java中的字段、继承Java类、实现Java接口，并且Scala也重度复用并包装了原生的Java类型，且支持隐式转换。

#### 2. 精简的语法

Scala的程序通常比较简洁，相比Java而言，代码行数会大大减少，这使得程序员对代码的阅读和理解更快，缺陷也更少。

#### 3. 高级语言的特性

Scala具有高级语言的特定，对代码进行了高级别的抽象，能够让你更好的控制复杂度，保证开发的效率。

#### 4. 静态类型

Scala拥有非常先进的静态类型系统，Scala不仅拥有与Java类似的允许嵌套类(内部类)的类型系统，还允许使用泛型对类型进行参数化，用交集（intersection）来组合类型，以及使用抽象类型来进行隐藏类型的细节。通过这些特定，我们可以更快地设计出安全易用的程序和接口。





## 二、配置IDEA开发环境

### 2.1 前置条件

首先Scala的运行依赖于Java环境，目前最新的Scala 2.12.x要求你必须安装JDK 1.8或以上版本。

### 2.2 安装Scala插件

首先需要安装Scala插件，使得IDEA支持scala语言的开发。打开 IDEA，依次点击**File** => **settings**=> **plugins**选项卡，搜索Scala插件(如下图)。找到插件后进行安装，并重启IDEA使得安装生效。

![idea-scala-plugin](D:\BigData-Notes\pictures\idea-scala-plugin.png)



### 2.3 创建Scala项目

在IDEA中依次点击 **File** => **New** => **Project**选项卡，然后选择创建Scala—IDEA工程：

![idea-newproject-scala](D:\BigData-Notes\pictures\idea-newproject-scala.png)



### 2.4 下载Scala SDK

#### 1.方式一（不推荐）

此时看到Scala SDK为空，依次点击`Create => Download` ,选择所需的版本后，点击`OK`按钮进行下载，下载完成点击`Finish`进入工程。

![idea-scala-select](D:\BigData-Notes\pictures\idea-scala-select.png)



#### 2.方式二（推荐使用）

方式一是Scala官方安装指南里使用的方式，但是由于下载源并不在国内，所以下载速度会比较慢，且这种安装下并没有直接提供Scala命令行工具。我们在一开始学习的时候，使用命令行去执行简单的代码能更快的看到输出。所以推荐到官方页面下载对应的安装包。

> 官方下载地址：https://www.scala-lang.org/download/

这里我的系统是Windows，下载msi版本的安装包后，一直点击下一步安装即可，安装完成后会自动配置好环境变量。

![idea-scala-select](D:\BigData-Notes\pictures\scala-other-resources.png)



由于安装时已经自动配置好环境变量，所以IDEA会自动选择对应版本的SDK。

![idea-scala-select](D:\BigData-Notes\pictures\idea-scala-2.1.8.png)



### 2.5 创建Hello World

在工程 `src`目录上右击**New** => **Scala class**.创建`Hello.scala`。输入代码如下，完成后点击运行按钮，成功运行则代表开发环境搭建成功。

![scala-hello-world](D:\BigData-Notes\pictures\scala-hello-world.png)





### 2.6 切换Scala版本

如果在日常的开发中，由于对应软件（如spark）的版本切换，导致需要切换Scala的版本，则可以在`Project Structures`中的`Global Libraries`选项卡中进行配置。

![scala-hello-world](D:\BigData-Notes\pictures\idea-scala-change.png)





## 参考资料

1. Scala编程第三版