package com.heibaiying.utils

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object JedisPoolUtil {

  /*创建Jedis连接池*/
  val config = new JedisPoolConfig
  config.setMaxTotal(30)
  config.setMaxIdle(10)
  val jedisPool = new JedisPool(config, "localhost", 6379)


  def getConnection: Jedis = {
    jedisPool.getResource
  }

}
