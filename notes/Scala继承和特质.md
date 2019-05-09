# 继承和特质

## 一、继承

### 1.1 Scala中的继承结构

scala中继承关系如下图：

+ Any是整个继承关系的根节点；
+ AnyRef包含Scala Classes和Java Classes，等价于Java中的java.lang.Object；
+ AnyVal是所有值类型的一个标记；
+ Null是所有引用类型的子类型，唯一实例是null，可以将null赋值给除了值类型外的所有类型的变量;
+ Nothing是所有类型的子类型。

![scala继承层次](D:\BigData-Notes\pictures\scala继承层次.png)

### 1.2 extends & override

Scala的集成机制和Java有很多相似之处，比如都使用`extends `关键字表示继承，都使用`override`关键字表示重写父类的方法或成员变量。下面给出一个Scala继承的示例：

```scala
//父类
class Person {


  var name = ""

  // 1.不加任何修饰词,默认为public,能被子类和外部访问
  var age = 0

  // 2.使用protected修饰的变量能子类访问，但是不能被外部访问
  protected var birthday = ""

  // 3.使用private修饰的变量不能被子类和外部访问
  private var sex = ""


  def setSex(sex: String): Unit = {
    this.sex = sex
  }

  // 4.重写父类的方法建议使用override关键字修饰
  override def toString: String = name + ":" + age + ":" + birthday + ":" + sex

}
```

使用`extends`关键字实现继承：

```scala
// 1.使用extends关键字实现继承
class Employee extends Person {

  override def toString: String = "Employee~" + super.toString

  // 2.使用public或protected关键字修饰的变量能被子类访问
  def setBirthday(date: String): Unit = {
    birthday = date
  }

}
```

测试继承：

```scala

object ScalaApp extends App {

  val employee = new Employee

  employee.name = "heibaiying"
  employee.age = 20
  employee.setBirthday("2019-03-05")
  employee.setSex("男")

  println(employee)
}

// 输出： Employee~heibaiying:20:2019-03-05:男
```

### 1.3 调用超类构造器

在Scala的类中，每个辅助构造器都必须首先调用其他构造器或主构造器，这样就导致了子类的辅助构造器永远无法直接调用超类的构造器，只有主构造器才能调用超类的构造器。所以想要调用超类的构造器，代码示例如下：

```scala
class Employee(name:String,age:Int,salary:Double) extends Person(name:String,age:Int) {
    .....
}
```

### 1.4 类型检查和转换

想要实现类检查可以使用`isInstanceOf`,判断一个实例是否来源于某个类或者其子类，如果是，则可以使用`asInstanceOf`进行强制类型转换。

```scala
object ScalaApp extends App {

  val employee = new Employee
  val person = new Person

  // 1. 判断一个实例是否来源于某个类或者其子类 输出 true 
  println(employee.isInstanceOf[Person])
  println(person.isInstanceOf[Person])

  // 2. 强制类型转换
  var p: Person = employee.asInstanceOf[Person]

  // 3. 判断一个实例是否来源于某个类(而不是其子类)
  println(employee.getClass == classOf[Employee])

}
```

### 1.5 构造顺序和提前定义

#### 1. 构造顺序

在Scala中还有一个需要注意的问题，如果你在子类中重写父类的val变量，并且超类的构造器中使用了该变量，那么可能会产生不可预期的错误。下面给出一个示例：

```scala
// 父类
class Person {
  println("父类的默认构造器")
  val range: Int = 10
  val array: Array[Int] = new Array[Int](range)
}

//子类
class Employee extends Person {
  println("子类的默认构造器")
  override val range = 2
}

//测试
object ScalaApp extends App {
  val employee = new Employee
  println(employee.array.mkString("(", ",", ")"))

}
```

这里初始化array用到了变量range，这里你会发现实际上array既不会被初始化Array(10)，也不会被初始化为Array(2)，实际的输出应该如下：

```properties
父类的默认构造器
子类的默认构造器
()
```

实际上array被初始化为Array(0)，原因在于父类的构造器的执行先于子类的构造器，这里给出实际的执行步骤：

1. 父类的构造器被调用，执行`new Array[Int](range)`语句;
2. 这里想要得到range的值，会去调用子类range()方法，因为`override val `重写变量的同时也重写了其get方法；
3. 调用子类的range()方法，自然也是返回子类的range值，但是由于子类的构造器还没有执行，这也就意味着对range赋值的`range = 2`语句还没有被执行，所以自然返回range的默认值，也就是0。

这里可能比较疑惑的是为什么`val range = 2`没有被执行，却能使用range变量，这里因为在虚拟机层面，是先对成员变量先分配存储空间并赋给默认值，之后才赋予给定的值。想要证明这一点其实也比较简单，代码如下:

```scala
class Person {
  // val range: Int = 10 正常代码 array为Array(10)
  val array: Array[Int] = new Array[Int](range)
  val range: Int = 10  //如果把变量的声明放在使用之后，此时数据array为array(0)
}

object Person {
  def main(args: Array[String]): Unit = {
    val person = new Person
    println(person.array.mkString("(", ",", ")"))
  }
}
```

#### 2. 提前定义

想要解决上面的问题，有以下几种方法：

(1) . 将变量用final修饰，代表不允许被子类重写，即 `final val range: Int = 10 `；

(2) . 将变量使用lazy修饰，代表懒加载，即只有当你实际使用到array时候，才去进行初始化；

```scala
lazy val array: Array[Int] = new Array[Int](range)
```

(3) . 采用提前定义，代码如下，代表range的定义优先于超类构造器。

```scala
class Employee extends {
  override val range = 2
} with Person
```

但是这种语法也有缺陷，就是你只能在代码块中重写已有的变量，而不能定义新的变量和方法，这时候你想要拓展新的方法就只能再继承Employee，这样你的继承链就过于繁杂。所以最好的办法就是合理设计父类以规避上面的问题。

## 二、抽象类

Scala中允许使用`abstract`定义抽象类，并且通过`extends`关键字继承它。

定义抽象类：

```scala
abstract class Person {
  // 1.定义字段
  var name: String
  val age: Int

  // 2.定义抽象方法
  def geDetail: String

  // 3. scala的抽象类允许定义具体方法
  def print(): Unit = {
    println("抽象类中的默认方法")
  }
}
```

继承抽象类：

```scala
class Employee extends Person {
  // 覆盖抽象类中变量
  override var name: String = "employee"
  override val age: Int = 12

  // 覆盖抽象方法
  def geDetail: String = name + ":" + age
}

```

## 三、特质

