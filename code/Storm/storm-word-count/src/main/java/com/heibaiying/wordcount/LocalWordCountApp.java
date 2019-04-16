package com.heibaiying.wordcount;

import com.heibaiying.wordcount.component.CountBolt;
import com.heibaiying.wordcount.component.DataSourceSpout;
import com.heibaiying.wordcount.component.SplitBolt;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

public class LocalWordCountApp {

    public static void main(String[] args) {
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("DataSourceSpout", new DataSourceSpout());
        // 指明将 DataSourceSpout 的数据发送到 SplitBolt 中处理
        builder.setBolt("SplitBolt", new SplitBolt()).shuffleGrouping("DataSourceSpout");
        //  指明将 SplitBolt 的数据发送到 CountBolt 中 处理
        builder.setBolt("CountBolt", new CountBolt()).shuffleGrouping("SplitBolt");

        // 创建本地集群用于测试 这种模式不需要本机安装storm,直接运行该Main方法即可
        LocalCluster cluster = new LocalCluster();
        cluster.submitTopology("LocalWordCountApp",
                new Config(), builder.createTopology());
    }

}
