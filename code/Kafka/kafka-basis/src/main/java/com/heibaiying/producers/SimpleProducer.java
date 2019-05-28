package com.heibaiying.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/*
 * Kafka生产者示例
 */

public class SimpleProducer {

    public static void main(String[] args) {

        String topicName = "Hello-Kafka";

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.200.226:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /*创建生产者*/
        org.apache.kafka.clients.producer.Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 10; i++) {
            /* 发送消息*/
            ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "hello" + i, "world" + i);
            producer.send(record);
        }

        /*关闭生产者*/
        producer.close();
    }
}