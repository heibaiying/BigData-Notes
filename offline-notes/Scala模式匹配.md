# Scala模式匹配

<nav>
<a href="#一模式匹配">一、模式匹配</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-更好的swith">1.1 更好的swith</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-用作类型检查">1.2 用作类型检查</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#13-匹配数据结构">1.3 匹配数据结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#14-提取器">1.4 提取器</a><br/>
<a href="#二样例类">二、样例类</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-样例类">2.1 样例类</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-用于模式匹配">2.3 用于模式匹配</a><br/>
</nav>

## 一、模式匹配

Scala 支持模式匹配机制，可以代替 swith 语句、执行类型检查、以及支持析构表达式等。

### 1.1 更好的swith

Scala 不支持 swith，可以使用模式匹配 `match...case` 语法代替。但是 match 语句与 Java 中的 switch 有以下三点不同：

- Scala 中的 case 语句支持任何类型；而 Java 中 case 语句仅支持整型、枚举和字符串常量；
- Scala 中每个分支语句后面不需要写 break，因为在 case 语句中 break 是隐含的，默认就有；
- 在 Scala 中 match 语句是有返回值的，而 Java 中 switch 语句是没有返回值的。如下：

```scala
object ScalaApp extends App {

  def matchTest(x: Int) = x match {
    case 1 => "one"
    case 2 => "two"
    case _ if x > 9 && x < 100 => "两位数"   //支持条件表达式 这被称为模式守卫
    case _ => "other"
  }

  println(matchTest(1))   //输出 one
  println(matchTest(10))  //输出 两位数
  println(matchTest(200)) //输出 other
}
```

### 1.2 用作类型检查

```scala
object ScalaApp extends App {

  def matchTest[T](x: T) = x match {
    case x: Int => "数值型"
    case x: String => "字符型"
    case x: Float => "浮点型"
    case _ => "other"
  }

  println(matchTest(1))     //输出 数值型
  println(matchTest(10.3f)) //输出 浮点型
  println(matchTest("str")) //输出 字符型
  println(matchTest(2.1))   //输出 other
}
```

### 1.3 匹配数据结构

匹配元组示例：

```scala
object ScalaApp extends App {

  def matchTest(x: Any) = x match {
    case (0, _, _) => "匹配第一个元素为 0 的元组"
    case (a, b, c) => println(a + "~" + b + "~" + c)
    case _ => "other"
  }

  println(matchTest((0, 1, 2)))             // 输出: 匹配第一个元素为 0 的元组
  matchTest((1, 2, 3))                      // 输出: 1~2~3
  println(matchTest(Array(10, 11, 12, 14))) // 输出: other
}
```

匹配数组示例：

```scala
object ScalaApp extends App {

  def matchTest[T](x: Array[T]) = x match {
    case Array(0) => "匹配只有一个元素 0 的数组"
    case Array(a, b) => println(a + "~" + b)
    case Array(10, _*) => "第一个元素为 10 的数组"
    case _ => "other"
  }

  println(matchTest(Array(0)))          // 输出: 匹配只有一个元素 0 的数组
  matchTest(Array(1, 2))                // 输出: 1~2
  println(matchTest(Array(10, 11, 12))) // 输出: 第一个元素为 10 的数组
  println(matchTest(Array(3, 2, 1)))    // 输出: other
}
```

### 1.4 提取器

数组、列表和元组能使用模式匹配，都是依靠提取器 (extractor) 机制，它们伴生对象中定义了 `unapply` 或 `unapplySeq` 方法：

+ **unapply**：用于提取固定数量的对象；
+ **unapplySeq**：用于提取一个序列；

这里以数组为例，`Array.scala` 定义了 `unapplySeq` 方法：

```scala
def unapplySeq[T](x : scala.Array[T]) : scala.Option[scala.IndexedSeq[T]] = { /* compiled code */ }
```

`unapplySeq` 返回一个序列，包含数组中的所有值，这样在模式匹配时，才能知道对应位置上的值。



## 二、样例类

### 2.1 样例类

样例类是一种的特殊的类，它们被经过优化以用于模式匹配，样例类的声明比较简单，只需要在 `class` 前面加上关键字 `case`。下面给出一个样例类及其用于模式匹配的示例：

```scala
//声明一个抽象类
abstract class Person{}
```

```scala
// 样例类 Employee
case class Employee(name: String, age: Int, salary: Double) extends Person {}
```

```scala
// 样例类 Student
case class Student(name: String, age: Int) extends Person {}
```

当你声明样例类后，编译器自动进行以下配置：

- 构造器中每个参数都默认为 `val`；
- 自动地生成 `equals, hashCode, toString, copy` 等方法；
- 伴生对象中自动生成 `apply` 方法，使得可以不用 new 关键字就能构造出相应的对象；
- 伴生对象中自动生成 `unapply` 方法，以支持模式匹配。

除了上面的特征外，样例类和其他类相同，可以任意添加方法和字段，扩展它们。

### 2.3 用于模式匹配

样例的伴生对象中自动生成 `unapply` 方法，所以样例类可以支持模式匹配，使用如下：

```scala
object ScalaApp extends App {

  def matchTest(person: Person) = person match {
    case Student(name, _) => "student:" + name
    case Employee(_, _, salary) => "employee salary:" + salary
    case _ => "other"
  }

  println(matchTest(Student("heibai", 12)))        //输出: student:heibai
  println(matchTest(Employee("ying", 22, 999999))) //输出: employee salary:999999.0
}
```





## 参考资料

1. Martin Odersky . Scala 编程 (第 3 版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学 Scala(第 2 版)[M] . 电子工业出版社 . 2017-7

