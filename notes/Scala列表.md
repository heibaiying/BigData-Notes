# 列表(List)

<nav>
<a href="#一List字面量">一、List字面量</a><br/>
<a href="#二List类型">二、List类型</a><br/>
<a href="#三构建List">三、构建List</a><br/>
<a href="#四模式匹配">四、模式匹配</a><br/>
<a href="#五列表的基本操作">五、列表的基本操作</a><br/>
<a href="#六列表的高级操作">六、列表的高级操作</a><br/>
<a href="#七List对象的方法">七、List对象的方法</a><br/>
<a href="#八处理多个列表">八、处理多个列表</a><br/>
</nav>


## 一、List字面量

List是Scala中非常重要的一个数据结构，其与Array(数组)非常类似，但是List是不可变的，和Java中的List一样，其底层实现是链表。

```scala
scala>  val list = List("hadoop", "spark", "storm")
list: List[String] = List(hadoop, spark, storm)

// List是不可变
scala> list(1) = "hive"
<console>:9: error: value update is not a member of List[String]
```

## 二、List类型

Scala中List具有以下两个特性：

+ 同构(homogeneous)：同一个List中的所有元素都必须是相同的类型；
+ 协变(covariant)：如果S是T的子类型，那么`List[S]`就是`List[T]`的子类型，例如`List[String]`是`List[Object]`的子类型。

需要特别说明的是空列表的类型为`List[Nothing]`：

```scala
scala> List()
res1: List[Nothing] = List()
```

## 三、构建List

所有List都由两个基本单元构成：`Nil` 和` ::`(读作"cons")。即列表要么是空列表(Nil)，要么是由一个head加上一个tail组成，而tail又是一个List。我们在上面使用的`List("hadoop", "spark", "storm")`最终也是被解释为` "hadoop"::"spark":: "storm"::Nil`。

```scala
scala>  val list01 = "hadoop"::"spark":: "storm"::Nil
list01: List[String] = List(hadoop, spark, storm)

// :: 操作符号是右结合的，所以上面的表达式和下面的等同
scala> val list02 = "hadoop"::("spark":: ("storm"::Nil))
list02: List[String] = List(hadoop, spark, storm)
```

## 四、模式匹配

Scala支持展开列表以实现模式匹配。

```scala
scala>  val list = List("hadoop", "spark", "storm")
list: List[String] = List(hadoop, spark, storm)

scala> val List(a,b,c)=list
a: String = hadoop
b: String = spark
c: String = storm
```

如果只需要匹配部分内容，可以如下：

```scala
scala> val a::rest=list
a: String = hadoop
rest: List[String] = List(spark, storm)
```

## 五、列表的基本操作

### 5.1 常用方法

```scala
object ScalaApp extends App {

  val list = List("hadoop", "spark", "storm")

  // 1.列表是否为空
  list.isEmpty

  // 2.返回列表中的第一个元素
  list.head

  // 3.返回列表中除第一个元素外的所有元素 这里输出List(spark, storm)
  list.tail

  // 4.tail和head可以结合使用
  list.tail.head

  // 5.返回列表中的最后一个元素 与head相反
  list.init

  // 6.返回列表中除了最后一个元素之外的其他元素 与tail相反 这里输出List(hadoop, spark)
  list.last

  // 7.使用下标访问元素
  list(2)

  // 8.获取列表长度
  list.length

  // 9. 反转列表
  list.reverse

}
```

### 5.2 indices

indices方法返回所有下标。

```scala
scala> list.indices
res2: scala.collection.immutable.Range = Range(0, 1, 2)
```

### 5.3 take & drop & splitAt

- take：获取前n个元素；
- drop：删除前n个元素；
- splitAt：从第几个位置开始拆分。

```scala
scala> list take 2
res3: List[String] = List(hadoop, spark)

scala> list drop 2
res4: List[String] = List(storm)

scala> list splitAt 2
res5: (List[String], List[String]) = (List(hadoop, spark),List(storm))
```

### 5.4 flatten

flatten接收一个由列表组成的列表，并将其进行扁平化操作，返回单个列表。

```scala
scala>  List(List(1, 2), List(3), List(), List(4, 5)).flatten
res6: List[Int] = List(1, 2, 3, 4, 5)
```

