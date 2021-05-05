package com.heibaiying.transformation;

import com.heibaiying.utils.EnvironmentUtil;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName UnionDemo.java
 * @Description 用于连接两个或者多个元素类型相同的 DataStream
 * @createTime 2021年05月04日 10:13:00
 */
public class UnionDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = EnvironmentUtil.getEnvironment();
        DataStreamSource<Tuple2<String, Integer>>  streamSource01 = env.fromElements(
                new Tuple2<>("a", 1)
                ,new Tuple2<>("a", 2));

        DataStreamSource<Tuple2<String, Integer>>  streamSource02 = env.fromElements(
                new Tuple2<>("b", 1), new Tuple2<>("b", 2));

//        streamSource01.union(streamSource02).print();
          streamSource01.union(streamSource01,streamSource02).print();
        env.execute();
    }
}
