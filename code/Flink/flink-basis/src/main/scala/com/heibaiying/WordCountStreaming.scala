package com.heibaiying

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


object WordCountStreaming {

  def main(args: Array[String]): Unit = {

    val senv = StreamExecutionEnvironment.getExecutionEnvironment

    val text: DataStream[String] = senv.socketTextStream("192.168.200.229", 9999, '\n')
    val windowCounts = text.flatMap { w => w.split(",") }.map { w => WordWithCount(w, 1) }.keyBy("word")
      .timeWindow(Time.seconds(5)).sum("count")

    windowCounts.print().setParallelism(1)

    senv.execute("Streaming WordCount")

  }

  case class WordWithCount(word: String, count: Long)

}
