# 类型参数
<nav>
<a href="#一泛型">一、泛型</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#11-泛型类">1.1 泛型类</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#12-泛型方法">1.2 泛型方法</a><br/>
<a href="#二类型限定">二、类型限定</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-类型上界限定">2.1 类型上界限定</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-视图界定">2.2 视图界定 </a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-类型约束">2.3 类型约束</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-上下文界定">2.4 上下文界定</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-类型下界限定">2.5 类型下界限定</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-多重界定">2.6 多重界定</a><br/>
<a href="#三Ordering--Ordered">三、Ordering & Ordered</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-Comparable">3.1 Comparable</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-Comparator">3.2 Comparator</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-上下文界定的优点">3.3 上下文界定的优点</a><br/>
</nav>

## 一、泛型

Scala支持类型参数化，使得我们能够编写泛型程序。

### 1.1 泛型类

Java中使用`<>`符号来包含定义的类型参数，Scala则使用`[]`。

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

### 2.1 类型上界限定

Scala和Java一样，对于对象之间进行大小比较，要求被比较的对象实现`java.lang.Comparable`接口。

所以如果想对泛型进行比较，需要限定类型上界为`java.lang.Comparable`，语法为` S <: T`,代表类型S是类型T的子类或其本身。示例如下：

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

>扩展：如果你想要在Java中实现类型变量限定，需要使用关键字extends来实现，等价的Java代码如下：
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
>     }
>}
>```

### 2.2 视图界定 

在上面的例子中，如果你使用Int类型或者Double等类型进行测试，点击运行后，你会发现程序根本无法通过编译：

```scala
val pair1 = new Pair(10, 12)
val pair2 = new Pair(10.0, 12.0)
```

之所以出现这样的问题，是因为Scala中的Int类并没有实现Comparable接口。在Scala中直接继承Comparable接口的是特质Ordered，它在继承compareTo方法的基础上，额外定义了关系符方法，源码如下:

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

之所以在日常的编程中之所以你能够执行`3>2`这样的判断操作，是因为程序执行了定义在`Predef`中的隐式转换方法`intWrapper(x: Int) `，将Int类型转换为RichInt类型，而RichInt间接混入了Ordered特质，所以能够进行比较。

```scala
// Predef.scala
@inline implicit def intWrapper(x: Int)   = new runtime.RichInt(x)
```

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-richInt.png"/> </div>

要想解决传入数值无法进行比较的问题，可以使用视图界定。语法为`T <% U`,代表T能够通过隐式转换转为U，即允许Int型参数在无法进行比较的时候转换为RichInt类型。示例如下：

```scala
// 视图界定符号 <%
class Pair[T <% Comparable[T]](val first: T, val second: T) {
  // 返回较小的值
  def smaller: T = if (first.compareTo(second) < 0) first else second
}
```

> 注：由于直接继承Java中Comparable接口的是特质Ordered，所以如下的视图界定和上面是等效的：
>
> ```scala
> // 隐式转换为Ordered[T]
>    class Pair[T <% Ordered[T]](val first: T, val second: T) {
> def smaller: T = if (first.compareTo(second) < 0) first else second
>    }
> ```

### 2.3 类型约束

如果你用的Scala是2.11+，会发现IDEA会提示视图界定已被标识为废弃。官方推荐使用类型约束(type constraint)来实现同样的功能，其本质是使用隐式参数进行隐式转换，示例如下：

```scala
 // 1.使用隐式参数隐式转换为Comparable[T]
class Pair[T](val first: T, val second: T)(implicit ev: T => Comparable[T]) 
  def smaller: T = if (first.compareTo(second) < 0) first else second
}

