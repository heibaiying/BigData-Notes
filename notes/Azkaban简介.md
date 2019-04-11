# Azkaban简介


## 一、Azkaban 介绍

#### 1.1 背景

一个完整的大数据分析系统，必然有很多任务单元组成（如数据收集、数据清洗、数据存储、数据分析等）。 所有的任务单元及其之间的依赖关系组成了多个工作流。复杂的工作流管理涉及到很多问题：

- 如何定时调度某个任务？
- 如何在某个任务执行完成后再去执行另一个任务？
- 如何在任务失败时候发出预警？
- ......

面对这些问题，工作流调度程序应运而生。Azkaban就是其中之一，也是大数据场景下使用最为广泛的工作流调度系统之一。

#### 1.2 功能

Azkaban产生于LinkedIn，在多种大数据生成环境下使用多年，按照官网的表述，其具备以下功能：

- 兼容任何版本的Hadoop
- 易于使用的Web UI
- 可以使用简单的Web页面进行工作流上传
- 支持按项目进行独立管理
- 定时任务调度
- 模块化和可插入
- 身份验证和授权
- 跟踪用户操作
- 支持失败和成功的电子邮件提醒
- SLA警报和自动查杀失败任务
- 重试失败的任务

Azkaban是基于可用性设计的，在使用上确实做到了简单易用，页面整体的风格也比较明朗，下面是其WEB UI界面：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/azkaban-web.png"/> </div>

## 二、Azkaban 和 Oozie

Azkaban 和 Oozie 都是目前使用最为广泛的工作流调度程序，其主要区别如下：

#### 功能对比

- 两者均可以调度Linux命令、MapReduce、Spark、Pig、Java、Hive等工作流任务
- 两者均可以定时执行工作流任务

#### 工作流定义

- Azkaban使用Properties（Flow 1.0）和 YAML（Flow 2.0）文件定义工作流
- Oozie使用hadoop流程定义语言（hadoop process defination language，hPDL）来描述工作流，hPDL是一种XML流程定义语言

#### 资源管理

- Azkaban有较严格的权限控制，如用户对工作流进行读/写/执行等操作
- Oozie暂无严格的权限控制

#### 运行模式

> After version 3.0, we provide two modes: the stand alone “solo-server” mode and distributed multiple-executor mode. The following describes thedifferences between the two modes.

按照官方文档的说明，Azkaban 3.x 之后版本提供2种运行模式：

- solo server model（单服务模式）：元数据默认存放在内置的H2数据库（也可以修改为MYSQL），该模式中 webServer 和 executorServer 运行在同一个进程中，进程名是AzkabanSingleServer。该模式适用于小规模的使用。
- multiple-executor（分布式多服务模式）：存放元数据的数据库为MYSQL，采用主从设置进行备份，管理服务器（webServer）和执行服务器（executorServer）在不同进程中运行，这种模式下，管理服务器和执行服务器互不影响，适合用于生产环境。

Oozie使用Tomcat等Web服务器作为Web页面展示容器，默认使用derby存储工作流元数据，由于derby过于轻量，实际中基本使用MySQL作为Oozie的元数据库。





## 三、总结

如果你的工作流不是特别复杂，没有超出了Azkaban的处理范围，则推荐轻量级的Azkaban，主要基于以下原因：

+ 安装方面：Azkaban 3.0 之前都是提供安装包的，解压配置部署即可。Azkaban 3.0 之后的版本需要编译，这个编译时基于gradle进行的，自动化程度也比较高，只需要按步骤执行编译命令即可；
+ 页面设计：Azkaban的界面比较简单易用，所有任务的执行结果、执行日志、任务之间的依赖关系都可以从界面上直观查看；
+ 配置方面：在Flow 1.0 版本的工作流定义是基于Properties文件的，这个时候的限制可能会多一点，但是在Flow 2.0 中支持YARM语法来进行配置，熟悉Spring开发的朋友可能都知道Spring Boot就采用的YAML代替之前繁重的XML，YAML语法使得Azkaban Flow 的配置更为简单、灵活。

 
