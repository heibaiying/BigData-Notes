# 隐式转换和隐式参数

<nav>
<a href="#一隐式转换">一、隐式转换</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-使用隐式转换">1.1 使用隐式转换</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-隐式转换规则">1.2 隐式转换规则</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-引入隐式转换">1.3 引入隐式转换</a><br/>
<a href="#二隐式参数">二、隐式参数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-使用隐式参数">2.1 使用隐式参数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-引入隐式参数">2.2 引入隐式参数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-利用隐式参数进行隐式转换">2.3 利用隐式参数进行隐式转换</a><br/>
</nav>


## 一、隐式转换

### 1.1 使用隐式转换

隐式转换指的是以 `implicit` 关键字声明带有单个参数的转换函数，它将值从一种类型转换为另一种类型，以便使用之前类型所没有的功能。示例如下：

```scala
// 普通人
class Person(val name: String)

// 雷神
class Thor(val name: String) {
  // 正常情况下只有雷神才能举起雷神之锤
  def hammer(): Unit = {
    println(name + "举起雷神之锤")
  }
}

object Thor extends App {
  // 定义隐式转换方法 将普通人转换为雷神 通常建议方法名使用 source2Target,即：被转换对象 To 转换对象
  implicit def person2Thor(p: Person): Thor = new Thor(p.name)
  // 这样普通人也能举起雷神之锤
  new Person("普通人").hammer()
}

输出： 普通人举起雷神之锤
```



### 1.2 隐式转换规则

并不是你使用 `implicit` 转换后，隐式转换就一定会发生，比如上面如果不调用 `hammer()` 方法的时候，普通人就还是普通人。通常程序会在以下情况下尝试执行隐式转换：

+ 当对象访问一个不存在的成员时，即调用的方法不存在或者访问的成员变量不存在；
+ 当对象调用某个方法，该方法存在，但是方法的声明参数与传入参数不匹配时。

而在以下三种情况下编译器不会尝试执行隐式转换：

+ 如果代码能够在不使用隐式转换的前提下通过编译，则不会使用隐式转换；
+ 编译器不会尝试同时执行多个转换，比如 `convert1(convert2(a))*b`；
+ 转换存在二义性，也不会发生转换。

这里首先解释一下二义性，上面的代码进行如下修改，由于两个隐式转换都是生效的，所以就存在了二义性：

```scala
//两个隐式转换都是有效的
implicit def person2Thor(p: Person): Thor = new Thor(p.name)
implicit def person2Thor2(p: Person): Thor = new Thor(p.name)
// 此时下面这段语句无法通过编译
new Person("普通人").hammer()
```

其次再解释一下多个转换的问题：

```scala
class ClassA {
  override def toString = "This is Class A"
}

class ClassB {
  override def toString = "This is Class B"
  def printB(b: ClassB): Unit = println(b)
}

class ClassC

class ClassD

object ImplicitTest extends App {
  implicit def A2B(a: ClassA): ClassB = {
    println("A2B")
    new ClassB
  }

  implicit def C2B(c: ClassC): ClassB = {
    println("C2B")
    new ClassB
  }

  implicit def D2C(d: ClassD): ClassC = {
    println("D2C")
    new ClassC
  }

  // 这行代码无法通过编译，因为要调用到 printB 方法，需要执行两次转换 C2B(D2C(ClassD))
  new ClassD().printB(new ClassA)
    
  /*
   *  下面的这一行代码虽然也进行了两次隐式转换，但是两次的转换对象并不是一个对象,所以它是生效的:
   *  转换流程如下:
   *  1. ClassC 中并没有 printB 方法,因此隐式转换为 ClassB,然后调用 printB 方法;
   *  2. 但是 printB 参数类型为 ClassB,然而传入的参数类型是 ClassA,所以需要将参数 ClassA 转换为 ClassB,这是第二次;
   *  即: C2B(ClassC) -> ClassB.printB(ClassA) -> ClassB.printB(A2B(ClassA)) -> ClassB.printB(ClassB)
   *  转换过程 1 的对象是 ClassC,而转换过程 2 的转换对象是 ClassA,所以虽然是一行代码两次转换，但是仍然是有效转换
   */
  new ClassC().printB(new ClassA)
}

// 输出：
C2B
A2B
This is Class B
```



### 1.3 引入隐式转换

隐式转换的可以定义在以下三个地方：

+ 定义在原类型的伴生对象中；
+ 直接定义在执行代码的上下文作用域中；
+ 统一定义在一个文件中，在使用时候导入。

上面我们使用的方法相当于直接定义在执行代码的作用域中，下面分别给出其他两种定义的代码示例：

**定义在原类型的伴生对象中**：

```scala
class Person(val name: String)
// 在伴生对象中定义隐式转换函数
object Person{
  implicit def person2Thor(p: Person): Thor = new Thor(p.name)
}
```

```scala
class Thor(val name: String) {
  def hammer(): Unit = {
    println(name + "举起雷神之锤")
  }
}
```

```scala
// 使用示例
object ScalaApp extends App {
  new Person("普通人").hammer()
}
```

**定义在一个公共的对象中**：

```scala
object Convert {
  implicit def person2Thor(p: Person): Thor = new Thor(p.name)
}
```

```scala
// 导入 Convert 下所有的隐式转换函数
import com.heibaiying.Convert._

object ScalaApp extends App {
  new Person("普通人").hammer()
}
```

> 注：Scala 自身的隐式转换函数大部分定义在 `Predef.scala` 中，你可以打开源文件查看，也可以在 Scala 交互式命令行中采用 `:implicit -v` 查看全部隐式转换函数。

<br/>

## 二、隐式参数

