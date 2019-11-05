package com.heibaiying;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;

import javax.annotation.Nullable;
import java.util.Properties;

public class KafkaStreamingJob {

    public static void main(String[] args) throws Exception {

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 1.指定Kafka的相关配置属性
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", "192.168.0.229:9092");

        // 2.接收Kafka上的数据
        DataStream<String> stream = env
                .addSource(new FlinkKafkaConsumer<>("flink-stream-in-topic", new SimpleStringSchema(), properties));

        // 3.定义计算结果到 Kafka ProducerRecord 的转换
        KafkaSerializationSchema<String> kafkaSerializationSchema = new KafkaSerializationSchema<String>() {
            @Override
            public ProducerRecord<byte[], byte[]> serialize(String element, @Nullable Long timestamp) {
                return new ProducerRecord<>("flink-stream-out-topic", element.getBytes());
            }
        };
        // 4. 定义Flink Kafka生产者
        FlinkKafkaProducer<String> kafkaProducer = new FlinkKafkaProducer<>("flink-stream-out-topic",
                kafkaSerializationSchema,
                properties,
                FlinkKafkaProducer.Semantic.AT_LEAST_ONCE, 5);
        // 5. 将接收到输入元素*2后写出到Kafka
        stream.map((MapFunction<String, String>) value -> value + value).addSink(kafkaProducer);
        env.execute("Flink Streaming");
    }
}
