# 流程控制语句

<nav>
<a href="#一条件表达式if">一、条件表达式if</a><br/>
<a href="#二块表达式">二、块表达式</a><br/>
<a href="#三循环表达式while">三、循环表达式while</a><br/>
<a href="#四循环表达式for">四、循环表达式for</a><br/>
<a href="#五异常处理try">五、异常处理try</a><br/>
<a href="#六条件选择表达式match">六、条件选择表达式match</a><br/>
<a href="#七没有break和continue">七、没有break和continue</a><br/>
<a href="#八输入与输出">八、输入与输出</a><br/>
</nav>

## 一、条件表达式if

Scala 中的 if/else 语法结构与 Java 中的一样，唯一不同的是，Scala 中的 if 表达式是有返回值的。

```scala
object ScalaApp extends App {

  val x = "scala"
  val result = if (x.length == 5) "true" else "false"
  print(result)
  
}
```

在 Java 中，每行语句都需要使用 `;` 表示结束，但是在 Scala 中并不需要。除非你在单行语句中写了多行代码。



## 二、块表达式

在 Scala 中，可以使用 `{}` 块包含一系列表达式，块中最后一个表达式的值就是块的值。

```scala
object ScalaApp extends App {

  val result = {
    val a = 1 + 1; val b = 2 + 2; a + b
  }
  print(result)
}

// 输出： 6
```

如果块中的最后一个表达式没有返回值，则块的返回值是 Unit 类型。

```scala
scala> val result ={ val a = 1 + 1; val b = 2 + 2 }
result: Unit = ()
```



## 三、循环表达式while

Scala 和大多数语言一样，支持 `while` 和 `do ... while` 表达式。

```scala
object ScalaApp extends App {

  var n = 0

  while (n < 10) {
    n += 1
    println(n)
  }

  // 循环至少要执行一次
  do {
    println(n)
  } while (n > 10)
}
```



## 四、循环表达式for

for 循环的基本使用如下：

```scala
object ScalaApp extends App {

  // 1.基本使用  输出[1,9)
  for (n <- 1 until 10) {print(n)}

  // 2.使用多个表达式生成器  输出: 11 12 13 21 22 23 31 32 33
  for (i <- 1 to 3; j <- 1 to 3) print(f"${10 * i + j}%3d")

  // 3.使用带条件的表达式生成器  输出: 12 13 21 23 31 32
  for (i <- 1 to 3; j <- 1 to 3 if i != j) print(f"${10 * i + j}%3d")

}
```

除了基本使用外，还可以使用 `yield` 关键字从 for 循环中产生 Vector，这称为 for 推导式。

```scala
scala> for (i <- 1 to 10) yield i * 6
res1: scala.collection.immutable.IndexedSeq[Int] = Vector(6, 12, 18, 24, 30, 36, 42, 48, 54, 60)
```



## 五、异常处理try

和 Java 中一样，支持 `try...catch...finally` 语句。

```scala
import java.io.{FileNotFoundException, FileReader}

object ScalaApp extends App {

  try {
    val reader = new FileReader("wordCount.txt")
  } catch {
    case ex: FileNotFoundException =>
      ex.printStackTrace()
      println("没有找到对应的文件!")
  } finally {
    println("finally 语句一定会被执行！")
  }
}
```

这里需要注意的是因为 finally 语句一定会被执行，所以不要在该语句中返回值，否则返回值会被作为整个 try 语句的返回值，如下：

```scala
scala> def g():Int = try return 1 finally  return  2
g: ()Int

// 方法 g() 总会返回 2
scala> g()
res3: Int = 2
```



## 六、条件选择表达式match

match 类似于 java 中的 switch 语句。

```scala
object ScalaApp extends App {

  val elements = Array("A", "B", "C", "D", "E")

  for (elem <- elements) {
    elem match {
      case "A" => println(10)
      case "B" => println(20)
      case "C" => println(30)
      case _ => println(50)
    }
  }
}

```

但是与 Java 中的 switch 有以下三点不同：

+ Scala 中的 case 语句支持任何类型；而 Java 中 case 语句仅支持整型、枚举和字符串常量；
+ Scala 中每个分支语句后面不需要写 break，因为在 case 语句中 break 是隐含的，默认就有；
+ 在 Scala 中 match 语句是有返回值的，而 Java 中 switch 语句是没有返回值的。如下：

```scala
object ScalaApp extends App {

  val elements = Array("A", "B", "C", "D", "E")

  for (elem <- elements) {
    val score = elem match {
      case "A" => 10
      case "B" => 20
      case "C" => 30
      case _ => 50
    }
    print(elem + ":" + score + ";")
  }
}
// 输出： A:10;B:20;C:30;D:50;E:50;
```



## 七、没有break和continue

额外注意一下：Scala 中并不支持 Java 中的 break 和 continue 关键字。



## 八、输入与输出

在 Scala 中可以使用 print、println、printf 打印输出，这与 Java 中是一样的。如果需要从控制台中获取输入，则可以使用 `StdIn` 中定义的各种方法。

```scala
val name = StdIn.readLine("Your name: ")
print("Your age: ")
val age = StdIn.readInt()
println(s"Hello, ${name}! Next year, you will be ${age + 1}.")
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>