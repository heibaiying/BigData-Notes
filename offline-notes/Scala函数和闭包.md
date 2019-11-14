# 函数和闭包

<nav>
<a href="#一函数">一、函数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-函数与方法">1.1 函数与方法</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-函数类型">1.2 函数类型</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-一等公民匿名函数">1.3 一等公民&匿名函数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-特殊的函数表达式">1.4 特殊的函数表达式</a><br/>
<a href="#二闭包">二、闭包</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-闭包的定义">2.1 闭包的定义</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-修改自由变量">2.2 修改自由变量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-自由变量多副本">2.3 自由变量多副本</a><br/>
<a href="#三高阶函数">三、高阶函数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-使用函数作为参数">3.1 使用函数作为参数</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-函数柯里化">3.2 函数柯里化</a><br/>
</nav>


## 一、函数

### 1.1 函数与方法

Scala 中函数与方法的区别非常小，如果函数作为某个对象的成员，这样的函数被称为方法，否则就是一个正常的函数。

```scala
// 定义方法
def multi1(x:Int) = {x * x}
// 定义函数
val multi2 = (x: Int) => {x * x}

println(multi1(3)) //输出 9
println(multi2(3)) //输出 9
```

也可以使用 `def` 定义函数：

```scala
def multi3 = (x: Int) => {x * x}
println(multi3(3))  //输出 9 
```

`multi2` 和 `multi3` 本质上没有区别，这是因为函数是一等公民，`val multi2 = (x: Int) => {x * x}` 这个语句相当于是使用 `def` 预先定义了函数，之后赋值给变量 `multi2`。

### 1.2 函数类型

上面我们说过 `multi2` 和 `multi3` 本质上是一样的，那么作为函数它们是什么类型的？两者的类型实际上都是 `Int => Int`，前面一个 Int 代表输入参数类型，后面一个 Int 代表返回值类型。

```scala
scala> val multi2 = (x: Int) => {x * x}
multi2: Int => Int = $$Lambda$1092/594363215@1dd1a777

scala> def multi3 = (x: Int) => {x * x}
multi3: Int => Int

// 如果有多个参数，则类型为：（参数类型，参数类型 ...）=>返回值类型
scala> val multi4 = (x: Int,name: String) => {name + x * x }
multi4: (Int, String) => String = $$Lambda$1093/1039732747@2eb4fe7
```

### 1.3 一等公民&匿名函数

在 Scala 中函数是一等公民，这意味着不仅可以定义函数并调用它们，还可以将它们作为值进行传递：

```scala
import scala.math.ceil
object ScalaApp extends App {
  // 将函数 ceil 赋值给变量 fun,使用下划线 (_) 指明是 ceil 函数但不传递参数
  val fun = ceil _
  println(fun(2.3456))  //输出 3.0

}
```

在 Scala 中你不必给每一个函数都命名，如 `(x: Int) => 3 * x` 就是一个匿名函数：

```scala
object ScalaApp extends App {
  // 1.匿名函数
  (x: Int) => 3 * x
  // 2.具名函数
  val fun = (x: Int) => 3 * x
  // 3.直接使用匿名函数
  val array01 = Array(1, 2, 3).map((x: Int) => 3 * x)  
  // 4.使用占位符简写匿名函数
  val array02 = Array(1, 2, 3).map(_ * 3)
  // 5.使用具名函数
  val array03 = Array(1, 2, 3).map(fun)
  
}
```

### 1.4 特殊的函数表达式

#### 1. 可变长度参数列表

在 Java 中如果你想要传递可变长度的参数，需要使用 `String ...args` 这种形式，Scala 中等效的表达为 `args: String*`。

```scala
object ScalaApp extends App {
  def echo(args: String*): Unit = {
    for (arg <- args) println(arg)
  }
  echo("spark","hadoop","flink")
}
// 输出
spark
hadoop
flink
```

#### 2. 传递具名参数

向函数传递参数时候可以指定具体的参数名。

```scala
object ScalaApp extends App {
  
  def detail(name: String, age: Int): Unit = println(name + ":" + age)
  
  // 1.按照参数定义的顺序传入
  detail("heibaiying", 12)
  // 2.传递参数的时候指定具体的名称,则不必遵循定义的顺序
  detail(age = 12, name = "heibaiying")

}
```

#### 3. 默认值参数

在定义函数时，可以为参数指定默认值。

```scala
object ScalaApp extends App {

  def detail(name: String, age: Int = 88): Unit = println(name + ":" + age)

  // 如果没有传递 age 值,则使用默认值
  detail("heibaiying")
  detail("heibaiying", 12)

}
```

## 二、闭包

### 2.1 闭包的定义

```scala
var more = 10
// addMore 一个闭包函数:因为其捕获了自由变量 more 从而闭合了该函数字面量
val addMore = (x: Int) => x + more
```

