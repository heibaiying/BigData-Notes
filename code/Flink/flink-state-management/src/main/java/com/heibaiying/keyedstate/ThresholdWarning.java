package com.heibaiying.keyedstate;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class ThresholdWarning extends RichFlatMapFunction<Tuple2<String, Long>, Tuple2<String, List<Long>>> {

    // 通过ListState来存储非正常数据的状态
    private transient ListState<Long> abnormalData;
    // 需要监控阈值
    private Long threshold;
    // 达到阈值多少次后触发报警
    private Integer numberOfTimes;

    ThresholdWarning(Long threshold, Integer numberOfTimes) {
        this.threshold = threshold;
        this.numberOfTimes = numberOfTimes;
    }

    @Override
    public void open(Configuration parameters) {
        // 通过状态名称(句柄)获取状态实例，如果不存在则会自动创建
        abnormalData = getRuntimeContext().getListState(new ListStateDescriptor<>("abnormalData", Long.class));
    }

    @Override
    public void flatMap(Tuple2<String, Long> value, Collector<Tuple2<String, List<Long>>> out) throws Exception {
        Long inputValue = value.f1;
        // 如果输入值超过阈值，则记录该次不正常的数据信息
        if (inputValue >= threshold) {
            abnormalData.add(inputValue);
        }
        ArrayList<Long> list = Lists.newArrayList(abnormalData.get().iterator());
        // 如果不正常的数据出现达到一定次数，则输出报警信息
        if (list.size() >= numberOfTimes) {
            out.collect(Tuple2.of(value.f0 + " 超过指定阈值 ", list));
            // 报警信息输出后，清空暂存的状态
            abnormalData.clear();
        }
    }
}
