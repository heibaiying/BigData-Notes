package com.heibaiying;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.TimeCharacteristic;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.apache.flink.streaming.api.windowing.time.Time;

public class PeriodicWatermarksJob {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 设置并行度为1
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
        }).assignTimestampsAndWatermarks(new BoundedOutOfOrdernessGenerator())
                .keyBy(1).timeWindow(Time.seconds(3)).sum(2).print();
        env.execute();

    }
}


class BoundedOutOfOrdernessGenerator implements AssignerWithPeriodicWatermarks<Tuple3<Long, String, Long>> {

    private final long maxOutOfOrderness = 3000L;
    private long currentMaxTimestamp = 0L;

    @Override
    public long extractTimestamp(Tuple3<Long, String, Long> element, long previousElementTimestamp) {
        long timestamp = element.f0;
        currentMaxTimestamp = Math.max(timestamp, currentMaxTimestamp);
        return timestamp;
    }

    @Override
    public Watermark getCurrentWatermark() {
        return new Watermark(currentMaxTimestamp - maxOutOfOrderness);
    }
}
