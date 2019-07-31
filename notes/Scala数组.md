# Scala 数组相关操作

<nav>
<a href="#一定长数组">一、定长数组</a><br/>
<a href="#二变长数组">二、变长数组</a><br/>
<a href="#三数组遍历">三、数组遍历</a><br/>
<a href="#四数组转换">四、数组转换</a><br/>
<a href="#五多维数组">五、多维数组</a><br/>
<a href="#六与Java互操作">六、与Java互操作</a><br/>
</nav>

## 一、定长数组

在 Scala 中，如果你需要一个长度不变的数组，可以使用 Array。但需要注意以下两点：

- 在 Scala 中使用 `(index)` 而不是 `[index]` 来访问数组中的元素，因为访问元素，对于 Scala 来说是方法调用，`(index)` 相当于执行了 `.apply(index)` 方法。
- Scala 中的数组与 Java 中的是等价的，`Array[Int]()` 在虚拟机层面就等价于 Java 的 `int[]`。

```scala
// 10 个整数的数组，所有元素初始化为 0
scala> val nums=new Array[Int](10)
nums: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

// 10 个元素的字符串数组，所有元素初始化为 null
scala> val strings=new Array[String](10)
strings: Array[String] = Array(null, null, null, null, null, null, null, null, null, null)

// 使用指定值初始化，此时不需要 new 关键字
scala> val a=Array("hello","scala")
a: Array[String] = Array(hello, scala)

// 使用 () 来访问元素
scala> a(0)
res3: String = hello
```

## 二、变长数组

在 scala 中通过 ArrayBuffer 实现变长数组 (又称缓冲数组)。在构建 ArrayBuffer 时必须给出类型参数，但不必指定长度，因为 ArrayBuffer 会在需要的时候自动扩容和缩容。变长数组的构建方式及常用操作如下：

```java
import scala.collection.mutable.ArrayBuffer

object ScalaApp {
    
  // 相当于 Java 中的 main 方法
  def main(args: Array[String]): Unit = {
    // 1.声明变长数组 (缓冲数组)
    val ab = new ArrayBuffer[Int]()
    // 2.在末端增加元素
    ab += 1
    // 3.在末端添加多个元素
    ab += (2, 3, 4)
    // 4.可以使用 ++=追加任何集合
    ab ++= Array(5, 6, 7)
    // 5.缓冲数组可以直接打印查看
    println(ab)
    // 6.移除最后三个元素
    ab.trimEnd(3)
    // 7.在第 1 个元素之后插入多个新元素
    ab.insert(1, 8, 9)
    // 8.从第 2 个元素开始,移除 3 个元素,不指定第二个参数的话,默认值为 1
    ab.remove(2, 3)
    // 9.缓冲数组转定长数组
    val abToA = ab.toArray
    // 10. 定长数组打印为其 hashcode 值
    println(abToA)
    // 11. 定长数组转缓冲数组
    val aToAb = abToA.toBuffer
  }
}
```

需要注意的是：使用 `+= ` 在末尾插入元素是一个高效的操作，其时间复杂度是 O(1)。而使用 `insert` 随机插入元素的时间复杂度是 O(n)，因为在其插入位置之后的所有元素都要进行对应的后移，所以在 `ArrayBuffer` 中随机插入元素是一个低效的操作。

## 三、数组遍历

```scala
object ScalaApp extends App {

  val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // 1.方式一 相当于 Java 中的增强 for 循环
  for (elem <- a) {
    print(elem)
  }

  // 2.方式二
  for (index <- 0 until a.length) {
    print(a(index))
  }

  // 3.方式三, 是第二种方式的简写
  for (index <- a.indices) {
    print(a(index))
  }

  // 4.反向遍历
  for (index <- a.indices.reverse) {
    print(a(index))
  }

}
```

这里我们没有将代码写在 main 方法中，而是继承自 App.scala，这是 Scala 提供的一种简写方式，此时将代码写在类中，等价于写在 main 方法中，直接运行该类即可。



## 四、数组转换

数组转换是指由现有数组产生新的数组。假设当前拥有 a 数组，想把 a 中的偶数元素乘以 10 后产生一个新的数组，可以采用下面两种方式来实现：

```scala
object ScalaApp extends App {

  val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // 1.方式一 yield 关键字
  val ints1 = for (elem <- a if elem % 2 == 0) yield 10 * elem
  for (elem <- ints1) {
    println(elem)
  }

  // 2.方式二 采用函数式编程的方式,这和 Java 8 中的函数式编程是类似的，这里采用下划线标表示其中的每个元素
  val ints2 = a.filter(_ % 2 == 0).map(_ * 10)
  for (elem <- ints1) {
    println(elem)
  }
}
```



## 五、多维数组

和 Java 中一样，多维数组由单维数组组成。

```scala
object ScalaApp extends App {

  val matrix = Array(Array(11, 12, 13, 14, 15, 16, 17, 18, 19, 20),
    Array(21, 22, 23, 24, 25, 26, 27, 28, 29, 30),
    Array(31, 32, 33, 34, 35, 36, 37, 38, 39, 40))


  for (elem <- matrix) {

    for (elem <- elem) {
      print(elem + "-")
    }
    println()
  }

}

打印输出如下：
11-12-13-14-15-16-17-18-19-20-
21-22-23-24-25-26-27-28-29-30-
31-32-33-34-35-36-37-38-39-40-
```



## 六、与Java互操作

由于 Scala 的数组是使用 Java 的数组来实现的，所以两者之间可以相互转换。

```scala
import java.util

import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConverters, mutable}

object ScalaApp extends App {

  val element = ArrayBuffer("hadoop", "spark", "storm")
  // Scala 转 Java
  val javaList: util.List[String] = JavaConverters.bufferAsJavaList(element)
  // Java 转 Scala
  val scalaBuffer: mutable.Buffer[String] = JavaConverters.asScalaBuffer(javaList)
  for (elem <- scalaBuffer) {
    println(elem)
  }
}
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7
