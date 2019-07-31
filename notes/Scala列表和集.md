# List & Set

<nav>
<a href="#一List字面量">一、List字面量</a><br/>
<a href="#二List类型">二、List类型</a><br/>
<a href="#三构建List">三、构建List</a><br/>
<a href="#四模式匹配">四、模式匹配</a><br/>
<a href="#五列表的基本操作">五、列表的基本操作</a><br/>
<a href="#六列表的高级操作">六、列表的高级操作</a><br/>
<a href="#七List对象的方法">七、List对象的方法</a><br/>
<a href="#八处理多个List">八、处理多个List</a><br/>
<a href="#九缓冲列表ListBuffer">九、缓冲列表ListBuffer</a><br/>
<a href="#十集Set">十、集(Set)</a><br/>
</nav>



## 一、List字面量

List 是 Scala 中非常重要的一个数据结构，其与 Array(数组) 非常类似，但是 List 是不可变的，和 Java 中的 List 一样，其底层实现是链表。

```scala
scala>  val list = List("hadoop", "spark", "storm")
list: List[String] = List(hadoop, spark, storm)

// List 是不可变
scala> list(1) = "hive"
<console>:9: error: value update is not a member of List[String]
```

## 二、List类型

Scala 中 List 具有以下两个特性：

+ **同构 (homogeneous)**：同一个 List 中的所有元素都必须是相同的类型；
+ **协变 (covariant)**：如果 S 是 T 的子类型，那么 `List[S]` 就是 `List[T]` 的子类型，例如 `List[String]` 是 `List[Object]` 的子类型。

需要特别说明的是空列表的类型为 `List[Nothing]`：

```scala
scala> List()
res1: List[Nothing] = List()
```

## 三、构建List

所有 List 都由两个基本单元构成：`Nil` 和 `::`(读作"cons")。即列表要么是空列表 (Nil)，要么是由一个 head 加上一个 tail 组成，而 tail 又是一个 List。我们在上面使用的 `List("hadoop", "spark", "storm")` 最终也是被解释为 ` "hadoop"::"spark":: "storm"::Nil`。

```scala
scala>  val list01 = "hadoop"::"spark":: "storm"::Nil
list01: List[String] = List(hadoop, spark, storm)

// :: 操作符号是右结合的，所以上面的表达式和下面的等同
scala> val list02 = "hadoop"::("spark":: ("storm"::Nil))
list02: List[String] = List(hadoop, spark, storm)
```

## 四、模式匹配

Scala 支持展开列表以实现模式匹配。

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

  // 3.返回列表中除第一个元素外的所有元素 这里输出 List(spark, storm)
  list.tail

  // 4.tail 和 head 可以结合使用
  list.tail.head

  // 5.返回列表中的最后一个元素 与 head 相反
  list.init

  // 6.返回列表中除了最后一个元素之外的其他元素 与 tail 相反 这里输出 List(hadoop, spark)
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

indices 方法返回所有下标。

```scala
scala> list.indices
res2: scala.collection.immutable.Range = Range(0, 1, 2)
```

### 5.3 take & drop & splitAt

- **take**：获取前 n 个元素；
- **drop**：删除前 n 个元素；
- **splitAt**：从第几个位置开始拆分。

```scala
scala> list take 2
res3: List[String] = List(hadoop, spark)

scala> list drop 2
res4: List[String] = List(storm)

scala> list splitAt 2
res5: (List[String], List[String]) = (List(hadoop, spark),List(storm))
```

### 5.4 flatten

flatten 接收一个由列表组成的列表，并将其进行扁平化操作，返回单个列表。

```scala
scala>  List(List(1, 2), List(3), List(), List(4, 5)).flatten
res6: List[Int] = List(1, 2, 3, 4, 5)
```

### 5.5 zip & unzip

对两个 List 执行 `zip` 操作结果如下，返回对应位置元素组成的元组的列表，`unzip` 则执行反向操作。

```scala
scala> val list = List("hadoop", "spark", "storm")
scala> val score = List(10,20,30)

scala> val zipped=list zip score
zipped: List[(String, Int)] = List((hadoop,10), (spark,20), (storm,30))

scala> zipped.unzip
res7: (List[String], List[Int]) = (List(hadoop, spark, storm),List(10, 20, 30))
```

### 5.6 toString & mkString

toString 返回 List 的字符串表现形式。

```scala
scala> list.toString
res8: String = List(hadoop, spark, storm)
```

