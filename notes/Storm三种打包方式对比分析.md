# Storm三种打包方式对比分析

<nav>
<a href="#一简介">一、简介</a><br/>
<a href="#二mvn-package">二、mvn package</a><br/>
<a href="#三maven-assembly-plugin插件">三、maven-assembly-plugin插件</a><br/>
<a href="#四maven-shade-plugin插件">四、maven-shade-plugin插件</a><br/>
<a href="#五结论">五、结论</a><br/>
<a href="#六打包注意事项">六、打包注意事项</a><br/>
</nav>


## 一、简介

在将 Storm Topology 提交到服务器集群运行时，需要先将项目进行打包。本文主要对比分析各种打包方式，并将打包过程中需要注意的事项进行说明。主要打包方式有以下三种：

+ 第一种：不加任何插件，直接使用 mvn package 打包；
+ 第二种：使用 maven-assembly-plugin 插件进行打包；
+ 第三种：使用 maven-shade-plugin 进行打包。

以下分别进行详细的说明。



## 二、mvn package

### 2.1 mvn package的局限

不在 POM 中配置任何插件，直接使用 `mvn package` 进行项目打包，这对于没有使用外部依赖包的项目是可行的。

但如果项目中使用了第三方 JAR 包，就会出现问题，因为 `mvn package` 打包后的 JAR 中是不含有依赖包的，如果此时你提交到服务器上运行，就会出现找不到第三方依赖的异常。

如果你想采用这种方式进行打包，但是又使用了第三方 JAR，有没有解决办法？答案是有的，这一点在官方文档的[Command Line Client](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Command-line-client.html) 章节有所讲解，主要解决办法如下。

### 2.2 解决办法

在使用 `storm jar` 提交 Topology 时，可以使用如下方式指定第三方依赖：

+ 如果第三方 JAR 包在本地，可以使用 `--jars` 指定；
+ 如果第三方 JAR 包在远程中央仓库，可以使用 `--artifacts` 指定，此时如果想要排除某些依赖，可以使用 `^` 符号。指定后 Storm 会自动到中央仓库进行下载，然后缓存到本地；
+ 如果第三方 JAR 包在其他仓库，还需要使用 `--artifactRepositories` 指明仓库地址，库名和地址使用 `^` 符号分隔。

以下是一个包含上面三种情况的命令示例：

```shell
./bin/storm jar example/storm-starter/storm-starter-topologies-*.jar \
org.apache.storm.starter.RollingTopWords blobstore-remote2 remote  \
--jars "./external/storm-redis/storm-redis-1.1.0.jar,./external/storm-kafka/storm-kafka-1.1.0.jar" \
--artifacts "redis.clients:jedis:2.9.0,org.apache.kafka:kafka_2.10:0.8.2.2^org.slf4j:slf4j-log4j12" \
--artifactRepositories "jboss-repository^http://repository.jboss.com/maven2, \
HDPRepo^http://repo.hortonworks.com/content/groups/public/"
```

这种方式是建立在你能够连接到外网的情况下，如果你的服务器不能连接外网，或者你希望能把项目直接打包成一个 `ALL IN ONE` 的 JAR，即包含所有相关依赖，此时可以采用下面介绍的两个插件。

## 三、maven-assembly-plugin插件

maven-assembly-plugin 是官方文档中介绍的打包方法，来源于官方文档：[Running Topologies on a Production Cluster](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Running-topologies-on-a-production-cluster.html)

