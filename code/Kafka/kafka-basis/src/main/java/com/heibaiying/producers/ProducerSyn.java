package com.heibaiying.producers;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

/*
 * Kafka生产者示例——同步发送消息
 */
public class ProducerSyn {

    public static void main(String[] args) {

        String topicName = "Hello-Kafka";

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.200.226:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        /*创建生产者*/
        Producer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 10; i++) {
            /*同步发送消息*/
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "k" + i, "world" + i);
                RecordMetadata metadata = producer.send(record).get();
                System.out.printf("topic=%s, partition=%d, offset=%s \n",
                        metadata.topic(), metadata.partition(), metadata.offset());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        /*关闭生产者*/
        producer.close();
    }
}