package com.heibaiying.consumers;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;


/**
 * Kafka消费者和消费者组
 */
public class ConsumerExit {

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


        /*调用wakeup优雅的退出*/
        final Thread mainThread = Thread.currentThread();
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (sc.hasNext()) {
                if ("exit".equals(sc.next())) {
                    consumer.wakeup();
                    try {
                        /*等待主线程完成提交偏移量、关闭消费者等操作*/
                        mainThread.join();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        try {
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(100, ChronoUnit.MILLIS));
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("topic = %s,partition = %d, key = %s, value = %s, offset = %d,\n",
                            record.topic(), record.partition(), record.key(), record.value(), record.offset());
                }
            }
        } catch (WakeupException e) {
            //对于wakeup()调用引起的WakeupException异常可以不必处理
        } finally {
            consumer.close();
            System.out.println("consumer关闭");
        }

    }
}
