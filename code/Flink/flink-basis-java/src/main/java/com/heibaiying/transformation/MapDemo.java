package com.heibaiying.transformation;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName Map.java
 * @Description TODO
 * @createTime 2021年05月04日 08:46:00
 */
public class MapDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<Integer> integerDataStream = env.fromElements(1, 2, 3, 4, 5);
        integerDataStream.map((MapFunction<Integer, Object>) value -> value * 2).print();
        env.execute();
    }
}
