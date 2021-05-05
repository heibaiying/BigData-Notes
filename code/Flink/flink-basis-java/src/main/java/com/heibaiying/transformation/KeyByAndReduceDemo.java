package com.heibaiying.transformation;

import com.heibaiying.utils.EnvironmentUtil;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName KeyByAndReduceDemo.java
 * @Description KeyBy [DataStream → KeyedStream] ：用于将相同 Key 值的数据分到相同的分区中,Reduce [KeyedStream → DataStream] ：用于对数据执行归约计算。
 * KeyBy 操作存在以下两个限制：
 * KeyBy 操作用于用户自定义的 POJOs 类型时，该自定义类型必须重写 hashCode 方法；
 * KeyBy 操作不能用于数组类型。
 * @createTime 2021年05月04日 09:44:00
 */
public class KeyByAndReduceDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = EnvironmentUtil.getEnvironment();
        DataStreamSource<Tuple2<String, Integer>> tuple2DataStream = env.fromElements(new Tuple2<>("1", 1)
                , new Tuple2<>("2", 2), new Tuple2<>("3", 3), new Tuple2<>("5", 5));
        KeyedStream<Tuple2<String, Integer>, Tuple> keyedStream = tuple2DataStream.keyBy(0);

        keyedStream.reduce((ReduceFunction<Tuple2<String, Integer>>) (value1, value2)
                -> new Tuple2<>(value1.f0, value1.f1 + value2.f1)).print();
        env.execute();
    }
}
