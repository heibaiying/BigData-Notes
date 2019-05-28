package com.heibaiying.producers;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/*
 * Kafka生产者示例——异步发送消息
 */
public class ProducerASyn {

    public static void main(String[] args) {

        String topicName = "Hello-Kafka";

        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /*创建生产者*/
        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 10; i++) {
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "k" + i, "world" + i);
            /*异步发送消息，并监听回调*/
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (exception != null) {
                        System.out.println("进行异常处理");
                    } else {
                        System.out.printf("topic=%s, partition=%d, offset=%s \n",
                                metadata.topic(), metadata.partition(), metadata.offset());
                    }
                }
            });
        }

        /*关闭生产者*/
        producer.close();
    }
}