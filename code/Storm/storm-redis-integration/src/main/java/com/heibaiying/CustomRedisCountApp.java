package com.heibaiying;

import com.heibaiying.component.*;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.redis.bolt.RedisStoreBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.TopologyBuilder;


/**
 * 利用自定义的RedisBolt实现词频统计
 */
public class CustomRedisCountApp {

    private static final String DATA_SOURCE_SPOUT = "dataSourceSpout";
    private static final String SPLIT_BOLT = "splitBolt";
    private static final String STORE_BOLT = "storeBolt";

    private static final String REDIS_HOST = "192.168.200.226";
    private static final int REDIS_PORT = 6379;

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(DATA_SOURCE_SPOUT, new DataSourceSpout());
        // split
        builder.setBolt(SPLIT_BOLT, new SplitBolt()).shuffleGrouping(DATA_SOURCE_SPOUT);
        // save to redis and count
        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost(REDIS_HOST).setPort(REDIS_PORT).build();
        RedisStoreMapper storeMapper = new WordCountStoreMapper();
        RedisCountStoreBolt countStoreBolt = new RedisCountStoreBolt(poolConfig, storeMapper);
        builder.setBolt(STORE_BOLT, countStoreBolt).shuffleGrouping(SPLIT_BOLT);

        // 如果外部传参cluster则代表线上环境启动,否则代表本地启动
        if (args.length > 0 && args[0].equals("cluster")) {
            try {
                StormSubmitter.submitTopology("ClusterCustomRedisCountApp", new Config(), builder.createTopology());
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalCustomRedisCountApp",
                    new Config(), builder.createTopology());
        }
    }
}