// 2.由于直接继承Java中Comparable接口的是特质Ordered，所以也可以隐式转换为Ordered[T]
class Pair[T](val first: T, val second: T)(implicit ev: T => Ordered[T]) {
  def smaller: T = if (first.compareTo(second) < 0) first else second
}
```

当然，隐式参数转换也可以运用在具体的方法上：

```scala
object PairUtils{
  def smaller[T](a: T, b: T)(implicit order: T => Ordered[T]) = if (a < b) a else b
}
```

### 2.4 上下文界定

上下文界定的形式为`T:M`，其中M是一个泛型，它要求必须存在一个类型为M[T]的隐式值，当你声明一个带隐式参数的方法时，需要定义一个隐式默认值。所以上面的程序也可以使用上下文界定进行改写：

```scala
class Pair[T](val first: T, val second: T) {
  // 请注意 这个地方用的是Ordering[T]，而上面视图界定和类型约束，用的是Ordered[T]，两者的区别会在后文给出解释
  def smaller(implicit ord: Ordering[T]): T = if (ord.compare(first, second) < 0) first else second 
}

// 测试
val pair= new Pair(88, 66)
println(pair.smaller)  //输出：66
```

在上面的示例中，我们无需手动添加隐式默认值就可以完成转换，这是因为Scala自动引入了Ordering[Int]这个隐式值。为了更好的说明上下文界定，下面给出一个自定义类型的比较示例：

```scala
// 1.定义一个人员类
class Person(val name: String, val age: Int) {
  override def toString: String = name + ":" + age
}

// 2.继承Ordering[T],实现自定义比较器,按照自己的规则重写比较方法
class PersonOrdering extends Ordering[Person] {
  override def compare(x: Person, y: Person): Int = if (x.age > y.age) 1 else -1
}

class Pair[T](val first: T, val second: T) {
  def smaller(implicit ord: Ordering[T]): T = if (ord.compare(first, second) < 0) first else second
}


object ScalaApp extends App {

  val pair = new Pair(new Person("hei", 88), new Person("bai", 66))
  // 3.定义隐式默认值,如果不定义,则下一行代码无法通过编译
  implicit val ImpPersonOrdering = new PersonOrdering
  println(pair.smaller) //输出： bai:66
}
```

### 2.5 类型下界限定

2.1小节介绍了类型上界的限定，Scala同时也支持下界的限定，语法为：`U >: T`，即U必须是类型T的超类或本身。

```scala
// 首席执行官
class CEO

// 部门经理
class Manager extends CEO

// 本公司普通员工
class Employee extends Manager

// 其他公司人员
class OtherCompany

object ScalaApp extends App {

  // 限定：只有本公司部门经理以上人员才能获取权限
  def Check[T >: Manager](t: T): T = {
    println("获得审核权限")
    t
  }

  // 错误写法: 省略泛型参数后,以下所有人都能获得权限,显然这是不正确的
  Check(new CEO)
  Check(new Manager)
  Check(new Employee)
  Check(new OtherCompany)


  // 正确写法,传入泛型参数
  Check[CEO](new CEO)
  Check[Manager](new Manager)
  /*
   * 以下两条语句无法通过编译,异常信息为: 
   * do not conform to method Check's type parameter bounds(不符合方法Check的类型参数边界)
   * 这种情况就完成了下界限制，即只有本公司经理及以上的人员才能获得审核权限
   */
  Check[Employee](new Employee)
  Check[OtherCompany](new OtherCompany)
}
```

### 2.6 多重界定

+ 类型变量可以同时有上界和下界。 写法为 ：`T > : Lower <: Upper`；

+ 不能同时有多个上界或多个下界 。但可以要求一个类型实现多个特质，写法为 :

  `T < : Comparable[T] with Serializable with Cloneable`；

+ 你可以有多个上下文界定，写法为`T : Ordering : ClassTag` 。



## 三、Ordering & Ordered

上文中使用到Ordering和Ordered特质，它们最主要的区别在于分别继承自不同的Java接口：Comparable和Comparator：

+ **Comparable**：可以理解为内置的比较器，实现此接口的对象可以与自身进行比较；
+ **Comparator**：可以理解为外置的比较器；当对象自身并没有定义比较规则的时候，可以传入外部比较器进行比较。

为什么Java中要同时给出这两个比较接口，这是因为你要比较的对象不一定实现了Comparable接口，而你又想对其进行比较，这时候当然你可以修改代码实现Comparable，但是如果这个类你无法修改(如源码中的类)，这时候就可以使用外置的比较器。同样的问题在Scala中当然也会出现，所以Scala分别使用了Ordering和Ordered来继承它们。

<div align="center"> <img src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-ordered-ordering.png"/> </div>



下面分别给出Java中Comparable和Comparator接口的使用示例：

### 3.1 Comparable

```scala
import java.util.Arrays;
// 实现Comparable接口
public class Person implements Comparable<Person> {

