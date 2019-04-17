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

    private static final String REDIS_HOST = "192.168.200.226";
    private static final int REDIS_PORT = 6379;

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("dataSourceSpout", new DataSourceSpout());
        // split
        builder.setBolt("splitBolt", new SplitBolt()).shuffleGrouping("dataSourceSpout");
        // save to redis and count
        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost(REDIS_HOST).setPort(REDIS_PORT).build();
        RedisStoreMapper storeMapper = new WordCountStoreMapper();
        RedisCountStoreBolt countStoreBolt = new RedisCountStoreBolt(poolConfig, storeMapper);
        builder.setBolt("storeBolt", countStoreBolt).shuffleGrouping("splitBolt");

        // 如果外部传参cluster则代表线上环境启动否则代表本地启动
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
