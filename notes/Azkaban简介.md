# Azkaban简介


## 一、Azkaban 介绍

#### 1.1 背景

一个完整的大数据分析系统，必然由很多任务单元 (如数据收集、数据清洗、数据存储、数据分析等) 组成，所有的任务单元及其之间的依赖关系组成了复杂的工作流。复杂的工作流管理涉及到很多问题：

- 如何定时调度某个任务？
- 如何在某个任务执行完成后再去执行另一个任务？
- 如何在任务失败时候发出预警？
- ......

面对这些问题，工作流调度系统应运而生。Azkaban 就是其中之一。

#### 1.2 功能

Azkaban 产生于 LinkedIn，并经过多年生产环境的检验，它具备以下功能：

- 兼容任何版本的 Hadoop
- 易于使用的 Web UI
- 可以使用简单的 Web 页面进行工作流上传
- 支持按项目进行独立管理
- 定时任务调度
- 模块化和可插入
- 身份验证和授权
- 跟踪用户操作
- 支持失败和成功的电子邮件提醒
- SLA 警报和自动查杀失败任务
- 重试失败的任务

Azkaban 的设计理念是在保证功能实现的基础上兼顾易用性，其页面风格清晰明朗，下面是其 WEB UI 界面：

<div align="center"> <img  src="../pictures/azkaban-web.png"/> </div>

## 二、Azkaban 和 Oozie

Azkaban 和 Oozie 都是目前使用最为广泛的工作流调度程序，其主要区别如下：

#### 功能对比

- 两者均可以调度 Linux 命令、MapReduce、Spark、Pig、Java、Hive 等工作流任务；
- 两者均可以定时执行工作流任务。

#### 工作流定义

- Azkaban 使用 Properties(Flow 1.0) 和 YAML(Flow 2.0) 文件定义工作流；
- Oozie 使用 Hadoop 流程定义语言（hadoop process defination language，HPDL）来描述工作流，HPDL 是一种 XML 流程定义语言。

#### 资源管理

- Azkaban 有较严格的权限控制，如用户对工作流进行读/写/执行等操作；
- Oozie 暂无严格的权限控制。

#### 运行模式

+ Azkaban 3.x 提供了两种运行模式：
  + **solo server model(单服务模式)** ：元数据默认存放在内置的 H2 数据库（可以修改为 MySQL），该模式中 `webServer`(管理服务器) 和 `executorServer`(执行服务器) 运行在同一个进程中，进程名是 `AzkabanSingleServer`。该模式适用于小规模工作流的调度。
  + **multiple-executor(分布式多服务模式)** ：存放元数据的数据库为 MySQL，MySQL 应采用主从模式进行备份和容错。这种模式下 `webServer` 和 `executorServer` 在不同进程中运行，彼此之间互不影响，适合用于生产环境。

+ Oozie 使用 Tomcat 等 Web 容器来展示 Web 页面，默认使用 derby 存储工作流的元数据，由于 derby 过于轻量，实际使用中通常用 MySQL 代替。





## 三、总结

如果你的工作流不是特别复杂，推荐使用轻量级的 Azkaban，主要有以下原因：

+ **安装方面**：Azkaban 3.0 之前都是提供安装包的，直接解压部署即可。Azkaban 3.0 之后的版本需要编译，这个编译是基于 gradle 的，自动化程度比较高；
+ **页面设计**：所有任务的依赖关系、执行结果、执行日志都可以从界面上直观查看到；
+ **配置方面**：Azkaban Flow 1.0 基于 Properties 文件来定义工作流，这个时候的限制可能会多一点。但是在 Flow 2.0 就支持了 YARM。YARM 语法更加灵活简单，著名的微服务框架 Spring Boot 就采用的 YAML 代替了繁重的 XML。

 
