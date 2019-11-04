package com.heibaiying.keyedstate;

import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.guava18.com.google.common.collect.Lists;
import org.apache.flink.util.Collector;

import java.util.ArrayList;
import java.util.List;

public class ThresholdWarningWithTTL extends RichFlatMapFunction<Tuple2<String, Long>, Tuple2<String, List<Long>>> {

    private transient ListState<Long> abnormalData;
    private Long threshold;
    private Integer numberOfTimes;

    ThresholdWarningWithTTL(Long threshold, Integer numberOfTimes) {
        this.threshold = threshold;
        this.numberOfTimes = numberOfTimes;
    }

    @Override
    public void open(Configuration parameters) {
        StateTtlConfig ttlConfig = StateTtlConfig
                // 设置有效期为 10 秒
                .newBuilder(Time.seconds(10))
                // 设置有效期更新规则，这里设置为当创建和写入时，都重置其有效期到规定的10秒
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                // 设置只要值过期就不可见，另外一个可选值是 ReturnExpiredIfNotCleanedUp，代表即使值过期了，但如果还没有被删除，就是可见的
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();
        ListStateDescriptor<Long> descriptor = new ListStateDescriptor<>("abnormalData", Long.class);
        descriptor.enableTimeToLive(ttlConfig);
        this.abnormalData = getRuntimeContext().getListState(descriptor);
    }

    @Override
    public void flatMap(Tuple2<String, Long> value, Collector<Tuple2<String, List<Long>>> out) throws Exception {
        Long inputValue = value.f1;
        if (inputValue >= threshold) {
            abnormalData.add(inputValue);
        }
        ArrayList<Long> list = Lists.newArrayList(abnormalData.get().iterator());
        if (list.size() >= numberOfTimes) {
            out.collect(Tuple2.of(value.f0 + " 超过指定阈值 ", list));
            abnormalData.clear();
        }
    }
}
