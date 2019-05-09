# 继承和特质

## 一、继承

### 1.1 extends & override

父类Person：

```scala
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

### 1.2 调用父类构造器



### 1.3 构造顺序和提前定义



二、抽象类



二、特质