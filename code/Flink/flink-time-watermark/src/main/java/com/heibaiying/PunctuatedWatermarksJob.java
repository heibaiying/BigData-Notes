package com.heibaiying;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPunctuatedWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;

public class PunctuatedWatermarksJob {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        // 设置以事件时间为基准
        env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);
        DataStreamSource<String> streamSource = env.socketTextStream("192.168.200.229", 8888, "\n", 3);
        streamSource.map(new MapFunction<String, Tuple3<Long, String, Long>>() {
            @Override
            public Tuple3<Long, String, Long> map(String value) throws Exception {
                String[] split = value.split(",");
                return new Tuple3<>(Long.valueOf(split[0]), split[1], 1L);
            }
        }).assignTimestampsAndWatermarks(new PunctuatedAssigner())
                .keyBy(1).timeWindow(Time.seconds(3)).sum(2).print();
        env.execute();

    }
}

class PunctuatedAssigner implements AssignerWithPunctuatedWatermarks<Tuple3<Long, String, Long>> {

    @Override
    public long extractTimestamp(Tuple3<Long, String, Long> element, long previousElementTimestamp) {
        return element.f0;
    }

    @Override
    public Watermark checkAndGetNextWatermark(Tuple3<Long, String, Long> lastElement, long extractedTimestamp) {
        return new Watermark(extractedTimestamp);
    }
}

