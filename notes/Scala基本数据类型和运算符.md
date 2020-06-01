# Scala基本数据类型和运算符

<nav>
<a href="#一数据类型">一、数据类型</a><br/>
<a href="#二字面量">二、字面量</a><br/>
<a href="#三运算符">三、运算符</a><br/>
</nav>

## 一、数据类型

### 1.1 类型支持

Scala 拥有下表所示的数据类型，其中 Byte、Short、Int、Long 和 Char 类型统称为整数类型，整数类型加上 Float 和 Double 统称为数值类型。Scala 数值类型的取值范围和 Java 对应类型的取值范围相同。

| 数据类型 | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| Byte     | 8 位有符号补码整数。数值区间为 -128 到 127                    |
| Short    | 16 位有符号补码整数。数值区间为 -32768 到 32767               |
| Int      | 32 位有符号补码整数。数值区间为 -2147483648 到 2147483647     |
| Long     | 64 位有符号补码整数。数值区间为 -9223372036854775808 到 9223372036854775807 |
| Float    | 32 位, IEEE 754 标准的单精度浮点数                           |
| Double   | 64 位 IEEE 754 标准的双精度浮点数                            |
| Char     | 16 位无符号 Unicode 字符, 区间值为 U+0000 到 U+FFFF             |
| String   | 字符序列                                                     |
| Boolean  | true 或 false                                                  |
| Unit     | 表示无值，等同于 Java 中的 void。用作不返回任何结果的方法的结果类型。Unit 只有一个实例值，写成 ()。 |
| Null     | null 或空引用                                                |
| Nothing  | Nothing 类型在 Scala 的类层级的最低端；它是任何其他类型的子类型。 |
| Any      | Any 是所有其他类的超类                                        |
| AnyRef   | AnyRef 类是 Scala 里所有引用类 (reference class) 的基类           |

### 1.2 定义变量

Scala 的变量分为两种，val 和 var，其区别如下：

+ **val** ： 类似于 Java 中的 final 变量，一旦初始化就不能被重新赋值；
+ **var** ：类似于 Java 中的非 final 变量，在整个声明周期内 var 可以被重新赋值；

```scala
scala> val a=1
a: Int = 1

scala> a=2
<console>:8: error: reassignment to val // 不允许重新赋值

scala> var b=1
b: Int = 1

scala> b=2
b: Int = 2
```

### 1.3 类型推断

在上面的演示中，并没有声明 a 是 Int 类型，但是程序还是把 a 当做 Int 类型，这就是 Scala 的类型推断。在大多数情况下，你都无需指明变量的类型，程序会自动进行推断。如果你想显式的声明类型，可以在变量后面指定，如下：

```scala
scala>  val c:String="hello scala"
c: String = hello scala
```

### 1.4 Scala解释器

在 scala 命令行中，如果没有对输入的值指定赋值的变量，则输入的值默认会赋值给 `resX`(其中 X 是一个从 0 开始递增的整数)，`res` 是 result 的缩写，这个变量可以在后面的语句中进行引用。

```scala
scala> 5
res0: Int = 5

scala> res0*6
res1: Int = 30

scala> println(res1)
30
```



## 二、字面量

Scala 和 Java 字面量在使用上很多相似，比如都使用 F 或 f 表示浮点型，都使用 L 或 l 表示 Long 类型。下文主要介绍两者差异部分。

```scala
scala> 1.2
res0: Double = 1.2

scala> 1.2f
res1: Float = 1.2

scala> 1.4F
res2: Float = 1.4

scala> 1
res3: Int = 1

scala> 1l
res4: Long = 1

scala> 1L
res5: Long = 1
```

### 2.1 整数字面量

Scala 支持 10 进制和 16 进制，但不支持八进制字面量和以 0 开头的整数字面量。

```scala
scala> 012
<console>:1: error: Decimal integer literals may not have a leading zero. (Octal syntax is obsolete.)
```

### 2.2 字符串字面量

#### 1. 字符字面量

字符字面量由一对单引号和中间的任意 Unicode 字符组成。你可以显式的给出原字符、也可以使用字符的 Unicode 码来表示，还可以包含特殊的转义字符。

```scala
scala> '\u0041'
res0: Char = A

scala> 'a'
res1: Char = a

scala> '\n'
res2: Char =
```

#### 2. 字符串字面量

