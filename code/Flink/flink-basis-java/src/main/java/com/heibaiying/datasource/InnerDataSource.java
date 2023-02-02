package com.heibaiying.datasource;

import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * @author liuweilong
 * @version 1.0.0
 * @ClassName InnerDataSource.java
 * @Description TODO
 * @createTime 2021年05月03日 22:00:00
 */
public class InnerDataSource {
    public static void main(String[] args) throws Exception {
//        final String filePath = "/Users/lwl/Desktop/person/github/Java-Note/数据中台.md";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
//        env.readFile(new TextInputFormat(new Path(filePath))
//                , filePath, FileProcessingMode.PROCESS_ONCE
//                , 1
//                , BasicTypeInfo.STRING_TYPE_INFO).print();
//        env.fromCollection(Arrays.asList(1, 2, 3, 4, 5)).print();
        env.fromCollection(new CustomIterator(), BasicTypeInfo.INT_TYPE_INFO).print();
        env.execute();
    }
}