如上函数 `addMore` 中有两个变量 x 和 more:

+ **x** : 是一个绑定变量 (bound variable)，因为其是该函数的入参，在函数的上下文中有明确的定义；
+ **more** : 是一个自由变量 (free variable)，因为函数字面量本生并没有给 more 赋予任何含义。

按照定义：在创建函数时，如果需要捕获自由变量，那么包含指向被捕获变量的引用的函数就被称为闭包函数。

### 2.2 修改自由变量

这里需要注意的是，闭包捕获的是变量本身，即是对变量本身的引用，这意味着：

+ 闭包外部对自由变量的修改，在闭包内部是可见的；
+ 闭包内部对自由变量的修改，在闭包外部也是可见的。

```scala
// 声明 more 变量
scala> var more = 10
more: Int = 10

// more 变量必须已经被声明，否则下面的语句会报错
scala> val addMore = (x: Int) => {x + more}
addMore: Int => Int = $$Lambda$1076/1844473121@876c4f0

scala> addMore(10)
res7: Int = 20

// 注意这里是给 more 变量赋值，而不是重新声明 more 变量
scala> more=1000
more: Int = 1000

scala> addMore(10)
res8: Int = 1010
```

### 2.3 自由变量多副本

自由变量可能随着程序的改变而改变，从而产生多个副本，但是闭包永远指向创建时候有效的那个变量副本。

```scala
// 第一次声明 more 变量
scala> var more = 10
more: Int = 10

// 创建闭包函数
scala> val addMore10 = (x: Int) => {x + more}
addMore10: Int => Int = $$Lambda$1077/1144251618@1bdaa13c

// 调用闭包函数
scala> addMore10(9)
res9: Int = 19

// 重新声明 more 变量
scala> var more = 100
more: Int = 100

// 创建新的闭包函数
scala> val addMore100 = (x: Int) => {x + more}
addMore100: Int => Int = $$Lambda$1078/626955849@4d0be2ac

// 引用的是重新声明 more 变量
scala> addMore100(9)
res10: Int = 109

// 引用的还是第一次声明的 more 变量
scala> addMore10(9)
res11: Int = 19

// 对于全局而言 more 还是 100
scala> more
res12: Int = 100
```

从上面的示例可以看出重新声明 `more` 后，全局的 `more` 的值是 100，但是对于闭包函数 `addMore10` 还是引用的是值为 10 的 `more`，这是由虚拟机来实现的，虚拟机会保证 `more` 变量在重新声明后，原来的被捕获的变量副本继续在堆上保持存活。

## 三、高阶函数

### 3.1 使用函数作为参数

定义函数时候支持传入函数作为参数，此时新定义的函数被称为高阶函数。

```scala
object ScalaApp extends App {

  // 1.定义函数
  def square = (x: Int) => {
    x * x
  }

  // 2.定义高阶函数: 第一个参数是类型为 Int => Int 的函数
  def multi(fun: Int => Int, x: Int) = {
    fun(x) * 100
  }

  // 3.传入具名函数
  println(multi(square, 5)) // 输出 2500
    
  // 4.传入匿名函数
  println(multi(_ * 100, 5)) // 输出 50000

}
```

### 3.2 函数柯里化

我们上面定义的函数都只支持一个参数列表，而柯里化函数则支持多个参数列表。柯里化指的是将原来接受两个参数的函数变成接受一个参数的函数的过程。新的函数以原有第二个参数作为参数。

```scala
object ScalaApp extends App {
  // 定义柯里化函数
  def curriedSum(x: Int)(y: Int) = x + y
  println(curriedSum(2)(3)) //输出 5
}
```

这里当你调用 curriedSum 时候，实际上是连着做了两次传统的函数调用，实际执行的柯里化过程如下：

+ 第一次调用接收一个名为 `x` 的 Int 型参数，返回一个用于第二次调用的函数，假设 `x` 为 2，则返回函数 `2+y`；
+ 返回的函数接收参数 `y`，并计算并返回值 `2+3` 的值。

想要获得柯里化的中间返回的函数其实也比较简单：

```scala
object ScalaApp extends App {
  // 定义柯里化函数
  def curriedSum(x: Int)(y: Int) = x + y
  println(curriedSum(2)(3)) //输出 5

  // 获取传入值为 10 返回的中间函数 10 + y
  val plus: Int => Int = curriedSum(10)_
  println(plus(3)) //输出值 13
}
```

柯里化支持多个参数列表，多个参数按照从左到右的顺序依次执行柯里化操作：

```scala
object ScalaApp extends App {
  // 定义柯里化函数
  def curriedSum(x: Int)(y: Int)(z: String) = x + y + z
  println(curriedSum(2)(3)("name")) // 输出 5name
  
}
```





## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7







