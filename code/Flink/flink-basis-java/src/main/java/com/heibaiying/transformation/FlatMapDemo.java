package com.heibaiying.transformation;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName FlatMapDemo.java
 * @Description TODO
 * @createTime 2021年05月04日 08:57:00
 */
public class FlatMapDemo {
    public static void main(String[] args) throws Exception {
        String string01 = "one one one two two";
        String string02 = "third third third four";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stringDataStream = env.fromElements(string01, string02);
        stringDataStream.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String value, Collector<String> out) throws Exception {
                for (String s : value.split(" ")) {
                    out.collect(s);
                }
            }
        }).print();
        env.execute();
    }
}