### 5.5 zip & unzip

对两个List执行`zip`操作结果如下，返回对应位置元素组成的元组的列表，`unzip`则执行反向操作。

```scala
scala> val list = List("hadoop", "spark", "storm")
scala> val score = List(10,20,30)

scala> val zipped=list zip score
zipped: List[(String, Int)] = List((hadoop,10), (spark,20), (storm,30))

scala> zipped.unzip
res7: (List[String], List[Int]) = (List(hadoop, spark, storm),List(10, 20, 30))
```

### 5.6 toString & mkString

toString 返回 list的字符串表现形式。

```scala
scala> list.toString
res8: String = List(hadoop, spark, storm)
```

如果想改变list的字符串表现形式，可以使用mkString，mkString有三个重载方法：

```scala
// start：前缀  sep：分隔符  end:后缀
def mkString(start: String, sep: String, end: String): String =
  addString(new StringBuilder(), start, sep, end).toString

// seq 分隔符
def mkString(sep: String): String = mkString("", sep, "")

// 如果不指定分隔符 默认使用""分隔
def mkString: String = mkString("")
```

使用示例如下：

```scala
scala> list.mkString
res9: String = hadoopsparkstorm

scala>  list.mkString(",")
res10: String = hadoop,spark,storm

scala> list.mkString("{",",","}")
res11: String = {hadoop,spark,storm}
```

### 5.7 iterator & toArray & copyToArray

iterator 方法返回的是迭代器，这和其他语言的使用是一样的。

```scala
object ScalaApp extends App {

  val list = List("hadoop", "spark", "storm")

  val iterator: Iterator[String] = list.iterator

  while (iterator.hasNext) {
    println(iterator.next)
  }
  
}
```

toArray和toList用于List和数组之间的互相转换。

```scala
scala> val array = list.toArray
array: Array[String] = Array(hadoop, spark, storm)

scala> array.toList
res13: List[String] = List(hadoop, spark, storm)
```

#### 3. copyToArray

copyToArray将List中的元素拷贝到数组中指定位置。

```scala
object ScalaApp extends App {

  val list = List("hadoop", "spark", "storm")
  val array = Array("10", "20", "30")

  list.copyToArray(array,1)

  println(array.toBuffer)
}

// 输出 ：ArrayBuffer(10, hadoop, spark)
```

## 六、列表的高级操作

### 6.1 列表转换：map & flatMap & foreach

map 与 Java 8 函数式编程中的map类似，都是对List中每一个元素执行指定操作。

```scala
scala> List(1,2,3).map(_+10)
res15: List[Int] = List(11, 12, 13)
```

flatMap 与 map 类似，但如果List中的元素还是List，则会对其进行flatten操作。

```scala
scala> list.map(_.toList)
res16: List[List[Char]] = List(List(h, a, d, o, o, p), List(s, p, a, r, k), List(s, t, o, r, m))

scala> list.flatMap(_.toList)
res17: List[Char] = List(h, a, d, o, o, p, s, p, a, r, k, s, t, o, r, m)
```

foreach 要求右侧的操作是一个返回值为Unit的函数，你也可以简单理解为执行一段没有返回值代码。

```scala
scala> var sum = 0
sum: Int = 0

scala> List(1, 2, 3, 4, 5) foreach (sum += _)

scala> sum
res19: Int = 15
```

### 6.2 列表过滤：filter & partition & find & takeWhile & dropWhile & span

filter用于筛选满足条件元素，返回新的List。

```scala
scala> List(1, 2, 3, 4, 5) filter (_ % 2 == 0)
res20: List[Int] = List(2, 4)
```

partition会按照筛选条件对元素进行分组，返回类型是tuple(元组)。

```scala
scala> List(1, 2, 3, 4, 5) partition (_ % 2 == 0)
res21: (List[Int], List[Int]) = (List(2, 4),List(1, 3, 5))
```

find查找第一个满足条件的值，由于可能并不存在这样的值，所以返回类型是`Option`，可以通过`getOrElse`在不存在满足条件值的情况下返回默认值。

