package com.heibaiying.transformation;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static com.heibaiying.utils.EnvironmentUtil.getEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName FilterDemo.java
 * @Description TODO
 * @createTime 2021年05月04日 09:39:00
 */
public class FilterDemo {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = getEnvironment();
        env.fromElements(1, 2, 3, 4, 5).filter(x -> x > 3).print();
    }
}
