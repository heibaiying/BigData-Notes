# Scala映射和元组

## 一、映射(Map)

### 1.1 构造映射

```scala
scala> import scala.collection.immutable.HashMap
import scala.collection.immutable.HashMap

// 初始化一个空map
scala> val scores01 = new HashMap[String, Int]
scores01: scala.collection.immutable.HashMap[String,Int] = Map()

// 从指定的值初始化映射（方式一）
scala>  val scores02 = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)
scores02: scala.collection.immutable.Map[String,Int] = Map(hadoop -> 10, spark -> 20, storm -> 30)

// 从指定的值初始化映射（方式二）
scala>  val scores03 = Map(("hadoop", 10), ("spark", 20), ("storm", 30))
scores03: scala.collection.immutable.Map[String,Int] = Map(hadoop -> 10, spark -> 20, storm -> 30)
```

采用上面方式得到的都是不可变(immutable)映射，想要得到可变映射，则用：

```scala
scala> val scores04 = scala.collection.mutable.Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)
scores04: scala.collection.mutable.Map[String,Int] = Map(spark -> 20, hadoop -> 10, storm -> 30)
```

### 1.2 获取值

```scala
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.获取指定key对应的值
  println(scores("hadoop"))

  // 2. 如果对应的值不存在则使用默认值
  println(scores.getOrElse("hadoop01", 100))
}
```

### 1.3 新增/修改/删除值

可变映射允许进行新增、修改、删除等操作。

```scala
object ScalaApp extends App {

  val scores = scala.collection.mutable.Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.如果key存在则更新
  scores("hadoop") = 100

  // 2.如果key不存在则新增
  scores("flink") = 40

  // 3.可以通过+=来进行多个更新或新增操作
  scores += ("spark" -> 200, "hive" -> 50)

  // 4.可以通过-= 来移除某个键和值
  scores -= "storm"

  for (elem <- scores) {println(elem)}
}

// 输出内容如下
(spark,200)
(hadoop,100)
(flink,40)
(hive,50)
```

不可变映射不允许进行新增、修改、删除等操作，但是允许由不可变映射产生新的映射。

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

### 1.4 遍历映射

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

### 1.5 产生新映射

可以使用`yield`关键字从现有映射产生新的映射。

```scala
object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // 1.将scores中所有的值扩大10倍
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

### 1.6 有序映射

在使用Map时候，如果不指定，默认使用的是HashMap，如果想要使用`TreeMap`或者`LinkedHashMap`，则需要显式的指定。

```scala
object ScalaApp extends App {

  // 1.使用TreeMap,按照键的字典序进行排序
  val scores01 = scala.collection.mutable.TreeMap("B" -> 20, "A" -> 10, "C" -> 30)
  for (elem <- scores01) {println(elem)}

  // 2.使用LinkedHashMap,按照键值对的插入顺序进行排序
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

### 1.7 其他方法

```scala
object ScalaApp extends App {

  val scores = scala.collection.mutable.TreeMap("B" -> 20, "A" -> 10, "C" -> 30)

  // 1. 获取长度
  println(scores.size)

  // 2. 判断是否为空
  println(scores.isEmpty)

  // 3. 判断是否包含特定的key
  println(scores.contains("A"))

}
```

### 1.8 与Java互操作

```scala
import java.util
import scala.collection.{JavaConverters, mutable}

object ScalaApp extends App {

  val scores = Map("hadoop" -> 10, "spark" -> 20, "storm" -> 30)

  // scala map转java map
  val javaMap: util.Map[String, Int] = JavaConverters.mapAsJavaMap(scores)

  // java map转scala map
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

可以通过模式匹配来进行获取元组中的值并赋予对应的变量：

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

### 2.2 Zip方法

```scala
object ScalaApp extends App {

   val array01 = Array("hadoop", "spark", "storm")
  val array02 = Array(10, 20, 30)
    
  // 1.zip方法得到的是多个tuple组成的数组
  val tuples: Array[(String, Int)] = array01.zip(array02)
  // 2.也可以在zip后调用toMap方法转换为映射
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

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7