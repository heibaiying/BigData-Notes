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
        SparkConf conf = new SparkConf().setMaster("local[2]").setAppName("TransformationTest");
        sc = new JavaSparkContext(conf);
    }

    @Test
    public void map() {
        List<Integer> list = Arrays.asList(3, 6, 9, 10, 12, 21);
        sc.parallelize(list).map(x -> x * 10).foreach(System.out::println);
    }


    @After
    public void destroy() {
        sc.close();
    }

}
