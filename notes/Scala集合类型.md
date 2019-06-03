# 集合

<nav>
<a href="#一集合简介">一、集合简介</a><br/>
<a href="#二集合结构">二、集合结构</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#31-scalacollection">3.1 scala.collection</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-scalacollectionmutable">3.2 scala.collection.mutable</a><br/>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href="#32-scalacollectionimmutable">3.2 scala.collection.immutable</a><br/>
<a href="#三Trait-Traversable">三、Trait Traversable</a><br/>
<a href="#四Trait-Iterable">四、Trait Iterable</a><br/>
<a href="#五修改集合">五、修改集合</a><br/>
</nav>

## 一、集合简介

Scala中拥有多种集合类型，主要分为可变的和不可变的集合两大类：

+ **可变集合**： 可以被修改。即可以更改，添加，删除集合中的元素；

+ **不可变集合类**：不能被修改。对集合执行更改，添加或删除操作都会返回一个新的集合，而不是修改原来的集合。

## 二、集合结构

scala所有的集合类主要位于`scala.collection` 、`scala.collection.mutable`、`scala.collection.immutable`、`scala.collection.generic`包中 ，大部分集合类存在三类变体，分别位于`scala.collection`, `scala.collection.immutable`, `scala.collection.mutable`包。

- `scala.collection.immutable`包是中的集合是不可变的；
- `scala.collection.mutable`包中的集合是可变的；
- `scala.collection`包中的集合，既可以是可变的，也可以是不可变的。

```scala
val sortSet = scala.collection.SortedSet(1, 2, 3, 4, 5)
val mutableSet = collection.mutable.SortedSet(1, 2, 3, 4, 5)
val immutableSet = collection.immutable.SortedSet(1, 2, 3, 4, 5)
```

如果你仅写了`Set` 而没有加任何前缀也没有进行任何`import`，则Scala默认采用不可变集合类。

```scala
scala> Set(1,2,3,4,5)
res0: scala.collection.immutable.Set[Int] = Set(5, 1, 2, 3, 4)
```

### 3.1 scala.collection

scala.collection包中所有集合如下图：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-collection.png"/> </div>

### 3.2 scala.collection.mutable

scala.collection.mutable包中所有集合如下图：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-collection-m.png"/> </div>

### 3.2 scala.collection.immutable

scala.collection.immutable包中所有集合如下图：

<div align="center"> <img  src="https://github.com/heibaiying/BigData-Notes/blob/master/pictures/scala-collection-imm.png"/> </div>

## 三、Trait Traversable

Scala中所有集合的顶层实现是`Traversable` 。它唯一的抽象方法是`foreach`：

```scala
def foreach[U](f: Elem => U)
```

实现Traversable的集合类只需要实现这个抽象方法，所有其他方法都可以从Traversable继承。Traversable中定义了其余的几十种的方法，具体可以参考官方文档：https://docs.scala-lang.org/overviews/collections/trait-traversable.html



## 四、Trait Iterable

Iterable拓展了Traversable，并定义了一些额外的方法，Scala中所有的集合都直接或者间接实现了Iterable。Iterable中额外定义了十几种方法，具体可以参考官方文档：https://docs.scala-lang.org/overviews/collections/trait-iterable.html



## 五、修改集合

当你想对集合添加或者删除元素，需要根据不同的集合类型选择不同的操作符号：

| 操作符                                                       | 描述                                              | 集合类型              |
| ------------------------------------------------------------ | ------------------------------------------------- | --------------------- |
| coll(k)<br/>即coll.apply(k)                                  | 获取指定位置的元素                                | Seq, Map              |
| coll :+ elem<br/>elem +: coll                                | 向集合末尾或者集合头增加元素                      | Seq                   |
| coll + elem<br/>coll + (e1, e2, ...)                         | 追加元素                                          | Seq, Map              |
| coll - elem<br/>coll - (e1, e2, ...)                         | 删除元素                                          | Set, Map, ArrayBuffer |
| coll ++ coll2<br/>coll2 ++: coll                             | 合并集合                                          | Iterable              |
| coll -- coll2                                                | 移除coll中包含的coll2中的元素                     | Set, Map, ArrayBuffer |
| elem :: lst<br/>lst2 :: lst                                  | 把指定列表(lst2)或者元素(elem)添加到列表(lst)头部 | List                  |
| list ::: list2                                               | 合并List                                          | List                  |
| set \| set2<br/>set & set2<br/>set &~ set2                   | 并集、交集、差集                                  | Set                   |
| coll += elem<br/>coll += (e1, e2, ...)<br/>coll ++= coll2<br/>coll -= elem<br/>coll -= (e1, e2, ...)<br/>coll --= coll2 | 添加或者删除元素，并将修改后的结果赋值给集合本身  | 可变集合              |
| elem +=: coll<br/>coll2 ++=: coll                            | 在集合头部追加元素或集合                          | ArrayBuffer           |



## 参考资料

1. Martin Odersky . Scala编程(第3版)[M] . 电子工业出版社 . 2018-1-1  
2. 凯.S.霍斯特曼  . 快学Scala(第2版)[M] . 电子工业出版社 . 2017-7
3. https://docs.scala-lang.org/overviews/collections/overview.html
4. https://docs.scala-lang.org/overviews/collections/trait-traversable.html
5. https://docs.scala-lang.org/overviews/collections/trait-iterable.html