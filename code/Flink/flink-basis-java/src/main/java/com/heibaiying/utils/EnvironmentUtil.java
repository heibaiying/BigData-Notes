package com.heibaiying.utils;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName EnvironmentUtil.java
 * @Description TODO
 * @createTime 2021年05月04日 09:40:00
 */
public class EnvironmentUtil {
    public static StreamExecutionEnvironment getEnvironment() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        return env;
    }
}