如果想改变 List 的字符串表现形式，可以使用 mkString。mkString 有三个重载方法，方法定义如下：

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

toArray 和 toList 用于 List 和数组之间的互相转换。

```scala
scala> val array = list.toArray
array: Array[String] = Array(hadoop, spark, storm)

scala> array.toList
res13: List[String] = List(hadoop, spark, storm)
```

copyToArray 将 List 中的元素拷贝到数组中指定位置。

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

map 与 Java 8 函数式编程中的 map 类似，都是对 List 中每一个元素执行指定操作。

```scala
scala> List(1,2,3).map(_+10)
res15: List[Int] = List(11, 12, 13)
```

flatMap 与 map 类似，但如果 List 中的元素还是 List，则会对其进行 flatten 操作。

```scala
scala> list.map(_.toList)
res16: List[List[Char]] = List(List(h, a, d, o, o, p), List(s, p, a, r, k), List(s, t, o, r, m))

scala> list.flatMap(_.toList)
res17: List[Char] = List(h, a, d, o, o, p, s, p, a, r, k, s, t, o, r, m)
```

foreach 要求右侧的操作是一个返回值为 Unit 的函数，你也可以简单理解为执行一段没有返回值代码。

```scala
scala> var sum = 0
sum: Int = 0

scala> List(1, 2, 3, 4, 5) foreach (sum += _)

scala> sum
res19: Int = 15
```

### 6.2 列表过滤：filter & partition & find & takeWhile & dropWhile & span

filter 用于筛选满足条件元素，返回新的 List。

```scala
scala> List(1, 2, 3, 4, 5) filter (_ % 2 == 0)
res20: List[Int] = List(2, 4)
```

partition 会按照筛选条件对元素进行分组，返回类型是 tuple(元组)。

```scala
scala> List(1, 2, 3, 4, 5) partition (_ % 2 == 0)
res21: (List[Int], List[Int]) = (List(2, 4),List(1, 3, 5))
```

find 查找第一个满足条件的值，由于可能并不存在这样的值，所以返回类型是 `Option`，可以通过 `getOrElse` 在不存在满足条件值的情况下返回默认值。

```scala
scala> List(1, 2, 3, 4, 5) find (_ % 2 == 0)
res22: Option[Int] = Some(2)

val result: Option[Int] = List(1, 2, 3, 4, 5) find (_ % 2 == 0)
result.getOrElse(10)
```

takeWhile 遍历元素，直到遇到第一个不符合条件的值则结束遍历，返回所有遍历到的值。

```scala
scala> List(1, 2, 3, -4, 5) takeWhile (_ > 0)
res23: List[Int] = List(1, 2, 3)
```

dropWhile 遍历元素，直到遇到第一个不符合条件的值则结束遍历，返回所有未遍历到的值。

```scala
// 第一个值就不满足条件,所以返回列表中所有的值
scala> List(1, 2, 3, -4, 5) dropWhile  (_ < 0)
res24: List[Int] = List(1, 2, 3, -4, 5)


scala> List(1, 2, 3, -4, 5) dropWhile (_ < 3)
res26: List[Int] = List(3, -4, 5)
```

span 遍历元素，直到遇到第一个不符合条件的值则结束遍历，将遍历到的值和未遍历到的值分别放入两个 List 中返回，返回类型是 tuple(元组)。

```scala
scala> List(1, 2, 3, -4, 5) span (_ > 0)
res27: (List[Int], List[Int]) = (List(1, 2, 3),List(-4, 5))
```



### 6.3 列表检查：forall & exists

forall 检查 List 中所有元素，如果所有元素都满足条件，则返回 true。

```scala
scala> List(1, 2, 3, -4, 5) forall ( _ > 0 )
res28: Boolean = false
```

exists 检查 List 中的元素，如果某个元素已经满足条件，则返回 true。

```scala
scala>  List(1, 2, 3, -4, 5) exists (_ > 0 )
res29: Boolean = true
```



### 6.4 列表排序：sortWith

sortWith 对 List 中所有元素按照指定规则进行排序，由于 List 是不可变的，所以排序返回一个新的 List。

```scala
scala> List(1, -3, 4, 2, 6) sortWith (_ < _)
res30: List[Int] = List(-3, 1, 2, 4, 6)

scala> val list = List( "hive","spark","azkaban","hadoop")
list: List[String] = List(hive, spark, azkaban, hadoop)

scala> list.sortWith(_.length>_.length)
res33: List[String] = List(azkaban, hadoop, spark, hive)
```