字符串字面量由双引号包起来的字符组成。

```scala
scala> "hello world"
res3: String = hello world
```

#### 3.原生字符串

Scala 提供了 `""" ... """` 语法，通过三个双引号来表示原生字符串和多行字符串，使用该种方式，原生字符串中的特殊字符不会被转义。

```scala
scala> "hello \tool"
res4: String = hello    ool

scala> """hello \tool"""
res5: String = hello \tool

scala> """hello
     | world"""
res6: String =
hello
world
```

### 2.3 符号字面量

符号字面量写法为： `'标识符 ` ，这里 标识符可以是任何字母或数字的组合。符号字面量会被映射成 `scala.Symbol` 的实例，如:符号字面量 `'x ` 会被编译器翻译为 `scala.Symbol("x")`。符号字面量可选方法很少，只能通过 `.name` 获取其名称。

注意：具有相同 `name` 的符号字面量一定指向同一个 Symbol 对象，不同 `name` 的符号字面量一定指向不同的 Symbol 对象。

```scala
scala> val sym = 'ID008
sym: Symbol = 'ID008

scala> sym.name
res12: String = ID008
```

### 2.4 插值表达式

Scala 支持插值表达式。

```scala
scala> val name="xiaoming"
name: String = xiaoming

scala> println(s"My name is $name,I'm ${2*9}.")
My name is xiaoming,I'm 18.
```

## 三、运算符

Scala 和其他语言一样，支持大多数的操作运算符：

- 算术运算符（+，-，*，/，%）
- 关系运算符（==，!=，>，<，>=，<=）
- 逻辑运算符 (&&，||，!，&，|)
- 位运算符 (~，&，|，^，<<，>>，>>>)
- 赋值运算符 (=，+=，-=，*=，/=，%=，<<=，>>=，&=，^=，|=)

以上操作符的基本使用与 Java 类似，下文主要介绍差异部分和注意事项。

### 3.1 运算符即方法

Scala 的面向对象比 Java 更加纯粹，在 Scala 中一切都是对象。所以对于 `1+2`,实际上是调用了 Int 类中名为 `+` 的方法，所以 1+2,也可以写成 `1.+(2)`。

```scala
scala> 1+2
res14: Int = 3

scala> 1.+(2)
res15: Int = 3
```

Int 类中包含了多个重载的 `+` 方法，用于分别接收不同类型的参数。

<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/scala-int+.png"/> </div>

### 3.2 逻辑运算符

和其他语言一样，在 Scala 中 `&&`，`||` 的执行是短路的，即如果左边的表达式能确定整个结果，右边的表达式就不会被执行，这满足大多数使用场景。但是如果你需要在无论什么情况下，都执行右边的表达式，则可以使用 `&` 或 `|` 代替。

### 3.3 赋值运算符

在 Scala 中没有 Java 中的 `++` 和 `--` 运算符，如果你想要实现类似的操作，只能使用 `+=1`，或者 `-=1`。

```scala
scala> var a=1
a: Int = 1

scala> a+=1

scala> a
res8: Int = 2

scala> a-=1

scala> a
res10: Int = 1
```

### 3.4 运算符优先级

操作符的优先级如下：优先级由上至下，逐级递减。

<div align="center"> <img src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/scala-操作符优先级.png"/> </div>

在表格中某个字符的优先级越高，那么以这个字符打头的方法就拥有更高的优先级。如 `+` 的优先级大于 `<`，也就意味则 `+` 的优先级大于以 `<` 开头的 `<<`，所以 `2<<2+2` , 实际上等价于 `2<<(2+2)` :

```scala
scala> 2<<2+2
res0: Int = 32

scala> 2<<(2+2)
res1: Int = 32
```

### 3.5 对象相等性

如果想要判断两个对象是否相等，可以使用 `==` 和 `!=`,这两个操作符可以用于所有的对象，包括 null。

```scala
scala> 1==2
res2: Boolean = false

scala> List(1,2,3)==List(1,2,3)
res3: Boolean = true

scala> 1==1.0
res4: Boolean = true

scala> List(1,2,3)==null
res5: Boolean = false

scala> null==null
res6: Boolean = true
```



## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1 


<div align="center"> <img  src="https://gitee.com/heibaiying/BigData-Notes/raw/master/pictures/weixin-desc.png"/> </div>