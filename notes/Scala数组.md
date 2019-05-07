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

在Scala中，如果你需要一个长度不变的数组，可以使用Array。同时需要注意以下两点：

- 在scala中使用`(index)`而不是`[index]`来访问数组中的元素，因为访问元素，对于Scala来说是方法调用，`(index)`相当于执行了`.apply(index)`方法。
- scala中的数组与Java中的是等价的，`Array[Int]()`在虚拟机层面就等价于Java的`int[]`。

```scala
// 10个整数的数组，所有元素初始化为0
scala> val nums=new Array[Int](10)
nums: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

// 10个元素的字符串数组，所有元素初始化为null
scala> val strings=new Array[String](10)
strings: Array[String] = Array(null, null, null, null, null, null, null, null, null, null)

// 使用指定值初始化，此时不需要new关键字
scala> val a=Array("hello","scala")
a: Array[String] = Array(hello, scala)

// 使用()来访问元素
scala> a(0)
res3: String = hello
```

## 二、变长数组

在scala中通过ArrayBuffer实现变长数组(又称缓冲数组)。在构建ArrayBuffer时必须给出类型参数，但不必指定长度，因为ArrayBuffer会在需要的时候自动扩容和缩容。变长数组的构建方式及常用操作如下：

```java
import scala.collection.mutable.ArrayBuffer

object ScalaApp {
    
  // 相当于Java中的main方法
  def main(args: Array[String]): Unit = {
    // 1.声明变长数组(缓冲数组)
    val ab = new ArrayBuffer[Int]()
    // 2.在末端增加元素
    ab += 1
    // 3.在末端添加多个元素
    ab += (2, 3, 4)
    // 4.可以使用++=追加任何集合
    ab ++= Array(5, 6, 7)
    // 5.缓冲数组可以直接打印查看
    println(ab)
    // 6.移除最后三个元素
    ab.trimEnd(3)
    // 7.在第1个元素之后插入多个新元素
    ab.insert(1, 8, 9)
    // 8.从第2个元素开始,移除3个元素,不指定第二个参数的话,默认值为1
    ab.remove(2, 3)
    // 9.缓冲数组转定长数组
    val abToA = ab.toArray
    // 10. 定长数组打印为其hashcode值
    println(abToA)
    // 11. 定长数组转缓冲数组
    val aToAb = abToA.toBuffer
  }
}
```

这里需要说明的是：使用`+= `在末尾插入元素是一个高效的操作，其时间复杂度是O(1)。而使用insert随机插入元素的时间复杂度是O(n)，因为在其插入位置之后的所有元素都要进行对应地后移，所以在`ArrayBuffer`中随机插入元素是一个低效的操作。

## 三、数组遍历

```scala
object ScalaApp extends App {

  val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // 1.方式一 相当于Java中的增强for循环
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

这里我们没有将代码写在main方法中，而是继承自App.scala，这是Scala提供的一种简写方式，此时将代码写在类中，等价于写在main方法中，直接运行该类即可。



## 四、数组转换

数组转换是指由现有数组产生新的数组。假设当前拥有a数组，想把a中的偶数元素乘以10后产生一个新的数组，可以采用下面两种方式来实现：

```scala
object ScalaApp extends App {

  val a = Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)

  // 1.方式一 yield关键字
  val ints1 = for (elem <- a if elem % 2 == 0) yield 10 * elem
  for (elem <- ints1) {
    println(elem)
  }

  // 2.方式二 采用函数式编程的方式,这和Java 8中的函数式编程是类似的，这里采用下划线标表示其中的每个元素
  val ints2 = a.filter(_ % 2 == 0).map(_ * 10)
  for (elem <- ints1) {
    println(elem)
  }
}
```



## 五、多维数组

和Java中一样，多维数组由单维数组组成。

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

由于Scala的数组是使用Java的数组来实现的，所以两者之间可以相互转换。

```scala
import java.util

import scala.collection.mutable.ArrayBuffer
import scala.collection.{JavaConverters, mutable}

object ScalaApp extends App {

  val element = ArrayBuffer("hadoop", "spark", "storm")
  // Scala转Java
  val javaList: util.List[String] = JavaConverters.bufferAsJavaList(element)
  // Java转Scala
  val scalaBuffer: mutable.Buffer[String] = JavaConverters.asScalaBuffer(javaList)
  for (elem <- scalaBuffer) {
    println(elem)
  }
}
```



## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7