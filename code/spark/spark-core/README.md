val list = List(3,6,9,10,12,21)
val listRDD = sc.parallelize(list)
val intsRDD = listRDD.map(_*10)
intsRDD.foreach(println)

sc.parallelize(list).map(_*10).foreach(println)


sc.parallelize(list).filter(_>=10).foreach(println)

val list = List(List(1, 2), List(3), List(), List(4, 5))
sc.parallelize(list).flatMap(_.toList).map(_*10).foreach(println)


val list = List(1,2,3,4,5)
sc.parallelize(list).reduce((x,y) => x+y)
sc.parallelize(list).reduce(_+_)


val list = List(("hadoop", 2), ("spark", 3), ("spark", 5), ("storm", 6),("hadoop", 2))
sc.parallelize(list).reduceByKey(_+_).foreach(println)



 val list = List(("hadoop", 2), ("spark", 3), ("spark", 5), ("storm", 6),("hadoop", 2))
sc.parallelize(list).groupByKey().map(x=>(x._1,x._2.toList)).foreach(println)

