package com.heibaiying.kafka.write;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.bolt.KafkaBolt;
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector;
import org.apache.storm.topology.TopologyBuilder;

import java.util.Properties;

/**
 * 写入数据到Kafka中
 */
public class WritingToKafkaApp {

    private static final String BOOTSTRAP_SERVERS = "hadoop001:9092";
    private static final String TOPIC_NAME = "storm-topic";

    public static void main(String[] args) {


        TopologyBuilder builder = new TopologyBuilder();

        // 定义Kafka生产者属性
        Properties props = new Properties();
        /*
         * 指定broker的地址清单，清单里不需要包含所有的broker地址，生产者会从给定的broker里查找其他broker的信息。
         * 不过建议至少要提供两个broker的信息作为容错。
         */
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        /*
         * acks 参数指定了必须要有多少个分区副本收到消息，生产者才会认为消息写入是成功的。
         * acks=0 : 生产者在成功写入消息之前不会等待任何来自服务器的响应。
         * acks=1 : 只要集群的首领节点收到消息，生产者就会收到一个来自服务器成功响应。
         * acks=all : 只有当所有参与复制的节点全部收到消息时，生产者才会收到一个来自服务器的成功响应。
         */
        props.put("acks", "1");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaBolt bolt = new KafkaBolt<String, String>()
                .withProducerProperties(props)
                .withTopicSelector(new DefaultTopicSelector(TOPIC_NAME))
                .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper<>());

        builder.setSpout("sourceSpout", new DataSourceSpout(), 1);
        builder.setBolt("kafkaBolt", bolt, 1).shuffleGrouping("sourceSpout");


        if (args.length > 0 && args[0].equals("cluster")) {
            try {
                StormSubmitter.submitTopology("ClusterWritingToKafkaApp", new Config(), builder.createTopology());
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalWritingToKafkaApp",
                    new Config(), builder.createTopology());
        }
    }
}
