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

Scala中的if/else语法结构与Java中的一样，唯一不同的是，Scala中的if表达式是有返回值的。

```scala
object ScalaApp extends App {

  val x = "scala"
  val result = if (x.length == 5) "true" else "false"
  print(result)
  
}
```

在Java中，每行语句都需要使用`;`表示结束，但是在Scala中并不需要。除非你在单行语句中写了多行代码。



## 二、块表达式

在Scala中，可以使用`{}`块包含一系列表达式，块中最后一个表达式的值就是块的值。

```scala
object ScalaApp extends App {

  val result = {
    val a = 1 + 1; val b = 2 + 2; a + b
  }
  print(result)
}

// 输出： 6
```

如果块中的最后一个表达式没有返回值，则块的返回值是Unit类型。

```scala
scala> val result ={ val a = 1 + 1; val b = 2 + 2 }
result: Unit = ()
```



## 三、循环表达式while

Scala和大多数语言一样，支持`while`和`do ... while`表达式。

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

for循环的基本使用如下：

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

除了基本使用外，还可以使用`yield`关键字从for循环中产生Vector，这称为for推导式。

```scala
scala> for (i <- 1 to 10) yield i * 6
res1: scala.collection.immutable.IndexedSeq[Int] = Vector(6, 12, 18, 24, 30, 36, 42, 48, 54, 60)
```



## 五、异常处理try

和Java中一样，支持`try...catch...finally`语句。

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

这里需要注意的是因为finally语句一定会被执行，所以不要在该语句中返回值，否则返回值会被作为整个try语句的返回值，如下：

```scala
scala> def g():Int = try return 1 finally  return  2
g: ()Int

// 方法g()总会返回2
scala> g()
res3: Int = 2
```



## 六、条件选择表达式match

match类似于java中的switch语句。

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

但是与Java中的switch有以下三点不同：

+ Scala中的case语句支持任何常量、字符串；而Java中case语句仅支持整型、枚举和字符串常量；
+ Scala中每个分支语句后面不需要写break，因为在case语句中break是隐含的，默认就有；
+ 在Scala中match语句是有返回值的，而Java中switch语句是没有返回值的。如下：

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

额外注意一下：Scala中并不支持Java中的break和continue关键字。



## 八、输入与输出

在Scala中可以使用print、println、printf打印输出，这与Java中是一样的。如果需要从控制台中获取输入，则可以使用`StdIn`中定义的各种方法。

```scala
val name = StdIn.readLine("Your name: ")
print("Your age: ")
val age = StdIn.readInt()
println(s"Hello, ${name}! Next year, you will be ${age + 1}.")
```



## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7