## 七、List对象的方法

上面介绍的所有方法都是 List 类上的方法，下面介绍的是 List 伴生对象中的方法。

### 7.1 List.range

List.range 可以产生指定的前闭后开区间内的值组成的 List，它有三个可选参数: start(开始值)，end(结束值，不包含)，step(步长)。

```scala
scala>  List.range(1, 5)
res34: List[Int] = List(1, 2, 3, 4)

scala> List.range(1, 9, 2)
res35: List[Int] = List(1, 3, 5, 7)

scala> List.range(9, 1, -3)
res36: List[Int] = List(9, 6, 3)
```

### 7.2 List.fill

List.fill 使用指定值填充 List。

```scala
scala> List.fill(3)("hello")
res37: List[String] = List(hello, hello, hello)

scala> List.fill(2,3)("world")
res38: List[List[String]] = List(List(world, world, world), List(world, world, world))
```

### 7.3 List.concat

List.concat 用于拼接多个 List。

```scala
scala> List.concat(List('a', 'b'), List('c'))
res39: List[Char] = List(a, b, c)

scala> List.concat(List(), List('b'), List('c'))
res40: List[Char] = List(b, c)

scala> List.concat()
res41: List[Nothing] = List()
```



## 八、处理多个List

当多个 List 被放入同一个 tuple 中时候，可以通过 zipped 对多个 List 进行关联处理。

```scala
// 两个 List 对应位置的元素相乘
scala> (List(10, 20), List(3, 4, 5)).zipped.map(_ * _)
res42: List[Int] = List(30, 80)

// 三个 List 的操作也是一样的
scala> (List(10, 20), List(3, 4, 5), List(100, 200)).zipped.map(_ * _ + _)
res43: List[Int] = List(130, 280)

// 判断第一个 List 中元素的长度与第二个 List 中元素的值是否相等
scala>  (List("abc", "de"), List(3, 2)).zipped.forall(_.length == _)
res44: Boolean = true
```



## 九、缓冲列表ListBuffer

上面介绍的 List，由于其底层实现是链表，这意味着能快速访问 List 头部元素，但对尾部元素的访问则比较低效，这时候可以采用 `ListBuffer`，ListBuffer 提供了在常量时间内往头部和尾部追加元素。

```scala
import scala.collection.mutable.ListBuffer

object ScalaApp extends App {

  val buffer = new ListBuffer[Int]
  // 1.在尾部追加元素
  buffer += 1
  buffer += 2
  // 2.在头部追加元素
  3 +=: buffer
  // 3. ListBuffer 转 List
  val list: List[Int] = buffer.toList
  println(list)
}

//输出：List(3, 1, 2)
```



## 十、集(Set)

Set 是不重复元素的集合。分为可变 Set 和不可变 Set。

### 10.1 可变Set

```scala
object ScalaApp extends App {

  // 可变 Set
  val mutableSet = new collection.mutable.HashSet[Int]

  // 1.添加元素
  mutableSet.add(1)
  mutableSet.add(2)
  mutableSet.add(3)
  mutableSet.add(3)
  mutableSet.add(4)

  // 2.移除元素
  mutableSet.remove(2)
  
  // 3.调用 mkString 方法 输出 1,3,4
  println(mutableSet.mkString(","))

  // 4. 获取 Set 中最小元素
  println(mutableSet.min)

  // 5. 获取 Set 中最大元素
  println(mutableSet.max)

}
```

### 10.2 不可变Set

不可变 Set 没有 add 方法，可以使用 `+` 添加元素，但是此时会返回一个新的不可变 Set，原来的 Set 不变。

```scala
object ScalaApp extends App {
  
  // 不可变 Set
  val immutableSet = new collection.immutable.HashSet[Int]

  val ints: HashSet[Int] = immutableSet+1

  println(ints)

}

// 输出 Set(1)
```

### 10.3 Set间操作

多个 Set 之间可以进行求交集或者合集等操作。

```scala
object ScalaApp extends App {

  // 声明有序 Set
  val mutableSet = collection.mutable.SortedSet(1, 2, 3, 4, 5)
  val immutableSet = collection.immutable.SortedSet(3, 4, 5, 6, 7)
  
  // 两个 Set 的合集  输出：TreeSet(1, 2, 3, 4, 5, 6, 7)
  println(mutableSet ++ immutableSet)

  // 两个 Set 的交集  输出：TreeSet(3, 4, 5)
  println(mutableSet intersect immutableSet)

}
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7
