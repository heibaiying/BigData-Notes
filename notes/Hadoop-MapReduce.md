# 分布式计算框架——MapReduce

<nav>
<a href="#一MapReduce-概述">一、MapReduce 概述</a><br/>
<a href="#二MapReduce-编程模型简述">二、MapReduce 编程模型简述</a><br/>
<a href="#三combiner--partitioner">三、combiner & partitioner</a><br/>
<a href="#四MapReduce-词频统计案例">四、MapReduce 词频统计案例</a><br/>
<a href="#五词频统计案例进阶">五、词频统计案例进阶</a><br/>
</nav>






## 一、MapReduce 概述

Hadoop MapReduce是一个分布式计算框架，用于编写批处理应用程序。编写好的程序可以提交到Hadoop集群上用于并行处理大规模的数据集。

MapReduce作业通过将输入的数据集拆分为独立的块，这些块由`map`以并行的方式处理；框架对`map`的输出进行排序，然后输入到`reduce`中。MapReduce框架专门用于`<key，value>`键值对处理，也就是说，框架将作业的输入视为一组`<key，value>`对，并生成一组`<key，value>`对作为输出。输出和输出的`key`和`value`都必须实现[Writable](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Writable.html) 接口。

```
(input) <k1, v1> -> map -> <k2, v2> -> combine -> <k2, v2> -> reduce -> <k3, v3> (output)
```



## 二、MapReduce 编程模型简述

这里以词频统计为例说明MapReduce的编程模型，下图为词频统计的流程图：

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduceProcess.png"/> </div>

1. **input** : 读取文本文件；

2. **splitting** : 将文件按照行进行拆分，此时得到的`K1`行数，`V1`表示对应行的文本内容；

3. **mapping** : 并行将每一行按照空格进行拆分，拆分得到的`List(K2,V2)`，其中`K2`代表每一个单词，由于是做词频统计，所以`V2`的值为1，代表出现1次；
4. **shuffling**：由于`Mapping`操作可能是在不同的机器上并行处理的，所以需要通过`shuffling`将相同`key`值的数据分发到同一个节点上去合并，这样才能统计出最终的结果，此时得到`K2`为每一个单词，`List(V2)`为可迭代集合，`V2`就是Mapping中的V2；
5. **Reducing** : 这里的案例是统计单词出现的总次数，所以`Reducing`对`List(V2)`进行归约求和操作，最终输出。

MapReduce编程模型中`splitting` 和`shuffing`操作都是由框架实现的，需要我们自己编程实现的只有`mapping`和`reducing`，这也就是框架MapReduce名字的来源。



## 三、combiner & partitioner

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/Detailed-Hadoop-MapReduce-Data-Flow-14.png"/> </div>

### 3.1 InputFormat & RecordReaders 

`InputFormat`将输出文件拆分为多个`InputSplit`，并由`RecordReaders`将`InputSplit`转换为标准的<key,value>键值对，作为map的输出。这一步的意义在于只有先进行逻辑拆分并转为标准的键值对格式后，才能为多个`map`提供输入，进行并行处理。

`InputFormat` 为一个抽象类，其中只定义了两个抽象方法，具体的操作则由其实现类来进行。其源码如下：

- **getSplits**：将输入文件拆分为多个InputSplit；
- **createRecordReader**: 定义RecordReader的创建方法；

```java
public abstract class InputFormat<K, V> {

  public abstract 
    List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException;
  

  public abstract 
    RecordReader<K,V> createRecordReader(InputSplit split,
                                         TaskAttemptContext context
                                        ) throws IOException, 
                                                 InterruptedException;

}
```

### 3.2 combiner

`combiner`是`map`运算后的可选操作，它实际上是一个本地化的`reduce`操作，它主要是在`map`计算出中间文件后做一个简单的合并重复`key`值的操作。这里以词频统计为例：

