package com.heibaiying.operatorstate;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


public class OperatorStateJob {

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 开启检查点机制
        env.enableCheckpointing(1000);
        // 设置并行度为1
        DataStreamSource<Tuple2<String, Long>> tuple2DataStreamSource = env.setParallelism(1).fromElements(
                Tuple2.of("a", 50L), Tuple2.of("a", 80L), Tuple2.of("a", 400L),
                Tuple2.of("a", 100L), Tuple2.of("a", 200L), Tuple2.of("a", 200L),
                Tuple2.of("b", 100L), Tuple2.of("b", 200L), Tuple2.of("b", 200L),
                Tuple2.of("b", 500L), Tuple2.of("b", 600L), Tuple2.of("b", 700L));
        tuple2DataStreamSource
                .flatMap(new ThresholdWarning(100L, 3))
                .printToErr();
        env.execute("Managed Keyed State");
    }

}
