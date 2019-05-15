package rdd.java;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;


public class TransformationTest {


    private static JavaSparkContext sc = null;


    @Before
    public void prepare() {
        SparkConf conf = new SparkConf().setAppName("TransformationTest").setMaster("local[2]");
        sc = new JavaSparkContext(conf);
    }

    @Test
    public void map() {
        List<Integer> list = Arrays.asList(3, 6, 9, 10, 12, 21);
        /*
         * 不要使用方法引用的形式 : System.out::println , 否则会抛出下面的异常:
         * org.apache.spark.SparkException: Task not serializable
         * Caused by: java.io.NotSerializableException: java.io.PrintStream
         * 这是由于Spark程序中map、foreach等算子内部引用了类成员函数或变量时,需要该类所有成员都支持序列化,
         * 如果该类某些成员变量不支持序列化，就会抛出上面的异常
         */
        sc.parallelize(list).map(x -> x * 10).foreach(x -> System.out.println(x));
    }


    @After
    public void destroy() {
        sc.close();
    }

}
