# 类和对象

<nav>
<a href="#一初识类和对象">一、初识类和对象</a><br/>
<a href="#二类">二、类</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#21-成员变量可见性">2.1 成员变量可见性</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#22-getter和setter属性">2.2 getter和setter属性</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#23-BeanProperty">2.3 @BeanProperty</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#24-主构造器">2.4 主构造器</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#25-辅助构造器">2.5 辅助构造器</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#26-方法传参不可变">2.6 方法传参不可变</a><br/>
<a href="#三对象">三、对象</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-工具类单例全局静态常量拓展特质">3.1 工具类&单例&全局静态常量&拓展特质</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-伴生对象">3.2 伴生对象</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#33-实现枚举类">3.3 实现枚举类</a><br/>
</nav>

## 一、初识类和对象

Scala中的类与Java中的类具有非常多的相似性，这里为了有个直观上的印象，首先给出一个类的示例。

```scala
// 1. 在scala中，类不需要用public声明,所有的类都具有公共的可见性
class Person {

  // 2. 声明私有变量,用var修饰的变量默认拥有getter/setter属性
  private var age = 0

  // 3.如果声明的变量不需要进行初始赋值，此时scala无法进行类型推荐，需要显式指明类型
  private var name: String = _


  // 4. 定义方法,应该指明传参类型和返回值的类型
  def growUp(step: Int): Unit = {
    age += step
  }

  // 5.对于改值器方法(即改变对象状态的方法),即使不需要传入参数,也建议在声明中包含()
  def growUpFix(): Unit = {
    age += 10
  }

  // 6.对于取值器方法(即不会改变对象状态的方法),不必在声明中包含()
  def currentAge: Int = {
    age
  }

  /**
   * 7.不建议使用return关键字,默认方法中最后一行代码的计算结果为返回值
   *   如果方法很简短，甚至可以写在同一行中
   */
  def getName: String = name

}


// 伴生对象
object Person {

  def main(args: Array[String]): Unit = {
    // 8.创建类的实例对象
    val counter = new Person()
    // 9.用var修饰的变量默认拥有getter/setter属性，可以直接对其进行赋值
    counter.age = 12
    counter.growUp(8)
    counter.growUpFix()
    // 10.用var修饰的变量默认拥有getter/setter属性，可以直接对其进行取值，输出: 30
    println(counter.age)
    // 输出: 30
    println(counter.currentAge)
    // 输出: null
    println(counter.getName)
  }

}
```

## 二、类

### 2.1 成员变量可见性

Scala中成员变量的可见性默认都是public，如果想要保证其不被外部干扰，可以声明为private，并通过getter和setter方法进行访问。

### 2.2 getter和setter属性

getter和setter属性与声明变量时使用的关键字有关：

+ 使用var关键字：变量同时拥有getter和setter属性；
+ 使用val关键字：变量只拥有getter属性；
+ 使用private[this]：变量既没有getter属性、也没有setter属性，只能通过内部的方法访问；

需要特别说明的是：假设变量名为age,则其对应的get和set的方法名分别叫做` age`和`age_=`。

```scala
class Person {

  private val name = "heibaiying"
  private var age = 12
  private[this] var birthday = "2019-08-08"
  // birthday只能被内部方法所访问
  def getBirthday: String = birthday
}

object Person {
  def main(args: Array[String]): Unit = {
    val person = new Person
    person.age = 30
    println(person.name)
    println(person.age)
    println(person.getBirthday)
  }
}
```

> 解释说明：
>
> 示例代码中`person.age=30`在执行时内部实际是调用了方法`person.age_=(30) `，而`person.age`内部执行时实际是调用了`person.age()`方法。想要证明这一点，可以对代码进行反编译。同时为了说明成员变量可见性的问题，我们对下面这段代码进行反编译：
>
> ```scala
> class Person {
>   var name = ""
>   private var age = ""
> }
> ```
>
> 依次执行下面编译命令：
>
> ```shell
> > scalac Person.scala
> > javap -private Person
> ```
>
> 编译结果如下，从编译结果可以看到实际的get和set的方法名，同时也验证了成员变量默认的可见性为public。
>
> ```java
> Compiled from "Person.scala"
> public class Person {
>   private java.lang.String name;
>   private java.lang.String age;
>     
>   public java.lang.String name();
>   public void name_$eq(java.lang.String);
>     
>   private java.lang.String age();
>   private void age_$eq(java.lang.String);
>     
>   public Person();
> }
> ```

### 2.3 @BeanProperty

在上面的例子中可以看到我们是使用`.`来对成员变量进行访问的，如果想要额外生成和Java中一样的getXXX和setXXX方法，则需要使用@BeanProperty进行注解。

```scala
class Person {
  @BeanProperty var name = ""
}

object Person {
  def main(args: Array[String]): Unit = {
    val person = new Person
    person.setName("heibaiying")
    println(person.getName)
  }
}
```



### 2.4 主构造器

和Java不同的是，Scala类的主构造器直接写在类名后面，同时需要注意以下两点：

