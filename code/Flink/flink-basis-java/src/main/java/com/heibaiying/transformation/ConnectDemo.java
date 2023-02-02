package com.heibaiying.transformation;

import com.heibaiying.utils.EnvironmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.ConnectedStreams;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoMapFunction;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName ConnectDemo.java
 * @Description Connect 操作用于连接两个或者多个类型不同的 DataStream ，其返回的类型是 ConnectedStreams
 * ，此时被连接的多个 DataStreams 可以共享彼此之间的数据状态
 * @createTime 2021年05月04日 10:21:00
 */
@Slf4j
public class ConnectDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = EnvironmentUtil.getEnvironment();

        DataStreamSource<Tuple2<String, Integer>> streamSource01 = env.fromElements(new
                Tuple2<>("a", 3), new Tuple2("b", 5));

        DataStreamSource<Integer> streamSource02 = env.fromElements(2, 3, 9);

        // 使用connect进行连接
        ConnectedStreams<Tuple2<String, Integer>, Integer> connect =
                streamSource01.connect(streamSource02);

        connect.map(new CoMapFunction<Tuple2<String, Integer>, Integer, Integer>() {
            @Override
            public Integer map1(Tuple2<String, Integer> value) throws Exception {
                log.info("value.f1=" + value.f1);
                return value.f1;
            }

            @Override
            public Integer map2(Integer value) throws Exception {
                log.info("value=" + value);
                return value;
            }
        }).map(x -> x * 100).print();
        env.execute();
    }
}
