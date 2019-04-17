package com.heibaiying.component;

import org.apache.storm.redis.bolt.AbstractRedisBolt;
import org.apache.storm.redis.common.config.JedisPoolConfig;
import org.apache.storm.redis.common.mapper.RedisDataTypeDescription;
import org.apache.storm.redis.common.mapper.RedisStoreMapper;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Tuple;
import redis.clients.jedis.JedisCommands;


/**
 * 自定义RedisBolt 利用Redis的哈希数据结构的hincrby key field功能进行词频统计
 */
public class RedisCountStoreBolt extends AbstractRedisBolt {

    private final RedisStoreMapper storeMapper;
    private final RedisDataTypeDescription.RedisDataType dataType;
    private final String additionalKey;

    public RedisCountStoreBolt(JedisPoolConfig config, RedisStoreMapper storeMapper) {
        super(config);
        this.storeMapper = storeMapper;
        RedisDataTypeDescription dataTypeDescription = storeMapper.getDataTypeDescription();
        this.dataType = dataTypeDescription.getDataType();
        this.additionalKey = dataTypeDescription.getAdditionalKey();
    }

    @Override
    protected void process(Tuple tuple) {
        String key = storeMapper.getKeyFromTuple(tuple);
        String value = storeMapper.getValueFromTuple(tuple);

        JedisCommands jedisCommand = null;
        try {
            jedisCommand = getInstance();
            if (dataType == RedisDataTypeDescription.RedisDataType.HASH) {
                jedisCommand.hincrBy(additionalKey, key, Long.valueOf(value));
            } else {
                throw new IllegalArgumentException("Cannot process such data type for Count: " + dataType);
            }

            collector.ack(tuple);
        } catch (Exception e) {
            this.collector.reportError(e);
            this.collector.fail(tuple);
        } finally {
            returnInstance(jedisCommand);
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

    }
}