package com.heibaiying.producers;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;

/*
 * Kafka生产者示例——异步发送消息
 */
public class ProducerWithPartitioner {

    public static void main(String[] args) {

        String topicName = "Kafka-Partitioner-Test";

        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.200.226:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        props.put("partitioner.class", "com.heibaiying.producers.utils.CustomPartitioner");
        props.put("pass.line", 6);

        Producer<Integer, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i <= 10; i++) {
            /*异步发送消息*/
            String score = "score:" + i;
            ProducerRecord<Integer, String> record = new ProducerRecord<>(topicName, i, score);
            producer.send(record, (metadata, exception) ->
                    System.out.printf("%s, partition=%d, \n", score, metadata.partition()));
        }

        /*关闭生产者*/
        producer.close();
    }
}