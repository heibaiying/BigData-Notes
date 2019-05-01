# HDFS Java API
<nav>
<a href="#一-简介">一、 简介</a><br/>
<a href="#二API的使用">二、API的使用</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-FileSystem">2.1 FileSystem</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-创建目录">2.2 创建目录</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-创建指定权限的目录">2.3 创建指定权限的目录</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-创建文件并写入内容">2.4 创建文件，并写入内容</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-判断文件是否存在">2.5 判断文件是否存在</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-查看文件内容">2.6 查看文件内容</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#27-文件重命名">2.7 文件重命名</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#28-删除目录或文件">2.8 删除目录或文件</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#29-上传文件到HDFS">2.9 上传文件到HDFS</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#210-上传大文件并显示上传进度">2.10 上传大文件并显示上传进度</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#211-从HDFS上下载文件">2.11 从HDFS上下载文件</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#212-查看指定目录下所有文件的信息">2.12 查看指定目录下所有文件的信息</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#213-递归查看指定目录下所有文件的信息">2.13 递归查看指定目录下所有文件的信息</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#214-查看文件的块信息">2.14 查看文件的块信息</a><br/>
</nav>

## 一、 简介

Hadoop提供了简单易用的Java API用于操作HDFS。通过这些API，我们可以通过编程来更灵活的操作HDFS。同时从编程体验上来说，这些API的设计确实非常人性化，基本上你只需要一行代码就能完成相应的操作。

想要使用HDFS API，你只需要导入`hadoop-client`这个依赖包即可。以下关于API的操作我均使用单元测试的方法进行演示，完整的POM文件如下：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.heibaiying</groupId>
    <artifactId>hdfs-java-api</artifactId>
    <version>1.0</version>


    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <hadoop.version>2.6.0-cdh5.15.2</hadoop.version>
    </properties>


    <!---配置CDH仓库地址-->
    <repositories>
        <repository>
            <id>cloudera</id>
            <url>https://repository.cloudera.com/artifactory/cloudera-repos/</url>
        </repository>
    </repositories>


    <dependencies>
        <!--Hadoop-client-->
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>hadoop-client</artifactId>
            <version>${hadoop.version}</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

</project>
```



## 二、API的使用

### 2.1 FileSystem

FileSystem是操作HDFS的入口，通过FileSystem我们可以完成对HDFS上文件和目录的所有操作。在使用前需要获取它，这里由于之后的每个单元测试都需要用到FileSystem，所以使用`@Before`注解进行标注。

```java
private static final String HDFS_PATH = "hdfs://192.168.0.106:8020";
private static final String HDFS_USER = "root";
private static FileSystem fileSystem;

@Before
public void prepare() {
    try {
        Configuration configuration = new Configuration();
        // 这里我启动的是单节点的Hadoop,副本系数可以设置为1,不设置的话默认值为3
        configuration.set("dfs.replication", "1");
        fileSystem = FileSystem.get(new URI(HDFS_PATH), configuration, HDFS_USER);
    } catch (IOException e) {
        e.printStackTrace();
    } catch (InterruptedException e) {
        e.printStackTrace();
    } catch (URISyntaxException e) {
        e.printStackTrace();
    }
}


@After
public void destroy() {
    fileSystem = null;
}
```



### 2.2 创建目录

这里目录可以是多级，支持递归创建。

```java
@Test
public void mkDir() throws Exception {
    fileSystem.mkdirs(new Path("/hdfs-api/test0/"));
}
```



### 2.3 创建指定权限的目录

这里`FsPermission(FsAction u, FsAction g, FsAction o)` 的三个参数分别对应创建者权限，同组其他用户权限，其他用户权限，可以使用`FsAction`枚举类中的值进行指定。

```java
@Test
public void mkDirWithPermission() throws Exception {
    fileSystem.mkdirs(new Path("/hdfs-api/test1/"),
            new FsPermission(FsAction.READ_WRITE, FsAction.READ, FsAction.READ));
}
```



### 2.4 创建文件，并写入内容

```java
@Test
public void create() throws Exception {
    // 如果文件存在，默认会覆盖, 可以通过第二个参数进行控制。第三个参数可以控制使用缓冲区的大小
    FSDataOutputStream out = fileSystem.create(new Path("/hdfs-api/test/a.txt"),
                                               true, 4096);
    out.write("hello hadoop!".getBytes());
    out.write("hello spark!".getBytes());
    out.write("hello flink!".getBytes());
    // 强制将缓冲区中内容刷出
    out.flush();
    out.close();
}
```



### 2.5 判断文件是否存在

```java
@Test
public void exist() throws Exception {
    boolean exists = fileSystem.exists(new Path("/hdfs-api/test/a.txt"));
    System.out.println(exists);
}
```



### 2.6 查看文件内容

这里我们查看的是一个小文件的内容，所以直接转换成字符串后输出，对于大文件，还是应该从输出流中读取数据，分批处理。

```java
@Test
public void readToString() throws Exception {
    FSDataInputStream inputStream = fileSystem.open(new Path("/hdfs-api/test/a.txt"));
    String context = inputStreamToString(inputStream, "utf-8");
    System.out.println(context);
}
```

inputStreamToString 是一个自定义方法，实现如下：

```java
/**
 * 把输入流转换为指定编码的字符
 *
 * @param inputStream 输入流
 * @param encode      指定编码类型
 */
