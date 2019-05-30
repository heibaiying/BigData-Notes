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

在将Storm Topology提交到服务器集群运行时，需要先将项目进行打包。本文主要对比分析各种打包方式，并将打包过程中需要注意的事项进行说明。主要打包方式有以下三种：

+ 第一种：不加任何插件，直接使用mvn package打包；
+ 第二种：使用maven-assembly-plugin插件进行打包；
+ 第三种：使用maven-shade-plugin进行打包。

以下分别进行详细的说明。



## 二、mvn package

### 2.1 mvn package的局限

不在POM中配置任何插件，直接使用`mvn package`进行项目打包，这对于没有使用外部依赖包的项目是可行的。

但如果项目中使用了第三方JAR包，就会出现问题，因为`mvn package`打包后的JAR中是不含有依赖包的，如果此时你提交到服务器上运行，就会出现找不到第三方依赖的异常。

如果你想采用这种方式进行打包，但是又使用了第三方JAR，有没有解决办法？答案是有的，这一点在官方文档的[Command Line Client](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Command-line-client.html)章节有所讲解，主要解决办法如下。

### 2.2 解决办法

在使用`storm jar`提交Topology时，可以使用如下方式指定第三方依赖：

+ 如果第三方JAR包在本地，可以使用`--jars`指定；
+ 如果第三方JAR包在远程中央仓库，可以使用`--artifacts` 指定，此时如果想要排除某些依赖，可以使用 `^` 符号。指定后Storm会自动到中央仓库进行下载，然后缓存到本地；
+ 如果第三方JAR包在其他仓库，还需要使用 `--artifactRepositories`指明仓库地址，库名和地址使用 `^` 符号分隔。

以下是一个包含上面三种情况的命令示例：

```shell
./bin/storm jar example/storm-starter/storm-starter-topologies-*.jar \
org.apache.storm.starter.RollingTopWords blobstore-remote2 remote  \
--jars "./external/storm-redis/storm-redis-1.1.0.jar,./external/storm-kafka/storm-kafka-1.1.0.jar" \
--artifacts "redis.clients:jedis:2.9.0,org.apache.kafka:kafka_2.10:0.8.2.2^org.slf4j:slf4j-log4j12" \
--artifactRepositories "jboss-repository^http://repository.jboss.com/maven2, \
HDPRepo^http://repo.hortonworks.com/content/groups/public/"
```

这种方式是建立在你能够连接到外网的情况下，如果你的服务器不能连接外网，或者你希望能把项目直接打包成一个`ALL IN ONE`的JAR，即包含所有相关依赖，此时可以采用下面介绍的两个插件。

## 三、maven-assembly-plugin插件

maven-assembly-plugin是官方文档中介绍的打包方法，来源于官方文档：[Running Topologies on a Production Cluster](http://storm.apache.org/releases/2.0.0-SNAPSHOT/Running-topologies-on-a-production-cluster.html)

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

官方文档说明了以下两点：

- maven-assembly-plugin会把所有的依赖一并打包到最后的JAR中；
- 需要排除掉Storm集群环境中已经提供的Storm jars。

所以采用maven-assembly-plugin进行打包时，配置应该如下：

### 1. 基本配置

在POM.xml中引入插件，并指定打包格式的配置文件`assembly.xml`(名称可自定义)：

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

assembly.xml文件内容如下：

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
            <!--排除storm环境中已经提供的storm-core-->
            <excludes>
                <exclude>org.apache.storm:storm-core</exclude>
            </excludes>
        </dependencySet>
    </dependencySets>
</assembly>
```

>在配置文件中不仅可以排除依赖，还可以排除指定的文件，更多的配置规则可以参考官方文档：[Descriptor Format](http://maven.apache.org/plugins/maven-assembly-plugin/assembly.html#)

### 2.  打包命令

采用maven-assembly-plugin进行打包时命令如下：

```shell
# mvn assembly:assembly 
```

打包后会同时生成两个JAR包，其中后缀为`jar-with-dependencies`是含有第三方依赖的JAR包，后缀是由`assembly.xml`中`<id>`标签指定的，可以自定义修改。提交该JAR到集群环境即可直接使用。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-jar.png"/> </div>



## 四、maven-shade-plugin插件

### 4.1 官方文档说明

第三种方式是使用maven-shade-plugin，既然已经有了maven-assembly-plugin，为什么还需要maven-shade-plugin，这一点在官方文档中也是有所说明的，来自于官方对HDFS整合讲解的章节[Storm HDFS Integration](http://storm.apache.org/releases/2.0.0-SNAPSHOT/storm-hdfs.html)，原文如下：

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

这里第一句就说的比较清晰，在集成HDFS时候，你必须使用maven-shade-plugin来代替maven-assembly-plugin，否则会抛出RuntimeException异常。

采用maven-shade-plugin打包有很多好处，比如你的工程依赖很多的JAR包，而被依赖的JAR又会依赖其他的JAR包，这样,当工程中依赖到不同的版本的 JAR时，并且JAR中具有相同名称的资源文件时，shade插件会尝试将所有资源文件打包在一起时，而不是和assembly一样执行覆盖操作。

### 4.2 配置

采用`maven-shade-plugin`进行打包时候，配置示例如下：

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

以上配置示例来源于Storm在Github上的examples，这里做一下说明：

在上面的配置中，排除了部分文件，这是因为有些JAR包生成时，会使用jarsigner生成文件签名（完成性校验），分为两个文件存放在META-INF目录下：

+ a signature file, with a .SF extension；
+ a signature block file, with a .DSA, .RSA, or .EC extension；

如果某些包的存在重复引用，这可能会导致在打包时候出现`Invalid signature file digest for Manifest main attributes`异常，所以在配置中排除这些文件。

### 4.3 打包命令

使用maven-shade-plugin进行打包的时候，打包命令和普通的一样：

```shell
# mvn  package
```

打包后会生成两个JAR包，提交到服务器集群时使用`非original`开头的JAR。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-jar2.png"/> </div>

## 五、结论

通过以上三种打包方式的详细介绍，这里给出最后的结论：**建议使用maven-shade-plugin插件进行打包**，因为其通用性最强，操作最简单，并且Storm Github中所有[examples](https://github.com/apache/storm/tree/master/examples)都是采用该方式进行打包。



## 六、打包注意事项

无论采用任何打包方式，都必须排除集群环境中已经提供的storm jars。这里比较典型的是storm-core，其在安装目录的lib目录下已经存在。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-lib.png"/> </div>



如果你不排除storm-core，通常会抛出下面的异常：

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

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/storm-jar-complie-error.png"/> </div>



## 参考资料

关于maven-shade-plugin的更多配置可以参考： [maven-shade-plugin 入门指南](https://www.jianshu.com/p/7a0e20b30401)
