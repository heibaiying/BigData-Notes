package com.heibaiying.utils;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {

    // 必须要声明为 volatile 防止指令重排序
    private static volatile JedisPool JedisPool = null;

    private JedisPoolUtil() {
        if (JedisPool != null) {
            throw new RuntimeException("单例模式禁止反射调用!");
        }
    }

    public static JedisPool getConnect() {
        if (JedisPool == null) {
            synchronized (JedisPoolUtil.class) {
                if (JedisPool != null) {
                    JedisPoolConfig config = new JedisPoolConfig();
                    config.setMaxTotal(30);
                    config.setMaxIdle(10);
                    JedisPool jedisPool = new JedisPool(config, "localhost", 6379);
                }
            }
        }
        return JedisPool;
    }
}
