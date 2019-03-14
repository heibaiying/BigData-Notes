# 分布式计算框架——MapReduce

<nav>
<a href="#一MapReduce-概述">一、MapReduce 概述</a><br/>
<a href="#二MapReduce-编程模型简述">二、MapReduce 编程模型简述</a><br/>
<a href="#三MapReduce-编程模型详述">三、MapReduce 编程模型详述</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-InputFormat-&-RecordReaders">3.1 InputFormat & RecordReaders </a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-combiner">3.2 combiner</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-partitioner">3.3 partitioner</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-sort-&-combiner">3.4 sort & combiner</a><br/>
<a href="#四MapReduce-词频统计案例">四、MapReduce 词频统计案例</a><br/>
</nav>

## 一、MapReduce 概述

Hadoop MapReduce是一个分布式计算框架，用于编写应用程序，以可靠，容错的方式在大型集群上并行处理大量数据（多为TB级别数据集）。

MapReduce 作业通常将输入数据集拆分为独立的块，这些块由**map任务**以完全并行的方式处理。框架对**map任务**的输出进行排序，然后输入到**reduce任务**。通常，作业的输入和输出都存储在文件系统中。该框架负责调度任务，监视任务并重新执行失败的任务。

MapReduce框架专门用于`<key，value>`对，也就是说，框架将作业的输入视为一组`<key，value>`对，并生成一组`<key，value>`对作为输出。输出和输出的`key`和`value`都必须实现[Writable](http://hadoop.apache.org/docs/stable/api/org/apache/hadoop/io/Writable.html) 接口。

```
(input) <k1, v1> -> map -> <k2, v2> -> combine -> <k2, v2> -> reduce -> <k3, v3> (output)
```



## 二、MapReduce 编程模型简述

这里以词频统计为例说明MapReduce的编程模型，下图为词频统计的流程图：

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduceProcess.png"/> </div>

1. **input** : 读取文本文件；

2. **splitting** : 将文件按照行进行拆分，此时得到的K1为行数，V1表示对应行的文本内容；

3. **mapping** : 并行将每一行按照空格进行拆分，拆分得到的List(K2,V2),其中K2代表每一个单词，由于是做词频统计，所以其V2为1，代表出现1次；
4. **shuffling**：由于Mapping操作可能是在不同的机器上并行处理的，所以需要通过shuffling将相同的数据分到同一个节点上去合并，这样才能统计出最终的结果，此时得到K2为每一个单词，List(V2)为可迭代集合，V2就是Mapping中的V2；
5. **Reducing** : 这里的案例是统计单词出现的总次数，所以Reducing迭代List(V2)，并计算其和值，最终输出。

MapReduce 编程模型中`splitting` 和` shuffing`操作都是由框架实现的，实际上，主要需要我们实现的是`mapping`和`reducing`

中的编程逻辑，这也就是为何该框架叫做MapReduce的原因。



## 三、MapReduce 编程模型详述

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/Detailed-Hadoop-MapReduce-Data-Flow-14.png"/> </div>

### 3.1 InputFormat & RecordReaders 

、通过InputFormat 将输出文件拆分为多个InputSplit，并由RecordReaders 将InputSplit 转换为标准的<key,value>键值对，作为map的输出。这一步的意义在于只有先进行逻辑拆分并转为标准的格式后，才能为多个map提供输入，进行并行处理；

`InputFormat` 为一个抽象类，其中定义了两个抽象方法，而实际的操作则由其实现类来进行。其源码如下：

**getSplits**：将输入文件拆分为多个InputSplit；

**createRecordReader**: 定义RecordReader的创建方法；

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

combiner是map运算后的可选操作，其实际上是一个本地化的reduce操作，它主要是在map计算出中间文件后做一个简单的合并重复key值的操作。例如我们对文件里的单词频率做统计，map计算时候如果碰到一个hadoop的单词就会记录为1，但是这篇文章里hadoop可能会出现n多次，那么map输出文件冗余就会很多，因此在reduce计算前对相同的key做一个合并操作，那么文件会变小。这样就提高了宽带的传输效率，因为hadoop计算的宽带资源往往是计算的瓶颈也是最为宝贵的资源。

但并非所有场景都适合使用combiner，使用它的原则是combiner的输入不会影响到reduce计算的最终输入，例如：如果计算只是求总数，最大值，最小值可以使用combiner，但是做平均值计算使用combiner的话，最终的reduce计算结果就会出错。

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduce-combiner.png"/> </div>

### 3.3 partitioner

partitioner可以理解成分类器，按照key的不同分别将map的输出分给不同的reduce，可以自定义实现。



### 3.4 sort & combiner

<div align="center"> <img width="600px" src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/mapreduce-sort.png"/> </div>

经过partitioner处理后，每个key-value对都得到分配到的reduecer信息，然后把记录先写入内存(In-memory buffer)。当写入内存的数据越来越多时，当buffer达到一定阀值（默认80M），就开始执行spill（溢写）步骤，即分成小文件写入磁盘。在写之前，先对memory中每个partition进行排序（in-memory sort）。如果数据量大的话，这个步骤会产生很多个spilled文件，如果我们定义了combine，那么在排序之前还会进行combine，最后一个步骤就是merge，把 spill 步骤产生的所有spilled files，merge成一个大的已排序文件。merge是相同的partition之间进行。

Merge是怎样的？如“aaa”从某个map task读取过来时值是5，从另外一个map 读取值是8，因为它们有相同的key，所以得merge成group。什么是group。对于“aaa”就是像这样的：{“aaa”, [5, 8, 10, …]}，数组中的值就是从不同溢写文件中读取出来的，然后再把这些值加起来。请注意，因为merge是将多个溢写文件合并到一个文件，所以可能也有相同的key存在，在这个过程中如果client设置过Combiner，也会使用Combiner来合并相同的key。



## 四、MapReduce 词频统计案例





## 参考资料

1. [分布式计算框架MapReduce](https://zhuanlan.zhihu.com/p/28682581)
2. [Apache Hadoop 2.9.2 > MapReduce Tutorial](http://hadoop.apache.org/docs/stable/hadoop-mapreduce-client/hadoop-mapreduce-client-core/MapReduceTutorial.html)