    private String name;
    private int age;

    Person(String name,int age) {this.name=name;this.age=age;}
    @Override
    public String toString() { return name+":"+age; }

    // 核心的方法是重写比较规则，按照年龄进行排序
    @Override
    public int compareTo(Person person) {
        return this.age - person.age;
    }

    public static void main(String[] args) {
        Person[] peoples= {new Person("hei", 66), new Person("bai", 55), new Person("ying", 77)};
        Arrays.sort(peoples);
        Arrays.stream(peoples).forEach(System.out::println);
    }
}

输出：
bai:55
hei:66
ying:77
```

### 3.2 Comparator

```scala
import java.util.Arrays;
import java.util.Comparator;

public class Person {

    private String name;
    private int age;

    Person(String name,int age) {this.name=name;this.age=age;}
    @Override
    public String toString() { return name+":"+age; }

    public static void main(String[] args) {
        Person[] peoples= {new Person("hei", 66), new Person("bai", 55), new Person("ying", 77)};
        // 这里为了直观直接使用匿名内部类,实现Comparator接口
         //如果是Java8你也可以写成Arrays.sort(peoples, Comparator.comparingInt(o -> o.age));
        Arrays.sort(peoples, new Comparator<Person>() {
            @Override
            public int compare(Person o1, Person o2) {
                return o1.age-o2.age;
            }
        });
        Arrays.stream(peoples).forEach(System.out::println);
    }
}
```

使用外置比较器还有一个好处，就是你可以随时定义其排序规则：

```scala
// 按照年龄大小排序
Arrays.sort(peoples, Comparator.comparingInt(o -> o.age));
Arrays.stream(peoples).forEach(System.out::println);
// 按照名字长度倒序排列
Arrays.sort(peoples, Comparator.comparingInt(o -> -o.name.length()));
Arrays.stream(peoples).forEach(System.out::println);
```

### 3.3 上下文界定的优点

这里再次给出上下文界定中的示例代码作为回顾：

```scala
// 1.定义一个人员类
class Person(val name: String, val age: Int) {
  override def toString: String = name + ":" + age
}

// 2.继承Ordering[T],实现自定义比较器,这个比较器就是一个外置比较器
class PersonOrdering extends Ordering[Person] {
  override def compare(x: Person, y: Person): Int = if (x.age > y.age) 1 else -1
}

class Pair[T](val first: T, val second: T) {
  def smaller(implicit ord: Ordering[T]): T = if (ord.compare(first, second) < 0) first else second
}


object ScalaApp extends App {

  val pair = new Pair(new Person("hei", 88), new Person("bai", 66))
  // 3.在当前上下文定义隐式默认值,这就相当于传入了外置比较器
  implicit val ImpPersonOrdering = new PersonOrdering
  println(pair.smaller) //输出： bai:66
}
```

使用上下文界定和Ordering带来的好处是：传入`Pair`中的参数不一定需要可比较，只要在其进行比较时传入外置比较器即可。

需要注意的是由于隐式默认值二义性的限制，你不能像上面Java代码一样，在同一个上下文中传入两个外置比较器，即下面的代码是无法通过编译的。但是你可以在不同的上下文中引入不同的隐式默认值，即使用不同的外置比较器。

```scala
implicit val ImpPersonOrdering = new PersonOrdering
println(pair.smaller) 
implicit val ImpPersonOrdering2 = new PersonOrdering
println(pair.smaller)
```



## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7

