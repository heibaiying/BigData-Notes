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

+ 可变集合： 可以被修改。即可以更改，添加，删除集合中的元素；

+ 不可变集合类：不能被修改。虽然可以对不可变集合执行添加，移除或更新操作，但是这些操作都会返回一个新的集合，而不是修改原来的集合。

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

如果你仅写了`Set` 而没有任何加前缀也没有进行任何`import`，则Scala默认采用不可变集合类。

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

实现Traversable的集合类只需要定义这个方法，所有其他方法都可以从Traversable继承。Traversable中定义了很多方法，如下:

> 下面表格来源于官方文档：https://docs.scala-lang.org/overviews/collections/trait-traversable.html

| 方法                                | 描述                                                         |
| ----------------------------------- | ------------------------------------------------------------ |
| **Abstract Method:**                |                                                              |
| `xs foreach f`                      | Executes function `f` for every element of `xs`.             |
| **Addition:**                       |                                                              |
| `xs ++ ys`                          | A collection consisting of the elements of both `xs` and `ys`. `ys` is a [TraversableOnce](http://www.scala-lang.org/api/current/scala/collection/TraversableOnce.html) collection, i.e., either a [Traversable](http://www.scala-lang.org/api/current/scala/collection/Traversable.html) or an [Iterator](http://www.scala-lang.org/api/current/scala/collection/Iterator.html). |
| **Maps:**                           |                                                              |
| `xs map f`                          | The collection obtained from applying the function f to every element in `xs`. |
| `xs flatMap f`                      | The collection obtained from applying the collection-valued function `f` to every element in `xs` and concatenating the results. |
| `xs collect f`                      | The collection obtained from applying the partial function `f` to every element in `xs` for which it is defined and collecting the results. |
| **Conversions:**                    |                                                              |
| `xs.toArray`                        | Converts the collection to an array.                         |
| `xs.toList`                         | Converts the collection to a list.                           |
| `xs.toIterable`                     | Converts the collection to an iterable.                      |
| `xs.toSeq`                          | Converts the collection to a sequence.                       |
| `xs.toIndexedSeq`                   | Converts the collection to an indexed sequence.              |
| `xs.toStream`                       | Converts the collection to a lazily computed stream.         |
| `xs.toSet`                          | Converts the collection to a set.                            |
| `xs.toMap`                          | Converts the collection of key/value pairs to a map. If the  collection does not have pairs as elements, calling this operation  results in a static type error. |
| **Copying:**                        |                                                              |
| `xs copyToBuffer buf`               | Copies all elements of the collection to buffer `buf`.       |
| `xs copyToArray(arr, s, n)`         | Copies at most `n` elements of the collection to array `arr` starting at index `s`. The last two arguments are optional. |
| **Size info:**                      |                                                              |
| `xs.isEmpty`                        | Tests whether the collection is empty.                       |
| `xs.nonEmpty`                       | Tests whether the collection contains elements.              |
| `xs.size`                           | The number of elements in the collection.                    |
| `xs.hasDefiniteSize`                | True if `xs` is known to have finite size.                   |
| **Element Retrieval:**              |                                                              |
| `xs.head`                           | The first element of the collection (or, some element, if no order is defined). |
| `xs.headOption`                     | The first element of `xs` in an option value, or None if `xs` is empty. |
| `xs.last`                           | The last element of the collection (or, some element, if no order is defined). |
| `xs.lastOption`                     | The last element of `xs` in an option value, or None if `xs` is empty. |
| `xs find p`                         | An option containing the first element in `xs` that satisfies `p`, or `None` if no element qualifies. |
| **Subcollections:**                 |                                                              |
| `xs.tail`                           | The rest of the collection except `xs.head`.                 |
| `xs.init`                           | The rest of the collection except `xs.last`.                 |
| `xs slice (from, to)`               | A collection consisting of elements in some index range of `xs` (from `from` up to, and excluding `to`). |
| `xs take n`                         | A collection consisting of the first `n` elements of `xs` (or, some arbitrary `n` elements, if no order is defined). |
| `xs drop n`                         | The rest of the collection except `xs take n`.               |
| `xs takeWhile p`                    | The longest prefix of elements in the collection that all satisfy `p`. |
| `xs dropWhile p`                    | The collection without the longest prefix of elements that all satisfy `p`. |
| `xs filter p`                       | The collection consisting of those elements of xs that satisfy the predicate `p`. |
| `xs withFilter p`                   | A non-strict filter of this collection. Subsequent calls to `map`, `flatMap`, `foreach`, and `withFilter` will only apply to those elements of `xs` for which the condition `p` is true. |
| `xs filterNot p`                    | The collection consisting of those elements of `xs` that do not satisfy the predicate `p`. |
| **Subdivisions:**                   |                                                              |
| `xs splitAt n`                      | Split `xs` at a position, giving the pair of collections `(xs take n, xs drop n)`. |
| `xs span p`                         | Split `xs` according to a predicate, giving the pair of collections `(xs takeWhile p, xs.dropWhile p)`. |
| `xs partition p`                    | Split `xs` into a pair of collections; one with elements that satisfy the predicate `p`, the other with elements that do not, giving the pair of collections `(xs filter p, xs.filterNot p)` |
| `xs groupBy f`                      | Partition `xs` into a map of collections according to a discriminator function `f`. |
| **Element Conditions:**             |                                                              |
| `xs forall p`                       | A boolean indicating whether the predicate `p` holds for all elements of `xs`. |
| `xs exists p`                       | A boolean indicating whether the predicate `p` holds for some element in `xs`. |
| `xs count p`                        | The number of elements in `xs` that satisfy the predicate `p`. |
| **Folds:**                          |                                                              |
| `(z /: xs)(op)`                     | Apply binary operation `op` between successive elements of `xs`, going left to right and starting with `z`. |
| `(xs :\ z)(op)`                     | Apply binary operation `op` between successive elements of `xs`, going right to left and starting with `z`. |
| `xs.foldLeft(z)(op)`                | Same as `(z /: xs)(op)`.                                     |
| `xs.foldRight(z)(op)`               | Same as `(xs :\ z)(op)`.                                     |
| `xs reduceLeft op`                  | Apply binary operation `op` between successive elements of non-empty collection `xs`, going left to right. |
| `xs reduceRight op`                 | Apply binary operation `op` between successive elements of non-empty collection `xs`, going right to left. |
| **Specific Folds:**                 |                                                              |
| `xs.sum`                            | The sum of the numeric element values of collection `xs`.    |
| `xs.product`                        | The product of the numeric element values of collection `xs`. |
| `xs.min`                            | The minimum of the ordered element values of collection `xs`. |
| `xs.max`                            | The maximum of the ordered element values of collection `xs`. |
| **Strings:**                        |                                                              |
| `xs addString (b, start, sep, end)` | Adds a string to `StringBuilder` `b` that shows all elements of `xs` between separators `sep` enclosed in strings `start` and `end`. `start`, `sep`, `end` are all optional. |
| `xs mkString (start, sep, end)`     | Converts the collection to a string that shows all elements of `xs` between separators `sep` enclosed in strings `start` and `end`. `start`, `sep`, `end` are all optional. |
| `xs.stringPrefix`                   | The collection name at the beginning of the string returned from `xs.toString`. |
| **Views:**                          |                                                              |
| `xs.view`                           | Produces a view over `xs`.                                   |
| `xs view (from, to)`                | Produces a view that represents the elements in some index range of `xs`. |



## 四、Trait Iterable

Iterable拓展了Traversable，并定义了一些额外的方法，Scala中所有的集合都直接或者间接实现了Iterable。

> 下面表格来源于官方文档：https://docs.scala-lang.org/overviews/collections/trait-iterable.html

| 方法                   | 描述                                                         |
| ---------------------- | ------------------------------------------------------------ |
| **Abstract Method:**   |                                                              |
| `xs.iterator`          | An `iterator` that yields every element in `xs`, in the same order as `foreach` traverses elements. |
| **Other Iterators:**   |                                                              |
| `xs grouped size`      | An iterator that yields fixed-sized “chunks” of this collection. |
| `xs sliding size`      | An iterator that yields a sliding fixed-sized window of elements in this collection. |
| **Subcollections:**    |                                                              |
| `xs takeRight n`       | A collection consisting of the last `n` elements of `xs` (or, some arbitrary `n` elements, if no order is defined). |
| `xs dropRight n`       | The rest of the collection except `xs takeRight n`.          |
| **Zippers:**           |                                                              |
| `xs zip ys`            | An iterable of pairs of corresponding elements from `xs` and `ys`. |
| `xs zipAll (ys, x, y)` | An iterable of pairs of corresponding elements from `xs` and `ys`, where the shorter sequence is extended to match the longer one by appending elements `x` or `y`. |
| `xs.zipWithIndex`      | An iterable of pairs of elements from `xs` with their indices. |
| **Comparison:**        |                                                              |
| `xs sameElements ys`   | A test whether `xs` and `ys` contain the same elements in the same order |

## 五、修改集合

当你想对集合添加或者删除元素，需要根据不同的集合类型选择不同的操作符号。

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