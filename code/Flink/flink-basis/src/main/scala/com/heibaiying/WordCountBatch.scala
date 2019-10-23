package com.heibaiying

import org.apache.flink.api.scala._

object WordCountBatch {

  def main(args: Array[String]): Unit = {
    val benv = ExecutionEnvironment.getExecutionEnvironment
    val text = benv.readTextFile("D:\\BigData-Notes\\code\\Flink\\flink-basis\\src\\main\\resources\\wordcount.txt")
    val counts = text.flatMap { _.toLowerCase.split(",") filter { _.nonEmpty } }.map { (_, 1) }.groupBy(0).sum(1)
    counts.print()
  }
}