+ 主构造器传入的参数默认就是val类型的，即不可变，你没有办法在内部改变传参；
+ 写在主构造器中的代码块会在类初始化的时候被执行，功能类似于Java的静态代码块`static{}`

```scala
class Person(name: String, age: Int) {

  println("功能类似于Java的静态代码块static{}")

  def getDetail: String = {
    //name="heibai" 无法通过编译
    name + ":" + age
  }
}

object Person {
  def main(args: Array[String]): Unit = {
    val person = new Person("heibaiying", 20)
    println(person.getDetail)
  }
}

输出：
功能类似于Java的静态代码块static{}
heibaiying:20
```



### 2.5 辅助构造器

辅助构造器有两点硬性要求：

+ 辅助构造器的名称必须为this;
+ 每个辅助构造器必须以主构造器或其他的辅助构造器的调用开始。

```scala
class Person(name: String, age: Int) {

  private var birthday = ""

  // 1.辅助构造器的名称必须为this
  def this(name: String, age: Int, birthday: String) {
    // 2.每个辅助构造器必须以主构造器或其他的辅助构造器的调用开始
    this(name, age)
    this.birthday = birthday
  }

  // 3.重写toString方法
  override def toString: String = name + ":" + age + ":" + birthday
}

object Person {
  def main(args: Array[String]): Unit = {
    println(new Person("heibaiying", 20, "2019-02-21"))
  }
}
```



### 2.6 方法传参不可变

在Scala中，方法传参默认是val类型，即不可变，这意味着你在方法体内部不能改变传入的参数。这和scala的设计理念有关，Scala遵循函数式编程理念，强调方法不应该有副作用。

```scala
class Person() {

  def low(word: String): String = {
    word="word" // 编译无法通过
    word.toLowerCase
  }
}
```



## 三、对象

Scala中的object(对象)主要有以下几个作用：

+ 因为object中的变量和方法都是静态的，所以可以用于存放工具类；
+ 可以作为单例对象的容器；
+ 可以作为类的伴生对象；
+ 可以拓展类或特质；
+ 可以拓展Enumeration来实现枚举。

### 3.1 工具类&单例&全局静态常量&拓展特质

这里我们创建一个对象`Utils`,代码如下：

```scala
object Utils {

  /*
   *1. 相当于Java中的静态代码块static,会在对象初始化时候被执行
   *   这种方式实现的单例模式是饿汉式单例,即无论你的单例对象是否被用到，
   *   都在一开始被初始化完成
   */
  val person = new Person
  
  // 2. 全局固定常量 等价于Java的public static final 
  val CONSTANT = "固定常量"

  // 3. 全局静态方法
  def low(word: String): String = {
    word.toLowerCase
  }
}
```

其中Person类代码如下：

```scala
class Person() {
  println("Person默认构造器被调用")
}
```

新建测试类：

```scala
// 1.ScalaApp对象扩展自trait App
object ScalaApp extends App {

  // 2.验证单例
  println(Utils.person == Utils.person)

  // 3.获取全局常量
  println(Utils.CONSTANT)

  // 4.调用工具类
  println(Utils.low("ABCDEFG"))
  
}

// 输出如下：
Person默认构造器被调用
true
固定常量
abcdefg
```

### 3.2 伴生对象

在Java中，你通常会用到既有实例方法又有静态方法的类，在Scala中，可以通过类和与类同名的伴生对象来实现。类和伴生对象必须存在与同一个文件中。

```scala
class Person() {

  private val name = "HEIBAIYING"

  def getName: String = {
    // 调用伴生对象的方法和属性
    Person.toLow(Person.PREFIX + name)
  }
}

// 伴生对象
object Person {

  val PREFIX = "prefix-"

  def toLow(word: String): String = {
    word.toLowerCase
  }

  def main(args: Array[String]): Unit = {
    val person = new Person
    // 输出 prefix-heibaiying  
    println(person.getName)
  }

}
```



### 3.3 实现枚举类

Scala中没有直接提供枚举类，需要通过扩展`Enumeration`，并调用其中的Value方法对所有枚举值进行初始化来实现。

```scala
object Color extends Enumeration {

  // 1.类型别名,建议声明,在import时有用
  type Color = Value

  // 2.调用Value方法
  val GREEN = Value
  // 3.只传入id
  val RED = Value(3)
  // 4.只传入值
  val BULE = Value("blue")
  // 5.传入id和值
  val YELLOW = Value(5, "yellow")
  // 6. 不传入id时,id为上一个声明变量的id+1,值默认和变量名相同
  val PINK = Value
 
}
```

使用枚举类：

```scala
object ScalaApp extends App {

  // 1.使用枚举类型,这种情况下需要导入枚举类，在枚举类中定义的类型别名就有用了
  def printColor(color: Color): Unit = {
    println(color.toString)
  }

  // 2.判断传入值和枚举值是否相等
  println(Color.YELLOW.toString == "yellow")
  // 3.遍历枚举类和值
  for (c <- Color.values) println(c.id + ":" + c.toString)
}

//输出
true
0:GREEN
3:RED
4:blue
5:yellow
6:PINK
```



## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7