private static String inputStreamToString(InputStream inputStream, String encode) {
    try {
        if (encode == null || ("".equals(encode))) {
            encode = "utf-8";
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encode));
        StringBuilder builder = new StringBuilder();
        String str = "";
        while ((str = reader.readLine()) != null) {
            builder.append(str).append("\n");
        }
        return builder.toString();
    } catch (IOException e) {
        e.printStackTrace();
    }
    return null;
}
```



### 2.7 文件重命名

```java
@Test
public void rename() throws Exception {
    Path oldPath = new Path("/hdfs-api/test/a.txt");
    Path newPath = new Path("/hdfs-api/test/b.txt");
    boolean result = fileSystem.rename(oldPath, newPath);
    System.out.println(result);
}
```



### 2.8 删除目录或文件

```java
public void delete() throws Exception {
    /*
     *  第二个参数代表是否递归删除
     *    +  如果path是一个目录且递归删除为true, 则删除该目录及其中所有文件;
     *    +  如果path是一个目录但递归删除为false,则会则抛出异常。
     */
    boolean result = fileSystem.delete(new Path("/hdfs-api/test/b.txt"), true);
    System.out.println(result);
}
```



### 2.9 上传文件到HDFS

```java
@Test
public void copyFromLocalFile() throws Exception {
    // 如果指定的是目录，则会把目录及其中的文件都复制到指定目录下
    Path src = new Path("D:\\BigData-Notes\\notes\\installation");
    Path dst = new Path("/hdfs-api/test/");
    fileSystem.copyFromLocalFile(src, dst);
}
```



### 2.10 上传大文件并显示上传进度

```java
@Test
    public void copyFromLocalBigFile() throws Exception {

        File file = new File("D:\\kafka.tgz");
        final float fileSize = file.length();
        InputStream in = new BufferedInputStream(new FileInputStream(file));

        FSDataOutputStream out = fileSystem.create(new Path("/hdfs-api/test/kafka5.tgz"),
                new Progressable() {
                  long fileCount = 0;

                  public void progress() {
                     fileCount++;
                     // progress方法每上传大约64KB的数据后就会被调用一次
                     System.out.println("上传进度：" + (fileCount * 64 * 1024 / fileSize) * 100 + " %");
                   }
                });

        IOUtils.copyBytes(in, out, 4096);

    }
```



### 2.11 从HDFS上下载文件

```java
@Test
public void copyToLocalFile() throws Exception {
    Path src = new Path("/hdfs-api/test/kafka.tgz");
    Path dst = new Path("D:\\app\\");
    /*
     * 第一个参数控制下载完成后是否删除源文件,默认是true,即删除;
     * 最后一个参数表示是否将RawLocalFileSystem用作本地文件系统;
     * RawLocalFileSystem默认为false,通常情况下可以不设置,
     * 但如果你在执行时候抛出NullPointerException异常,则代表你的文件系统与程序可能存在不兼容的情况(window下常见),
     * 此时可以将RawLocalFileSystem设置为true
     */
    fileSystem.copyToLocalFile(false, src, dst, true);
}
```



### 2.12 查看指定目录下所有文件的信息

```java
public void listFiles() throws Exception {
    FileStatus[] statuses = fileSystem.listStatus(new Path("/hdfs-api"));
    for (FileStatus fileStatus : statuses) {
        //fileStatus的toString方法被重写过，直接打印可以看到所有信息
        System.out.println(fileStatus.toString());
    }
}
```

FileStatus中包含了文件的基本信息，比如文件路径，是否是文件夹，修改时间，访问时间，所有者，所属组，文件权限，是否是符号链接等，输出内容示例如下(这里为了直观，我对输出进行了换行显示)：

```properties
FileStatus{
path=hdfs://192.168.0.106:8020/hdfs-api/test; 
isDirectory=true; 
modification_time=1556680796191; 
access_time=0; 
owner=root; 
group=supergroup; 
permission=rwxr-xr-x; 
isSymlink=false
}
```



### 2.13 递归查看指定目录下所有文件的信息

```java
@Test
public void listFilesRecursive() throws Exception {
    RemoteIterator<LocatedFileStatus> files = fileSystem.listFiles(new Path("/hbase"), true);
    while (files.hasNext()) {
        System.out.println(files.next());
    }
}
```

这里输出和上面类似，只是多了文本大小，副本系数，块大小等信息。

```properties
LocatedFileStatus{
path=hdfs://192.168.0.106:8020/hbase/hbase.version; 
isDirectory=false; 
length=7; 
replication=1; 
blocksize=134217728; 
modification_time=1554129052916; 
access_time=1554902661455; 
owner=root; group=supergroup;
permission=rw-r--r--; 
isSymlink=false}
```



### 2.14 查看文件的块信息

```java
@Test
public void getFileBlockLocations() throws Exception {

    FileStatus fileStatus = fileSystem.getFileStatus(new Path("/hdfs-api/test/kafka.tgz"));
    BlockLocation[] blocks = fileSystem.getFileBlockLocations(fileStatus, 0, fileStatus.getLen());
    for (BlockLocation block : blocks) {
        System.out.println(block);
    }
}
```

块输出信息比较简单，第一个值是文件的起始偏移量(offset)，第二个值是文件大小(length)，第三个是块所在的主机名(hosts)。

```
0,57028557,hadoop001
```

这里我上传的文件比较小，只有57M(小于128M)，且程序中设置了副本系数为1，所有只有一个块信息，如果文件很大，则这里会输出文件所有块的信息。



**以上所有测试用例源码可以从本仓库进行下载**：[HDFS Java API](https://github.com/heibaiying/BigData-Notes/tree/master/code/Hadoop/hdfs-java-api)