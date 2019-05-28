package com.heibaiying.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;

/**
 * Kafka消费者——同步加异步提交
 */
public class ConsumerASynAndSyn {

    public static void main(String[] args) {
        String topic = "Hello-Kafka";
        String group = "group1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:9092");
        props.put("group.id", group);
        props.put("enable.auto.commit", false);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record);
                }
                // 异步提交
                consumer.commitAsync();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                // 因为即将要关闭消费者，所以要用同步提交保证提交成功
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }
}