`map`在遇到一个hadoop的单词时就会记录为1，但是这篇文章里hadoop可能会出现n多次，那么`map`输出文件冗余就会很多，因此在`reduce`计算前对相同的key做一个合并操作，那么需要传输的数据量就会减少，传输效率就可以得到提升。

但并非所有场景都适合使用`combiner`，使用它的原则是`combiner`的输出不会影响到`reduce`计算的最终输入，例如：如果计算只是求总数，最大值，最小值时都可以使用`combiner`，但是做平均值计算则不能使用`combiner`。

不使用combiner的情况：

<div align="center"> <img  width="600px"  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduce-without-combiners.png"/> </div>

使用combiner的情况：

<div align="center"> <img width="600px"  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduce-with-combiners.png"/> </div>



可以看到使用combiner的时候，需要传输到reducer中的数据由12keys，降低到10keys。降低的幅度取决于你keys的重复率，后文词频统计案例可以直观演示用combiner降低数百倍的传输量。

### 3.3 partitioner

`partitioner`可以理解成分类器，将`map`的输出按照key值的不同分别分给对应的`reducer`，支持自定义实现，在后文案例中会有演示。



## 四、MapReduce 词频统计案例

> 源码下载地址：[hadoop-word-count](https://github.com/heibaiying/BigData-Notes/tree/master/code/Hadoop/hadoop-word-count)

### 4.1 项目简介

这里给出一个经典的案例:词频统计。统计如下样本数据中每个单词出现的次数。

```properties
Spark	HBase
Hive	Flink	Storm	Hadoop	HBase	Spark
Flink
HBase	Storm
HBase	Hadoop	Hive	Flink
HBase	Flink	Hive	Storm
Hive	Flink	Hadoop
HBase	Hive
Hadoop	Spark	HBase	Storm
HBase	Hadoop	Hive	Flink
HBase	Flink	Hive	Storm
Hive	Flink	Hadoop
HBase	Hive
```

为方便大家开发，我在项目源码中放置了一个工具类`WordCountDataUtils`，用于产生词频统计样本文件：


```java
public class WordCountDataUtils {

    public static final List<String> WORD_LIST = Arrays.asList("Spark", "Hadoop", "HBase", 
                                                                "Storm", "Flink", "Hive");


    /**
     * 模拟产生词频数据
     *
     * @return 词频数据
     */
    private static String generateData() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            Collections.shuffle(WORD_LIST);
            Random random = new Random();
            int endIndex = random.nextInt(WORD_LIST.size()) % (WORD_LIST.size()) + 1;
            String line = StringUtils.join(WORD_LIST.toArray(), "\t", 0, endIndex);
            builder.append(line).append("\n");
        }
        return builder.toString();
    }


    /**
     * 模拟产生词频数据并输出到本地
     *
     * @param outputPath 输出文件路径
     */
    private static void generateDataToLocal(String outputPath) {
        try {
            java.nio.file.Path path = Paths.get(outputPath);
            if (Files.exists(path)) {
                Files.delete(path);
            }
            Files.write(path, generateData().getBytes(), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 模拟产生词频数据并输出到HDFS
     *
     * @param hdfsUrl          HDFS地址
     * @param user             hadoop用户名
     * @param outputPathString 存储到HDFS上的路径
     */
    private static void generateDataToHDFS(String hdfsUrl, String user, String outputPathString) {
        FileSystem fileSystem = null;
        try {
            fileSystem = FileSystem.get(new URI(hdfsUrl), new Configuration(), user);
            Path outputPath = new Path(outputPathString);
            if (fileSystem.exists(outputPath)) {
                fileSystem.delete(outputPath, true);
            }
            FSDataOutputStream out = fileSystem.create(outputPath);
            out.write(generateData().getBytes());
            out.flush();
            out.close();
            fileSystem.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
       //generateDataToLocal("input.txt");
       generateDataToHDFS("hdfs://192.168.0.107:8020", "root", "/wordcount/input.txt");
    }
}
```

### 4.2 WordCountMapper

```java
/**
 * 将每行数据按照指定分隔符进行拆分
 */
public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    @Override
    protected void map(LongWritable key, Text value, Context context) throws IOException, 
                                                                      InterruptedException {
        String[] words = value.toString().split("\t");
        for (String word : words) {
            context.write(new Text(word), new IntWritable(1));
        }
    }

}
```


WordCountMapper对应下图的Mapping操作，这里WordCountMapper继承自Mapper类，这是一个泛型类，定义如下：

```java
public class Mapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {
   ......
}
```

+ **KEYIN** : mapping输入的key的数据类型，即每行的偏移量(每行第一个字符在文本中的位置)，Long类型，对应Hadoop中的LongWritable类型；
+ **VALUEIN** : mappin输入的value的数据类型，即每行数据；String类型，对应Hadoop中Text类型；
+ **KEYOUT** ：mapping输出的key的数据类型，即每个单词；String类型，对应Hadoop中Text类型；
+ **VALUEOUT**：mapping输出的value的数据类型，即每个单词出现的次数；这里用int类型，对应Hadoop中IntWritable类型；

在MapReduce中必须使用Hadoop定义的类型，因为Hadoop预定义的类型都是可序列化，可比较的，所有类型均实现了`WritableComparable`接口。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-code-mapping.png"/> </div>

### 4.3 WordCountReducer

```java
/**
 * 进行词频统计
 */
public class WordCountReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, 
                                                                                  InterruptedException {
        int count = 0;
        for (IntWritable value : values) {
            count += value.get();
        }
        context.write(key, new IntWritable(count));
    }
}
```

这里的key是每个单词，values是一个可迭代的数据类型，因为shuffling输出的数据实际上是下图中所示的这样的，即`key，(1,1,1,1,1,1,1,.....)`。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-code-reducer.png"/> </div>

### 4.4 WordCountApp

组装MapReduce作业，并提交到服务器运行，代码如下：

```java

/**
 * 组装作业 并提交到集群运行
 */
public class WordCountApp {


    // 这里为了直观显示参数 使用了硬编码，实际开发中可以通过外部传参
    private static final String HDFS_URL = "hdfs://192.168.0.107:8020";
    private static final String HADOOP_USER_NAME = "root";

    public static void main(String[] args) throws Exception {

        //  文件输入路径和输出路径由外部传参指定
        if (args.length < 2) {
            System.out.println("Input and output paths are necessary!");
            return;
        }

        // 需要指明hadoop用户名，否则在HDFS上创建目录时可能会抛出权限不足的异常
        System.setProperty("HADOOP_USER_NAME", HADOOP_USER_NAME);

        Configuration configuration = new Configuration();
        // 指明HDFS的地址
        configuration.set("fs.defaultFS", HDFS_URL);

        // 创建一个Job
        Job job = Job.getInstance(configuration);

        // 设置运行的主类
        job.setJarByClass(WordCountApp.class);

        // 设置Mapper和Reducer
        job.setMapperClass(WordCountMapper.class);
        job.setReducerClass(WordCountReducer.class);

        // 设置Mapper输出key和value的类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        // 设置Reducer输出key和value的类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        // 如果输出目录已经存在，则必须先删除，否则重复运行程序时会抛出异常
        FileSystem fileSystem = FileSystem.get(new URI(HDFS_URL), configuration, HADOOP_USER_NAME);
        Path outputPath = new Path(args[1]);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        // 设置作业输入文件和输出文件的路径
        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, outputPath);

        // 将作业提交到群集并等待它完成，参数设置为true代表打印显示对应的进度
        boolean result = job.waitForCompletion(true);

        // 关闭之前创建的fileSystem
        fileSystem.close();

        // 根据作业结果,终止当前运行的Java虚拟机,退出程序
        System.exit(result ? 0 : -1);

    }
}
```

这里说明一下：`setMapOutputKeyClass`和`setOutputValueClass`用于设置reducer函数的输出类型。map函数的输出类型默认情况下和reducer函数式相同的，如果不同，则必须通过`setMapOutputKeyClass`和`setMapOutputValueClass`进行设置。

### 4.5 提交到服务器运行

在实际开发中，可以在本机配置hadoop开发环境，直接运行`main`方法既可。这里主要介绍一下打包提交到服务器运行：

由于本项目没有使用除Hadoop外的第三方依赖，直接打包即可：

```shell
# mvn clean package
```

使用以下命令运行作业：

```shell
hadoop jar /usr/appjar/hadoop-word-count-1.0.jar \
com.heibaiying.WordCountApp \
/wordcount/input.txt /wordcount/output/WordCountApp
```

作业完成后查看HDFS上生成目录：

```shell
# 查看目录
hadoop fs -ls /wordcount/output/WordCountApp

# 查看统计结果
hadoop fs -cat /wordcount/output/WordCountApp/part-r-00000
```

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-wordcountapp.png"/> </div>



## 五、词频统计案例进阶

## 5.1 combiner

### 1. combiner的代码实现

combiner的代码实现比较简单，只要在组装作业时，添加下面一行代码即可：

```java
// 设置Combiner
job.setCombinerClass(WordCountReducer.class);
```

### 2. 测试结果

加入combiner后统计结果是不会有变化的，但是我们可以从打印的日志看出combiner的效果：

没有加入combiner的打印日志：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-no-combiner.png"/> </div>

加入combiner后的打印日志如下。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-combiner.png"/> </div>

这里我们只有一个输入文件并且小于128M，所以只有一个Map进行处理，可以看到经过combiner后，records由3519降低为6（样本中单词种类就只有6个），在这个用例中combiner就能极大地降低需要传输的数据量。

## 5.2 Partitioner

### 1.  默认Partitioner规则

这里假设有个需求：将不同单词的统计结果输出到不同文件。这种需求实际上比较常见，比如统计产品的销量时，需要将结果按照产品分类输出。

要实现这个功能，就需要用到自定义Partitioner，这里我们先说一下默认的分区规则：在构建job时候，如果不指定，默认的使用的是`HashPartitioner`，其实现如下：

```java
public class HashPartitioner<K, V> extends Partitioner<K, V> {

  public int getPartition(K key, V value,
                          int numReduceTasks) {
    return (key.hashCode() & Integer.MAX_VALUE) % numReduceTasks;
  }

}
```

对key进行哈希散列并对`numReduceTasks`取余，这里由于`numReduceTasks`默认值为1，所以我们之前的统计结果都输出到同一个文件中。

### 2. 自定义Partitioner

这里我们继承`Partitioner`自定义分区规则，这里按照单词进行分区：

```java
/**
 * 自定义partitioner,按照单词分区
 */
public class CustomPartitioner extends Partitioner<Text, IntWritable> {

    public int getPartition(Text text, IntWritable intWritable, int numPartitions) {
        return WordCountDataUtils.WORD_LIST.indexOf(text.toString());
    }
}
```

并在构建job时候指定使用我们自己的分区规则，并设置reduce的个数：

```java
// 设置自定义分区规则
job.setPartitionerClass(CustomPartitioner.class);
// 设置reduce个数
job.setNumReduceTasks(WordCountDataUtils.WORD_LIST.size());
```



### 3. 测试结果

测试结果如下，分别生成6个文件，每个文件中为对应单词的统计结果。

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/hadoop-wordcountcombinerpartition.png"/> </div>





## 参考资料

1. [分布式计算框架MapReduce](https://zhuanlan.zhihu.com/p/28682581)
2. [Apache Hadoop 2.9.2 > MapReduce Tutorial](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)
3. [MapReduce - Combiners]( https://www.tutorialscampus.com/tutorials/map-reduce/combiners.htm)



