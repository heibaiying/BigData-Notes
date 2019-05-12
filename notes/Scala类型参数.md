# 类型参数

## 一、泛型

Scala支持类型参数化，使得我们能够编写泛型程序。

### 1.1 泛型类

Java中使用`<>`符号来定义类型参数，Scala中使用`[]`来定义类型参数。

```scala
class Pair[T, S](val first: T, val second: S) {
  override def toString: String = first + ":" + second
}
```

```scala
object ScalaApp extends App {

  // 使用时候你直接指定参数类型，也可以不指定，由程序自动推断
  val pair01 = new Pair("heibai01", 22)
  val pair02 = new Pair[String,Int]("heibai02", 33)

  println(pair01)
  println(pair02)
}
```

### 1.2 泛型方法

函数和方法也支持类型参数。

```scala
object Utils {
  def getHalf[T](a: Array[T]): Int = a.length / 2
}
```

## 二、类型限定

### 2.1 类型上界

对于对象之间进行大小比较，Scala和Java一样，都要求比较的对象需要实现`java.lang.Comparable`接口。

所以如果想对泛型进行比较，需要限定类型上界，语法为` S <: T`,代表S必须是类型T的子类或其本身。示例如下：

```scala
// 使用 <: 符号，限定T必须是Comparable[T]的子类型
class Pair[T <: Comparable[T]](val first: T, val second: T) {
  // 返回较小的值
  def smaller: T = if (first.compareTo(second) < 0) first else second
}
```

```scala
// 测试代码
val pair = new Pair("abc", "abcd")
println(pair.smaller) // 输出 abc
```

>注：如果你想要在Java中实现类型变量限定，需要使用关键字extends来实现，对于的Java代码如下：
>
>```java
>public class Pair<T extends Comparable<T>> {
>    private T first;
>    private T second;
>    Pair(T first, T second) {
>        this.first = first;
>        this.second = second;
>    }
>    public T smaller() {
>        return first.compareTo(second) < 0 ? first : second;
>    }
>}
>```

### 2.2 视图界定 & 类型约束

#### 1.视图界定

在上面的例子中，如果你使用Int类型或者Double等类型进行测试，点击运行后，你会发现程序根本无法通过编译：

```scala
val pair1 = new Pair(10, 12)
val pair2 = new Pair(10.0, 12.0)
```

之所以出现这样的问题，是因为在Scala中Int并没有实现Comparable，真正实现Comparable接口的是RichInt。在日常的编程中之所以你能够执行`3>2`这样的判断操作，是因为程序执行了隐式转换，将Int转换为RichInt。

![scala-richInt](D:\BigData-Notes\pictures\scala-richInt.png)

直接继承Java中Comparable接口的是特质Ordered，RichInt混入了该特质，Ordered源码如下:

```scala
// 除了compareTo方法外，还提供了额外的关系符方法
trait Ordered[A] extends Any with java.lang.Comparable[A] {
  def compare(that: A): Int
  def <  (that: A): Boolean = (this compare that) <  0
  def >  (that: A): Boolean = (this compare that) >  0
  def <= (that: A): Boolean = (this compare that) <= 0
  def >= (that: A): Boolean = (this compare that) >= 0
  def compareTo(that: A): Int = compare(that)
}
```

所以要想在泛型中解决这个问题，需要使用视图界定：

```scala
// 视图界定符号 <%
class Pair[T <% Comparable[T]](val first: T, val second: T) {
  // 返回较小的值
  def smaller: T = if (first.compareTo(second) < 0) first else second
}
```

> 注：由于直接继承Java中Comparable接口的是特质Ordered，所以也可以使用如下的视图界定：
>
> ```scala
> class Pair[T <% Ordered[T]](val first: T, val second: T) {
>     
> def smaller: T = if (first.compareTo(second) < 0) first else second
>     
> }
> ```

#### 2. 类型约束

如果你用的Scala是2.11+，则视图界定已经不推荐使用，官方推荐使用类型约束(type constraint)来实现同样的功能：

```scala
class Pair[T](val first: T, val second: T)(implicit ev: T => Comparable[T]) {
    
  def smaller: T = if (first.compareTo(second) < 0) first else second
    
}
```

### 2.3 上下文界定

上下文界定的形式为`T:M`,它要求必须存在一个类型为M[T]的隐式值，当你声明一个使用隐式值的方法时，需要添加一个隐式参数。上面的程序也可以使用上下文界定进行如下改写：

```scala
class Pair[T](val first: T, val second: T) {
    
  def smaller(implicit ord: Ordering[T]): T = if (ord.compare(first, second) < 0) first else second
    
}
```

### 2.4 类型下界



## 三、类型通配符