> If you're using Maven, the [Maven Assembly Plugin](http://maven.apache.org/plugins/maven-assembly-plugin/) can do the packaging for you. Just add this to your pom.xml:
>
> ```xml
> <plugin>
> <artifactId>maven-assembly-plugin</artifactId>
> <configuration>
>  <descriptorRefs>  
>    <descriptorRef>jar-with-dependencies</descriptorRef>
>  </descriptorRefs>
>  <archive>
>    <manifest>
>      <mainClass>com.path.to.main.Class</mainClass>
>    </manifest>
>  </archive>
> </configuration>
> </plugin>
> ```
>
> Then run mvn assembly:assembly to get an appropriately packaged jar. Make sure you [exclude](http://maven.apache.org/plugins/maven-assembly-plugin/examples/single/including-and-excluding-artifacts.html) the Storm jars since the cluster already has Storm on the classpath.

官方文档主要说明了以下几点：

- 使用 maven-assembly-plugin 可以把所有的依赖一并打入到最后的 JAR 中；
- 需要排除掉 Storm 集群环境中已经提供的 Storm jars；
- 通过 `  <mainClass>` 标签指定主入口类；
- 通过 `<descriptorRef>` 标签指定打包相关配置。

`jar-with-dependencies` 是 Maven[预定义](http://maven.apache.org/plugins/maven-assembly-plugin/descriptor-refs.html#jar-with-dependencies) 的一种最基本的打包配置，其 XML 文件如下：

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0
                              http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    <id>jar-with-dependencies</id>
    <formats>
        <format>jar</format>
    </formats>
    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
        </dependencySet>
    </dependencySets>
</assembly>
```

我们可以通过对该配置文件进行拓展，从而实现更多的功能，比如排除指定的 JAR 等。使用示例如下：

### 1. 引入插件

在 POM.xml 中引入插件，并指定打包格式的配置文件为 `assembly.xml`(名称可自定义)：

```xml
<build>
    <plugins>
        <plugin>
            <artifactId>maven-assembly-plugin</artifactId>
            <configuration>
                <descriptors>
                    <descriptor>src/main/resources/assembly.xml</descriptor>
                </descriptors>
                <archive>
                    <manifest>
                        <mainClass>com.heibaiying.wordcount.ClusterWordCountApp</mainClass>
                    </manifest>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>
```

`assembly.xml` 拓展自 `jar-with-dependencies.xml`，使用了 `<excludes>` 标签排除 Storm jars，具体内容如下：

```xml
<assembly xmlns="http://maven.apache.org/ASSEMBLY/2.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/ASSEMBLY/2.0.0 
                              http://maven.apache.org/xsd/assembly-2.0.0.xsd">
    
    <id>jar-with-dependencies</id>

    <!--指明打包方式-->
    <formats>
        <format>jar</format>
    </formats>

    <includeBaseDirectory>false</includeBaseDirectory>
    <dependencySets>
        <dependencySet>
            <outputDirectory>/</outputDirectory>
            <useProjectArtifact>true</useProjectArtifact>
            <unpack>true</unpack>
            <scope>runtime</scope>
            <!--排除 storm 环境中已经提供的 storm-core-->
            <excludes>
                <exclude>org.apache.storm:storm-core</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
</assembly>
```

>在配置文件中不仅可以排除依赖，还可以排除指定的文件，更多的配置规则可以参考官方文档：[Descriptor Format](http://maven.apache.org/plugins/maven-assembly-plugin/assembly.html#)

### 2.  打包命令

采用 maven-assembly-plugin 进行打包时命令如下：

```shell
# mvn assembly:assembly 
```

打包后会同时生成两个 JAR 包，其中后缀为 `jar-with-dependencies` 是含有第三方依赖的 JAR 包，后缀是由 `assembly.xml` 中 `<id>` 标签指定的，可以自定义修改。提交该 JAR 到集群环境即可直接使用。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm-jar.png"/> </div>



## 四、maven-shade-plugin插件

### 4.1 官方文档说明

第三种方式是使用 maven-shade-plugin，既然已经有了 maven-assembly-plugin，为什么还需要 maven-shade-plugin，这一点在官方文档中也是有所说明的，来自于官方对 HDFS 整合讲解的章节[Storm HDFS Integration](http://storm.apache.org/releases/2.0.0-SNAPSHOT/storm-hdfs.html)，原文如下：

>When packaging your topology, it's important that you use the [maven-shade-plugin](http://storm.apache.org/releases/2.0.0-SNAPSHOT/storm-hdfs.html) as opposed to the [maven-assembly-plugin](http://storm.apache.org/releases/2.0.0-SNAPSHOT/storm-hdfs.html).
>
>The shade plugin provides facilities for merging JAR manifest entries, which the hadoop client leverages for URL scheme resolution.
>
>If you experience errors such as the following:
>
>```
>java.lang.RuntimeException: Error preparing HdfsBolt: No FileSystem for scheme: hdfs
>```
>
>it's an indication that your topology jar file isn't packaged properly.
>
>If you are using maven to create your topology jar, you should use the following `maven-shade-plugin` configuration to create your topology jar。

这里第一句就说的比较清晰，在集成 HDFS 时候，你必须使用 maven-shade-plugin 来代替 maven-assembly-plugin，否则会抛出 RuntimeException 异常。

采用 maven-shade-plugin 打包有很多好处，比如你的工程依赖很多的 JAR 包，而被依赖的 JAR 又会依赖其他的 JAR 包，这样,当工程中依赖到不同的版本的 JAR 时，并且 JAR 中具有相同名称的资源文件时，shade 插件会尝试将所有资源文件打包在一起时，而不是和 assembly 一样执行覆盖操作。

### 4.2 配置

采用 `maven-shade-plugin` 进行打包时候，配置示例如下：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <configuration>
        <createDependencyReducedPom>true</createDependencyReducedPom>
        <filters>
            <filter>
                <artifact>*:*</artifact>
                <excludes>
                    <exclude>META-INF/*.SF</exclude>
                    <exclude>META-INF/*.sf</exclude>
                    <exclude>META-INF/*.DSA</exclude>
                    <exclude>META-INF/*.dsa</exclude>
                    <exclude>META-INF/*.RSA</exclude>
                    <exclude>META-INF/*.rsa</exclude>
                    <exclude>META-INF/*.EC</exclude>
                    <exclude>META-INF/*.ec</exclude>
                    <exclude>META-INF/MSFTSIG.SF</exclude>
                    <exclude>META-INF/MSFTSIG.RSA</exclude>
                </excludes>
            </filter>
        </filters>
        <artifactSet>
            <excludes>
                <exclude>org.apache.storm:storm-core</exclude>
            </excludes>
        </artifactSet>
    </configuration>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer
                       implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                    <transformer
                       implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                    </transformer>
                </transformers>
            </configuration>
        </execution>
    </executions>
</plugin>
```

以上配置示例来源于 Storm Github，这里做一下说明：

在上面的配置中，排除了部分文件，这是因为有些 JAR 包生成时，会使用 jarsigner 生成文件签名（完成性校验），分为两个文件存放在 META-INF 目录下：

+ a signature file, with a .SF extension；
+ a signature block file, with a .DSA, .RSA, or .EC extension；

如果某些包的存在重复引用，这可能会导致在打包时候出现 `Invalid signature file digest for Manifest main attributes` 异常，所以在配置中排除这些文件。

### 4.3 打包命令

使用 maven-shade-plugin 进行打包的时候，打包命令和普通的一样：

```shell
# mvn  package
```

打包后会生成两个 JAR 包，提交到服务器集群时使用 ` 非 original` 开头的 JAR。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm-jar2.png"/> </div>

## 五、结论

通过以上三种打包方式的详细介绍，这里给出最后的结论：**建议使用 maven-shade-plugin 插件进行打包**，因为其通用性最强，操作最简单，并且 Storm Github 中所有[examples](https://github.com/apache/storm/tree/master/examples) 都是采用该方式进行打包。



## 六、打包注意事项

无论采用任何打包方式，都必须排除集群环境中已经提供的 storm jars。这里比较典型的是 storm-core，其在安装目录的 lib 目录下已经存在。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm-lib.png"/> </div>



如果你不排除 storm-core，通常会抛出下面的异常：

```properties
Caused by: java.lang.RuntimeException: java.io.IOException: Found multiple defaults.yaml resources.   
You're probably bundling the Storm jars with your topology jar.   
[jar:file:/usr/app/apache-storm-1.2.2/lib/storm-core-1.2.2.jar!/defaults.yaml,   
jar:file:/usr/appjar/storm-hdfs-integration-1.0.jar!/defaults.yaml]
        at org.apache.storm.utils.Utils.findAndReadConfigFile(Utils.java:384)
        at org.apache.storm.utils.Utils.readDefaultConfig(Utils.java:428)
        at org.apache.storm.utils.Utils.readStormConfig(Utils.java:464)
        at org.apache.storm.utils.Utils.<clinit>(Utils.java:178)
        ... 39 more
```

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/storm-jar-complie-error.png"/> </div>



## 参考资料

关于 maven-shade-plugin 的更多配置可以参考： [maven-shade-plugin 入门指南](https://www.jianshu.com/p/7a0e20b30401)


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>