### 2.1 使用隐式参数

在定义函数或方法时可以使用标记为 `implicit` 的参数，这种情况下，编译器将会查找默认值，提供给函数调用。

```scala
// 定义分隔符类
class Delimiters(val left: String, val right: String)

object ScalaApp extends App {
  
    // 进行格式化输出
  def formatted(context: String)(implicit deli: Delimiters): Unit = {
    println(deli.left + context + deli.right)
  }
    
  // 定义一个隐式默认值 使用左右中括号作为分隔符
  implicit val bracket = new Delimiters("(", ")")
  formatted("this is context") // 输出: (this is context)
}
```

关于隐式参数，有两点需要注意：

1.我们上面定义 `formatted` 函数的时候使用了柯里化，如果你不使用柯里化表达式，按照通常习惯只有下面两种写法：

```scala
// 这种写法没有语法错误，但是无法通过编译
def formatted(implicit context: String, deli: Delimiters): Unit = {
  println(deli.left + context + deli.right)
} 
// 不存在这种写法，IDEA 直接会直接提示语法错误
def formatted( context: String,  implicit deli: Delimiters): Unit = {
  println(deli.left + context + deli.right)
} 
```

上面第一种写法编译的时候会出现下面所示 `error` 信息,从中也可以看出 `implicit` 是作用于参数列表中每个参数的，这显然不是我们想要到达的效果，所以上面的写法采用了柯里化。

```
not enough arguments for method formatted: 
(implicit context: String, implicit deli: com.heibaiying.Delimiters)
```

2.第二个问题和隐式函数一样，隐式默认值不能存在二义性，否则无法通过编译，示例如下：

```scala
implicit val bracket = new Delimiters("(", ")")
implicit val brace = new Delimiters("{", "}")
formatted("this is context")
```

上面代码无法通过编译，出现错误提示 `ambiguous implicit values`，即隐式值存在冲突。



### 2.2 引入隐式参数

引入隐式参数和引入隐式转换函数方法是一样的，有以下三种方式：

- 定义在隐式参数对应类的伴生对象中；
- 直接定义在执行代码的上下文作用域中；
- 统一定义在一个文件中，在使用时候导入。

我们上面示例程序相当于直接定义执行代码的上下文作用域中，下面给出其他两种方式的示例：

**定义在隐式参数对应类的伴生对象中**；

```scala
class Delimiters(val left: String, val right: String)

object Delimiters {
  implicit val bracket = new Delimiters("(", ")")
}
```

```scala
// 此时执行代码的上下文中不用定义
object ScalaApp extends App {

  def formatted(context: String)(implicit deli: Delimiters): Unit = {
    println(deli.left + context + deli.right)
  }
  formatted("this is context") 
}
```

**统一定义在一个文件中，在使用时候导入**：

```scala
object Convert {
  implicit val bracket = new Delimiters("(", ")")
}
```

```scala
// 在使用的时候导入
import com.heibaiying.Convert.bracket

object ScalaApp extends App {
  def formatted(context: String)(implicit deli: Delimiters): Unit = {
    println(deli.left + context + deli.right)
  }
  formatted("this is context") // 输出: (this is context)
}
```



### 2.3 利用隐式参数进行隐式转换

```scala
def smaller[T] (a: T, b: T) = if (a < b) a else b
```

在 Scala 中如果定义了一个如上所示的比较对象大小的泛型方法，你会发现无法通过编译。对于对象之间进行大小比较，Scala 和 Java 一样，都要求被比较的对象需要实现 java.lang.Comparable 接口。在 Scala 中，直接继承 Java 中 Comparable 接口的是特质 Ordered，它在继承 compareTo 方法的基础上，额外定义了关系符方法，源码如下:

```scala
trait Ordered[A] extends Any with java.lang.Comparable[A] {
  def compare(that: A): Int
  def <  (that: A): Boolean = (this compare that) <  0
  def >  (that: A): Boolean = (this compare that) >  0
  def <= (that: A): Boolean = (this compare that) <= 0
  def >= (that: A): Boolean = (this compare that) >= 0
  def compareTo(that: A): Int = compare(that)
}
```

所以要想在泛型中解决这个问题，有两种方法：

#### 1. 使用视图界定

```scala
object Pair extends App {

 // 视图界定
  def smaller[T<% Ordered[T]](a: T, b: T) = if (a < b) a else b
 
  println(smaller(1,2)) //输出 1
}
```

视图限定限制了 T 可以通过隐式转换 `Ordered[T]`，即对象一定可以进行大小比较。在上面的代码中 `smaller(1,2)` 中参数 `1` 和 `2` 实际上是通过定义在 `Predef` 中的隐式转换方法 `intWrapper` 转换为 `RichInt`。

```scala
// Predef.scala
@inline implicit def intWrapper(x: Int)   = new runtime.RichInt(x)
```

为什么要这么麻烦执行隐式转换，原因是 Scala 中的 Int 类型并不能直接进行比较，因为其没有实现 `Ordered` 特质，真正实现 `Ordered` 特质的是 `RichInt`。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/scala-richInt.png"/> </div>



#### 2. 利用隐式参数进行隐式转换

Scala2.11+ 后，视图界定被标识为废弃，官方推荐使用类型限定来解决上面的问题，本质上就是使用隐式参数进行隐式转换。

```scala
object Pair extends App {

   // order 既是一个隐式参数也是一个隐式转换，即如果 a 不存在 < 方法，则转换为 order(a)<b
  def smaller[T](a: T, b: T)(implicit order: T => Ordered[T]) = if (a < b) a else b

  println(smaller(1,2)) //输出 1
}
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7





<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>