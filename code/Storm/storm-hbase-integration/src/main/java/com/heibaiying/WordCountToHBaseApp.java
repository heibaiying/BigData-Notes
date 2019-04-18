package com.heibaiying;

import com.heibaiying.component.CountBolt;
import com.heibaiying.component.DataSourceSpout;
import com.heibaiying.component.SplitBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.hbase.bolt.HBaseBolt;
import org.apache.storm.hbase.bolt.mapper.SimpleHBaseMapper;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

import java.util.HashMap;
import java.util.Map;

/**
 * 进行词频统计 并将统计结果存储到HBase中
 */
public class WordCountToHBaseApp {

    private static final String DATA_SOURCE_SPOUT = "dataSourceSpout";
    private static final String SPLIT_BOLT = "splitBolt";
    private static final String COUNT_BOLT = "countBolt";
    private static final String HBASE_BOLT = "hbaseBolt";

    public static void main(String[] args) {

        // storm的配置
        Config config = new Config();

        // HBase的配置
        Map<String, Object> hbConf = new HashMap<>();
        hbConf.put("hbase.rootdir", "hdfs://hadoop001:8020/hbase");
        hbConf.put("hbase.zookeeper.quorum", "hadoop001:2181");

        // 将HBase的配置传入Storm的配置中
        config.put("hbase.conf", hbConf);

        // 定义流数据与HBase中数据的映射
        SimpleHBaseMapper mapper = new SimpleHBaseMapper()
                .withRowKeyField("word")
                .withColumnFields(new Fields("word","count"))
                .withColumnFamily("info");

        /*
         * 给HBaseBolt传入表名、数据映射关系、和HBase的配置信息
         * 表需要预先创建: create 'WordCount','info'
         */
        HBaseBolt hbase = new HBaseBolt("WordCount", mapper)
                .withConfigKey("hbase.conf");

        // 构建Topology
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(DATA_SOURCE_SPOUT, new DataSourceSpout(),1);
        // split
        builder.setBolt(SPLIT_BOLT, new SplitBolt(), 1).shuffleGrouping(DATA_SOURCE_SPOUT);
        // count
        builder.setBolt(COUNT_BOLT, new CountBolt(),1).shuffleGrouping(SPLIT_BOLT);
        // save to HBase
        builder.setBolt(HBASE_BOLT, hbase, 1).shuffleGrouping(COUNT_BOLT);


        // 如果外部传参cluster则代表线上环境启动,否则代表本地启动
        if (args.length > 0 && args[0].equals("cluster")) {
            try {
                StormSubmitter.submitTopology("ClusterWordCountToRedisApp", config, builder.createTopology());
            } catch (AlreadyAliveException | InvalidTopologyException | AuthorizationException e) {
                e.printStackTrace();
            }
        } else {
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("LocalWordCountToRedisApp",
                    config, builder.createTopology());
        }
    }
}