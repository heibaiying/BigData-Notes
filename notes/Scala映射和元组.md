# Map & Tuple

<nav>
<a href="#一映射Map">一、映射(Map)</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-构造Map">1.1 构造Map</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-获取值">1.2 获取值</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-新增修改删除值">1.3 新增/修改/删除值</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-遍历Map">1.4 遍历Map</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#15-yield关键字">1.5 yield关键字</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#16-其他Map结构">1.6 其他Map结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#17-可选方法">1.7 可选方法</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#18-与Java互操作">1.8 与Java互操作</a><br/>
<a href="#二元组Tuple">二、元组(Tuple)</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21--模式匹配">2.1  模式匹配</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-zip方法">2.2 zip方法</a><br/>
</nav>



## 一、映射(Map)

### 1.1 构造Map

```scala
// 初始化一个空 map
val scores01 = new HashMap[String, Int]

// 从指定的值初始化 Map（方式一）
val scores02 = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

// 从指定的值初始化 Map（方式二）
val scores03 = Map(("hadoop", 10), ("spark", 20), ("storm", 30))
```

采用上面方式得到的都是不可变 Map(immutable map)，想要得到可变 Map(mutable map)，则需要使用：

```scala
val scores04 = scala.collection.mutable.Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)
```

### 1.2 获取值

```scala
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.获取指定 key 对应的值
  println(scores("hadoop"))

  // 2. 如果对应的值不存在则使用默认值
  println(scores.getOrElse("hadoop01", 100))
}
```

### 1.3 新增/修改/删除值

可变 Map 允许进行新增、修改、删除等操作。

```scala
object ScalaApp extends App {

  val scores = scala.collection.mutable.Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.如果 key 存在则更新
  scores("hadoop") = 100

  // 2.如果 key 不存在则新增
  scores("flink") = 40

  // 3.可以通过 += 来进行多个更新或新增操作
  scores += ("spark" -> 200, "hive" -> 50)

  // 4.可以通过 -= 来移除某个键和值
  scores -= "storm"

  for (elem <- scores) {println(elem)}
}

// 输出内容如下
(spark,200)
(hadoop,100)
(flink,40)
(hive,50)
```

不可变 Map 不允许进行新增、修改、删除等操作，但是允许由不可变 Map 产生新的 Map。

```scala
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  val newScores = scores + ("spark" -> 200, "hive" -> 50)

  for (elem <- scores) {println(elem)}

}

// 输出内容如下
(hadoop,10)
(spark,200)
(storm,30)
(hive,50)
```

### 1.4 遍历Map

```java
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1. 遍历键
  for (key <- scores.keys) { println(key) }

  // 2. 遍历值
  for (value <- scores.values) { println(value) }

  // 3. 遍历键值对
  for ((key, value) <- scores) { println(key + ":" + value) }

}
```

### 1.5 yield关键字

可以使用 `yield` 关键字从现有 Map 产生新的 Map。

```scala
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.将 scores 中所有的值扩大 10 倍
  val newScore = for ((key, value) <- scores) yield (key, value * 10)
  for (elem <- newScore) { println(elem) }


  // 2.将键和值互相调换
  val reversalScore: Map[Int, String] = for ((key, value) <- scores) yield (value, key)
  for (elem <- reversalScore) { println(elem) }

}

// 输出
(hadoop,100)
(spark,200)
(storm,300)

(10,hadoop)
(20,spark)
(30,storm)
```

### 1.6 其他Map结构

在使用 Map 时候，如果不指定，默认使用的是 HashMap，如果想要使用 `TreeMap` 或者 `LinkedHashMap`，则需要显式的指定。

```scala
object ScalaApp extends App {

  // 1.使用 TreeMap,按照键的字典序进行排序
  val scores01 = scala.collection.mutable.TreeMap("B" -> 20, "A" -> 10, "C" -> 30)
  for (elem <- scores01) {println(elem)}

  // 2.使用 LinkedHashMap,按照键值对的插入顺序进行排序
  val scores02 = scala.collection.mutable.LinkedHashMap("B" -> 20, "A" -> 10, "C" -> 30)
  for (elem <- scores02) {println(elem)}
}

// 输出
(A,10)
(B,20)
(C,30)

(B,20)
(A,10)
(C,30)
```

### 1.7 可选方法

```scala
object ScalaApp extends App {

  val scores = scala.collection.mutable.TreeMap("B" -> 20, "A" -> 10, "C" -> 30)

  // 1. 获取长度
  println(scores.size)

  // 2. 判断是否为空
  println(scores.isEmpty)

  // 3. 判断是否包含特定的 key
  println(scores.contains("A"))

}
```

### 1.8 与Java互操作

```scala
import java.util
import scala.collection.{JavaConverters, mutable}

object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // scala map 转 java map
  val javaMap: util.Map[String, Int] = JavaConverters.mapAsJavaMap(scores)

  // java map 转 scala map
  val scalaMap: mutable.Map[String, Int] = JavaConverters.mapAsScalaMap(javaMap)
  
  for (elem <- scalaMap) {println(elem)}
}
```



## 二、元组(Tuple)

元组与数组类似，但是数组中所有的元素必须是同一种类型，而元组则可以包含不同类型的元素。

```scala
scala> val tuple=(1,3.24f,"scala")
tuple: (Int, Float, String) = (1,3.24,scala)
```

### 2.1  模式匹配

可以通过模式匹配来获取元组中的值并赋予对应的变量：

```scala
scala> val (a,b,c)=tuple
a: Int = 1
b: Float = 3.24
c: String = scala
```

如果某些位置不需要赋值，则可以使用下划线代替：

```scala
scala> val (a,_,_)=tuple
a: Int = 1
```

### 2.2 zip方法

```scala
object ScalaApp extends App {

   val array01 = Array("hadoop", "spark", "storm")
  val array02 = Array(10, 20, 30)
    
  // 1.zip 方法得到的是多个 tuple 组成的数组
  val tuples: Array[(String, Int)] = array01.zip(array02)
  // 2.也可以在 zip 后调用 toMap 方法转换为 Map
  val map: Map[String, Int] = array01.zip(array02).toMap
    
  for (elem <- tuples) { println(elem) }
  for (elem <- map) {println(elem)}
}

// 输出
(hadoop,10)
(spark,20)
(storm,30)

(hadoop,10)
(spark,20)
(storm,30)
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7
