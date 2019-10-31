package com.heibaiying

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time


object WordCountStreaming {

  def main(args: Array[String]): Unit = {

    val senv = StreamExecutionEnvironment.getExecutionEnvironment

    val dataStream: DataStream[String] = senv.socketTextStream("192.168.0.229", 9999, '\n')
    dataStream.flatMap { line => line.toLowerCase.split(",") }
              .filter(_.nonEmpty)
              .map { word => (word, 1) }
              .keyBy(0)
              .timeWindow(Time.seconds(3))
              .sum(1)
              .print()
    senv.execute("Streaming WordCount")
  }
}
