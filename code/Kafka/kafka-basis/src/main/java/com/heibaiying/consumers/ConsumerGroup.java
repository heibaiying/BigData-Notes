package com.heibaiying.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;


/**
 * Kafka消费者和消费者组
 */
public class ConsumerGroup {

    public static void main(String[] args) {
        String topic = "Hello-Kafka";
        String group = "group1";
        Properties props = new Properties();
        props.put("bootstrap.servers", "hadoop001:9092");
        /*指定分组ID*/
        props.put("group.id", group);
        props.put("enable.auto.commit", true);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

         /*订阅主题(s)*/
        consumer.subscribe(Collections.singletonList(topic));

        try {
            while (true) {
                /*轮询获取数据*/
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s,partition = %d, key = %s, value = %s, offset = %d,\n",
                            record.topic(), record.partition(), record.key(), record.value(), record.offset());
                }
            }
        } finally {
            consumer.close();
        }

    }
}
