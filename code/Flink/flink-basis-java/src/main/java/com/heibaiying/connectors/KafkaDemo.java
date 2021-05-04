package com.heibaiying.connectors;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;

import java.util.Properties;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName KafkaTest.java
 * @Description TODO
 * @createTime 2021年05月03日 22:37:00
 */
public class KafkaDemo {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "kafka:9092");
        // 指定监听的主题，并定义Kafka字节消息到Flink对象之间的转换规则
        DataStream<String> stream = env.addSource(new FlinkKafkaConsumer<>("flink-stream-in-topic", new
                SimpleStringSchema(), properties));
        stream.print();
        env.execute("Flink Streaming ");
    }
}
