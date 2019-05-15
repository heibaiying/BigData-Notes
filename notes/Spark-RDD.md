

## 弹性式数据集RDDs

## 一、RDD简介

RDD，全称为 Resilient Distributed Datasets，是Spark最基本的数据抽象，它是只读的、分区记录的集合，支持并行操作。RDD可以由物理存储中的数据集创建或从其他RDD转换而来。RDD具备高度的容错性，允许开发人员在大型集群上执行基于内存的并行计算。它具有以下特性：

+ 一个RDD由一个或者多个分区（Partitions）组成，对于RDD来说，每个分区会被一个计算任务所处理，用户可以在创建RDD是指定其分区个数，如果没有指定，则采用程序所分配到的CPU的核心数；
+ 一个用于计算所有分区的函数compute；
+ RDD之间的依赖关系，RDD的每次转换都会生成一个新的依赖关系，这种RDD之间的依赖关系就像流水线一样。在部分分区数据丢失后，可以通过这种依赖关系重新计算丢失的分区数据，不是对RDD的所有分区进行重新计算；
+ 对于Key-Value型的RDD，还有Partitioner，即分区函数。目前Spark中支持HashPartitioner(按照哈希分区)和RangeParationer(按照范围进行分区)；
+ 一个列表，存储每个Partition的优先位置(prefered location)。对于一个HDFS文件来说，这个列表保存的就是每个分区所在的块的位置，按照“移动数据不如移动计算“的理念，Spark在进行任务调度的时候，会尽可能的将计算任务分配到其所要处理数据块的存储位置。

## 二、创建RDD

RDD是一个的集合。创建RDD有两种方法：

+ 由现有集合创建；
+ 引用外部存储系统中的数据集，例如共享文件系统，HDFS，HBase或支持Hadoop InputFormat的任何数据源。

## 三、操作RDD

RDD支持两种类型的操作：*transformations*（转换，从现有数据集创建新数据集）和*actions*（在数据集上运行计算后将值返回到驱动程序）。RDD中的所有转换操作都是惰性的，它们只是记住这些转换操作，但不会立即执行，只有遇到action操作后才会真正的进行计算，这类似于函数式编程中的惰性求值。



## 四、缓存RDD

Spark速度非常快的一个原因是其支持将RDD缓存到内存中。当缓存一个RDD到内存中后，如果之后的操作使用到了该数据集，则使用内存中缓存的数据。

缓存有丢失的风险，但是由于RDD之间的依赖关系，如果RDD上某个分区的数据丢失，只需要重新计算该分区即可，这是Spark高容错性的基础。





## 五、理解shuffle

### 5.1 shuffle介绍

通常在Spark中，一个任务遇到对应一个分区，但是如果reduceByKey等操作，Spark必须从所有分区读取以查找所有键的所有值，然后将分区中的值汇总在一起以计算每个键的最终结果 ，这称为shuffle。

![spark-reducebykey](D:\BigData-Notes\pictures\spark-reducebykey.png)





### 5.2 Shuffle的影响

Shuffle是一项昂贵的操作，因为它涉及磁盘I/O，网络I/O，和数据序列化。某些shuffle操作还会消耗大量的堆内存，因为它们使用内存中的数据结构来组织传输数据。Shuffle还会在磁盘上生成大量中间文件，从Spark 1.3开始，这些文件将被保留，直到相应的RDD不再使用并进行垃圾回收。这样做是为了避免在计算时重复创建shuffle文件。如果应用程序保留对这些RDD的引用则垃圾回收可能在很长一段时间后才会发生，这意味着长时间运行的Spark作业可能会占用大量磁盘空间。可以使用`spark.local.dir`参数指定临时存储目录。

### 5.3 导致Shuffle的操作

以下操作都会导致Shuffle：

+ 涉及到重新分区操作： 如`repartition` 和 `coalesce`;
+ 所有涉及到ByKey的操作（counting除外）：如`groupByKey`和`reduceByKey`;
+ 联结操作：如`cogroup`和`join`。



## 五、宽依赖和窄依赖

RDD和它的父RDD(s)之间的依赖关系分为两种不同的类型：

- 窄依赖(narrow dependency)：父RDDs的一个分区最多被子RDDs一个分区所依赖；
- 宽依赖(wide dependency)：父RDDs的一个分区可以被子RDDs的多个子分区所依赖。

如下图：每一个方框表示一个 RDD，带有颜色的矩形表示分区

![spark-窄依赖和宽依赖](D:\BigData-Notes\pictures\spark-窄依赖和宽依赖.png)



区分这两种依赖是非常有用的：

+ 首先，窄依赖允许在一个集群节点上以流水线的方式（pipeline）计算所有父分区的数据。例如，逐个元素地执行map、然后filter操作；而宽依赖则需要首先计算好所有父分区数据，然后在节点之间进行Shuffle，这与MapReduce类似。
+ 窄依赖能够更有效地进行失效节点的恢复，即只需重新计算丢失RDD分区的父分区，且不同节点之间可以并行计算；而对于一个宽依赖关系的Lineage图，子RDD部分分区数据的丢失都需要对父RDD的所有分区数据进行再次计算。



## 六、DAG的生成

RDD(s)及其之间的依赖关系组成了DAG(有向无环图)，DAG定义了这些RDD(s)之间的Lineage(血统)关系，通过这些关系，如果一个RDD的部分或者全部计算结果丢失了，也可以重新进行计算。

那么Spark是如何根据DAG来生成计算任务呢？首先，根据依赖关系的不同将DAG划分为不同的阶段(Stage)。

+ 对于窄依赖，由于分区的依赖关系是确定的，其转换操作可以在同一个线程执行，所以可以划分到同一个执行阶段；
+ 对于宽依赖，由于Shuffle的存在，只能在父RDD(s)Shuffle处理完成后，才能开始接下来的计算，因此遇到宽依赖就需要重新划分开始新的阶段。

![spark-DAG](D:\BigData-Notes\pictures\spark-DAG.png)





## 参考资料

1. [RDD：基于内存的集群计算容错抽象](http://shiyanjun.cn/archives/744.html)



