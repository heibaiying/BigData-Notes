package com.heibaiying.transformation;

import com.heibaiying.utils.EnvironmentUtil;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName AggregationsDemo.java
 * @Description Aggregations 是官方提供的聚合算子，封装了常用的聚合操作
 * @createTime 2021年05月04日 09:58:00
 */
public class AggregationsDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = EnvironmentUtil.getEnvironment();
        DataStreamSource<Tuple2<String, Integer>> tuple2DataStream = env.fromElements(new Tuple2<>("1", 1)
                , new Tuple2<>("2", 2), new Tuple2<>("3", 3), new Tuple2<>("5", 5));
        tuple2DataStream.keyBy(0).sum(1).print();

        KeyedStream<Tuple2<String, Integer>, Tuple> keyedStream = tuple2DataStream.keyBy(0);
        // 滚动计算指定key的最小值，可以通过index或者fieldName来指定key
        keyedStream.min(0);
        keyedStream.min("key");
        // 滚动计算指定key的最大值
        keyedStream.max(0);
        keyedStream.max("key");
        // 滚动计算指定key的最小值，并返回其对应的元素
        keyedStream.minBy(0);
        keyedStream.minBy("key");
        // 滚动计算指定key的最大值，并返回其对应的元素
        keyedStream.maxBy(0);
        keyedStream.maxBy("key");
        env.execute();
    }
}
