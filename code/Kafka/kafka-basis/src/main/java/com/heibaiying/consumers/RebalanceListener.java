package com.heibaiying.consumers;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class RebalanceListener {

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

        Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();

        consumer.subscribe(Collections.singletonList(topic), new ConsumerRebalanceListener() {

            /*该方法会在消费者停止读取消息之后，再均衡开始之前就调用*/
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                System.out.println("再均衡即将触发");
                // 提交已经处理的偏移量
                consumer.commitSync(offsets);
            }

            /*该方法会在重新分配分区之后，消费者开始读取消息之前被调用*/
            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

            }
        });

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.println(record);
                    TopicPartition topicPartition = new TopicPartition(record.topic(), record.partition());
                    OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1, "no metaData");
                    /*TopicPartition重写过hashCode和equals方法，所以能够保证同一主题和分区的实例不会被重复添加*/
                    offsets.put(topicPartition, offsetAndMetadata);
                }
                consumer.commitAsync(offsets, null);
            }
        } finally {
            consumer.close();
        }

    }
}