```scala
scala> List(1, 2, 3, 4, 5) find (_ % 2 == 0)
res22: Option[Int] = Some(2)

val result: Option[Int] = List(1, 2, 3, 4, 5) find (_ % 2 == 0)
result.getOrElse(10)
```

takeWhile遍历元素，直到遇到第一个不符合条件的值则结束遍历，返回所有遍历到的值。

```scala
scala> List(1, 2, 3, -4, 5) takeWhile (_ > 0)
res23: List[Int] = List(1, 2, 3)
```

takeWhile遍历元素，直到遇到第一个不符合条件的值则结束遍历，返回没有遍历到的值。

```scala
// 第一个值就不满足条件,所以返回列表中所有的值
scala> List(1, 2, 3, -4, 5) dropWhile  (_ < 0)
res24: List[Int] = List(1, 2, 3, -4, 5)


scala> List(1, 2, 3, -4, 5) dropWhile (_ < 3)
res26: List[Int] = List(3, -4, 5)
```

takeWhile遍历元素，直到遇到第一个不符合条件的值则结束遍历，将遍历到的值和为遍历到的值分别放入两个List中返回，返回类型是tuple(元组)。

```scala
scala> List(1, 2, 3, -4, 5) span (_ > 0)
res27: (List[Int], List[Int]) = (List(1, 2, 3),List(-4, 5))
```



### 6.3 列表检查：forall & exists

forall检查List中所有元素，如果所有元素都满足条件，则返回true；

```scala
scala> List(1, 2, 3, -4, 5) forall ( _ > 0 )
res28: Boolean = false
```

exists检查List中的元素，如果某个元素已经满足条件，则返回true。

```scala
scala>  List(1, 2, 3, -4, 5) exists (_ > 0 )
res29: Boolean = true
```



### 6.4 列表排序：sortWith

sortWith对List中所有元素按照指定规则进行排序，由于List是不可变的，所以排序返回一个新的List。

```scala
scala> List(1, -3, 4, 2, 6) sortWith (_ < _)
res30: List[Int] = List(-3, 1, 2, 4, 6)

scala> val list = List( "hive","spark","azkaban","hadoop")
list: List[String] = List(hive, spark, azkaban, hadoop)

scala> list.sortWith(_.length>_.length)
res33: List[String] = List(azkaban, hadoop, spark, hive)
```



## 七、List对象的方法

上面介绍的所有方法都是List类上的方法，下面介绍的是List伴生对象中的方法。

### 7.1 List.range

List.range可以产生指定的前闭后开区间内的值组成的List，它有三个可选参数: start(开始值)，end(结束值，不包含)，step(步长)。

```scala
scala>  List.range(1, 5)
res34: List[Int] = List(1, 2, 3, 4)

scala> List.range(1, 9, 2)
res35: List[Int] = List(1, 3, 5, 7)

scala> List.range(9, 1, -3)
res36: List[Int] = List(9, 6, 3)
```

### 7.2 List.fill

List.fill使用指定值填充List。

```scala
scala> List.fill(3)("hello")
res37: List[String] = List(hello, hello, hello)

scala> List.fill(2,3)("world")
res38: List[List[String]] = List(List(world, world, world), List(world, world, world))
```

### 7.3 List.concat

List.concat用于拼接多个List。

```scala
scala> List.concat(List('a', 'b'), List('c'))
res39: List[Char] = List(a, b, c)

scala> List.concat(List(), List('b'), List('c'))
res40: List[Char] = List(b, c)

scala> List.concat()
res41: List[Nothing] = List()
```



## 八、处理多个列表

当多个List被放入同一个tuple中时候，可以通过zipped对多个List进行关联处理。

```scala
// 两个List对应位置的元素相乘
scala> (List(10, 20), List(3, 4, 5)).zipped.map(_ * _)
res42: List[Int] = List(30, 80)

// 三个List的操作也是一样的
scala> (List(10, 20), List(3, 4, 5), List(100, 200)).zipped.map(_ * _ + _)
res43: List[Int] = List(130, 280)

// 判断第一个List中元素的长度与第二个List中元素的值是否相等
scala>  (List("abc", "de"), List(3, 2)).zipped.forall(_.length == _)
res44: Boolean = true
```





## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7