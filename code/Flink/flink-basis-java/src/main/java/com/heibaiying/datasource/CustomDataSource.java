package com.heibaiying.datasource;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName CustomDataSource.java
 * @Description TODO
 * @createTime 2021年05月03日 22:23:00
 */
public class CustomDataSource {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.addSource(new SourceFunction<Long>() {

            private long count = 0L;
            private volatile boolean isRunning = true;

            @Override
            public void run(SourceContext<Long> ctx) throws Exception {
                if (isRunning && count < 1000) {
                    //将输入发送出去
                    ctx.collect(count);
                    count++;
                }
            }

            @Override
            public void cancel() {
                isRunning = false;
            }
        }).print();
        env.execute();

    }
}
