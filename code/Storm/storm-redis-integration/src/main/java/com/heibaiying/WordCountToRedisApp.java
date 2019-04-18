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
 */
public class WordCountToRedisApp {

    private static final String DATA_SOURCE_SPOUT = "dataSourceSpout";
    private static final String SPLIT_BOLT = "splitBolt";
    private static final String COUNT_BOLT = "countBolt";
    private static final String STORE_BOLT = "storeBolt";

    //在实际开发中这些参数可以将通过外部传入 使得程序更加灵活
    private static final String REDIS_HOST = "192.168.200.226";
    private static final int REDIS_PORT = 6379;

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(DATA_SOURCE_SPOUT, new DataSourceSpout());
        // split
        builder.setBolt(SPLIT_BOLT, new SplitBolt()).shuffleGrouping(DATA_SOURCE_SPOUT);
        // count
        builder.setBolt(COUNT_BOLT, new CountBolt()).shuffleGrouping(SPLIT_BOLT);
        // save to redis
        JedisPoolConfig poolConfig = new JedisPoolConfig.Builder()
                .setHost(REDIS_HOST).setPort(REDIS_PORT).build();
        RedisStoreMapper storeMapper = new WordCountStoreMapper();
        RedisStoreBolt storeBolt = new RedisStoreBolt(poolConfig, storeMapper);
        builder.setBolt(STORE_BOLT, storeBolt).shuffleGrouping(COUNT_BOLT);

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
