# Scala基本数据类型和运算符

<nav>
<a href="#一数据类型">一、数据类型</a><br/>
<a href="#二字面量">二、字面量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-整数字面量">2.1 整数字面量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-字符串字面量">2.2 字符串字面量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-符号字面量">2.3 符号字面量</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-插值表达式">2.4 插值表达式</a><br/>
<a href="#三运算符">三、运算符</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-运算符即方法">3.1 运算符即方法</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-运算符优先级">3.2 运算符优先级</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-逻辑运算符">3.3 逻辑运算符</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#34-对象相等性">3.4 对象相等性</a><br/>
</nav>


## 一、数据类型

Scala 拥有下表所示的数据类型，其中Byte、Short、Int、Long和Char类型统称为整数类型，整数类型加上Float和Double统称为数值类型。Scala数值类型的取值范围和Java对应类型的取值范围相同。

| 数据类型 | 描述                                                         |
| -------- | ------------------------------------------------------------ |
| Byte     | 8位有符号补码整数。数值区间为 -128 到 127                    |
| Short    | 16位有符号补码整数。数值区间为 -32768 到 32767               |
| Int      | 32位有符号补码整数。数值区间为 -2147483648 到 2147483647     |
| Long     | 64位有符号补码整数。数值区间为 -9223372036854775808 到 9223372036854775807 |
| Float    | 32 位, IEEE 754 标准的单精度浮点数                           |
| Double   | 64 位 IEEE 754 标准的双精度浮点数                            |
| Char     | 16位无符号Unicode字符, 区间值为 U+0000 到 U+FFFF             |
| String   | 字符序列                                                     |
| Boolean  | true或false                                                  |
| Unit     | 表示无值，等同于Java中的void。用作不返回任何结果的方法的结果类型。Unit只有一个实例值，写成()。 |
| Null     | null 或空引用                                                |
| Nothing  | Nothing类型在Scala的类层级的最低端；它是任何其他类型的子类型。 |
| Any      | Any是所有其他类的超类                                        |
| AnyRef   | AnyRef类是Scala里所有引用类(reference class)的基类           |

## 二、字面量

Scala和Java字面量在使用上很多相似，比如都使用F或f表示浮点型，都使用L或l表示Long类型。下文主要介绍两者差异部分。

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

Scala支持10进制和16进制，但不支持八进制字面量和以0开头的整数字面量。

```scala
scala> 012
<console>:1: error: Decimal integer literals may not have a leading zero. (Octal syntax is obsolete.)
```

### 2.2 字符串字面量

#### 1. 字符字面量

字符字面量由一对单引号和中间的任意Unicode字符组成。你可以显式的给出原字符、也可以使用字符的Unicode码来表示，还可以包含特殊的转义字符。

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

Scala提供了`""" ... """`语法，通过三个双引号来表示原生字符串和多行字符串，使用该种方式，原生字符串中的特殊字符不会被转义。

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

符号字面量写法为： `'标识符` ，这里 标识符可以是任何字母或数字的组合。符号字面量会被映射成`scala.Symbol`的实例，如:符号字面量 `'x `会被编译器翻译为`scala.Symbol("x")`。符号可选方法很少，只能通过`.name`获取其名称。

需要注意的是：任意的同名symbols都指向同一个Symbol对象，而不同名的symbols一定指向不同的Symbol对象。

```scala
scala> val sym = 'ID008
sym: Symbol = 'ID008

scala> sym.name
res12: String = ID008
```

### 2.4 插值表达式

Scala支持插值表达式。

```scala
scala> val name="xiaoming"
name: String = xiaoming

scala> println(s"My name is $name,I'm ${2*9}.")
My name is xiaoming,I'm 18.
```

## 三、运算符

Scala和其他语言一样，支持大多数的操作运算符：

- 算术运算符（+，-，*，/，%）
- 关系运算符（==，!=，>，<，>=，<=）
- 逻辑运算符(&&，||，!，&，|)
- 位运算符(~，&，|，^，<<，>>，>>>)
- 赋值运算符(=，+=，-=，*=，/=，%=，<<=，>>=，&=，^=，|=)

以上操作符的基本使用与Java类似，下文主要介绍差异部分和注意事项。

### 3.1 运算符即方法

Scala的面向对象比Java更加纯粹，在Scala中一切都是对象。所以对于`1+2`,实际上是调用了Int类中名为`+`的方法，所以1+2,也可以写成`1.+(2)`。

```scala
scala> 1+2
res14: Int = 3

scala> 1.+(2)
res15: Int = 3
```

Int类中包含了多个重载的`+`方法，用于分别接收不同类型的参数。

<div align="center"> <img width='700px' src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-int+.png"/> </div>

### 3.2 运算符优先级

操作符的优先级如下：优先级由上至下，逐级递减。

<div align="center"> <img width='700px' src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-操作符优先级.png"/> </div>

在表格中某个字符的优先级越高，那么以这个字符打头的方法就拥有更高的优先级。如`+`的优先级大于`<`，也就意味则`+`的优先级大于以`<`开头的`<<`，所以`2<<2+2` , 实际上等价于`2<<(2+2)` :

```scala
scala> 2<<2+2
res0: Int = 32

scala> 2<<(2+2)
res1: Int = 32
```

### 3.3 逻辑运算符

和其他语言一样，在Scala中&&，||的执行是短路的，即如果左边的表达式能确定整个结果，右边的表达式就不会被执行，这满足大多数使用场景。但是如果你需要在无论什么情况下，都执行右边的表达式，则可以使用&或|代替。

### 3.4 对象相等性

如果想要判断两个对象是否相等，可以使用`==`和`!=`,这两个操作符可以用于所有的对象，包括null。

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

1. Martin Odersky(著)，高宇翔(译) . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1 