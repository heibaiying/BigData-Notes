package com.heibaiying;

import com.heibaiying.component.CountBolt;
import com.heibaiying.component.DataSourceSpout;
import com.heibaiying.component.SplitBolt;
import com.heibaiying.component.WordCountStoreMapper;
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
 * 进行词频统计 并将统计结果存储到Redis中
 *
 * 编译打包:           mvn clean assembly:assembly -Dmaven.test.skip=true
 * 提交Topology到集群: storm jar /usr/appjar/storm-redis-integration-1.0-jar-with-dependencies.jar  com.heibaiying.WordCountToRedisApp cluster
 * 停止Topology:      storm kill ClusterWordCountApp -w 3
 */
public class WordCountToRedisApp {

    private static final String REDIS_HOST = "192.168.200.226";
    private static final int REDIS_PORT = 6379;

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("dataSourceSpout", new DataSourceSpout());
        // split
        builder.setBolt("splitBolt", new SplitBolt()).shuffleGrouping("dataSourceSpout");
        // count
        builder.setBolt("countBolt", new CountBolt()).shuffleGrouping("splitBolt");
        // save to redis
        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost(REDIS_HOST).setPort(REDIS_PORT).build();
        RedisStoreMapper storeMapper = new WordCountStoreMapper();
        RedisStoreBolt storeBolt = new RedisStoreBolt(poolConfig, storeMapper);
        builder.setBolt("storeBolt", storeBolt).shuffleGrouping("countBolt");

        // 如果外部传参cluster则代表线上环境启动否则代表本地启动
        if (args.length > 0 && args[0].equals("cluster")) {
            try {
                StormSubmitter.submitTopology("ClusterWordCountToRedisApp", new Config(), builder.createTopology());
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalWordCountToRedisApp",
                    new Config(), builder.createTopology());
        }
    }
}
