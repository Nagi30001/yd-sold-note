package com.ydxsj.ydsoldnote.config.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolUtil {
    private static JedisPoolUtil myRedisPool = null;

    //redis 自带的 连接池
    private static JedisPool jedisPool = null;

    public JedisPoolUtil(String ipAddress, int port, String password) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(RedisPoolConfig.CONNECTION_MAX_TOTAL);
        jedisPoolConfig.setMaxIdle(RedisPoolConfig.CONNECTION_MAX_IDLE);
        jedisPoolConfig.setMinIdle(RedisPoolConfig.CONNECTION_MIN_IDLE);
        jedisPoolConfig.setMaxWaitMillis(RedisPoolConfig.CONNECTION_MAX_WAIT);
        jedisPoolConfig.setTestOnBorrow(RedisPoolConfig.TEST_ON_BORROW);
        jedisPoolConfig.setTestOnReturn(RedisPoolConfig.TEST_ON_RETURN);

        if ("".equals(password.trim())) {
            //无密码
            jedisPool = new JedisPool(jedisPoolConfig, ipAddress, port);
        } else {
            //有密码
            int waitTime = 10000;
            jedisPool = new JedisPool(jedisPoolConfig, ipAddress, port, waitTime, password);
        }
    }

    /**
     * 双重锁定获取 my redis pool 实例
     * 在生成的过程中 生成了 redis pool 实例
     * @param ipAddress
     * @param port
     * @param password
     * @return
     */
    public static JedisPoolUtil getRedisPoolInstance(String ipAddress, int port, String password) {
        if (myRedisPool == null) {
            synchronized (JedisPoolUtil.class) {
                if (myRedisPool == null) {
                    myRedisPool = new JedisPoolUtil(ipAddress, port, password);
                }
            }
        }
        return myRedisPool;
    }

    /**
     * 获取JedisPool 实例
     * @return
     */
    public static JedisPool getJedisPool(){
        return jedisPool;
    }

    /**
     * 获取一个jedis
     * @return
     */
    public Jedis borrowJedis(){
        return jedisPool.getResource();
    }

    /**
     * 返还一个jedis
     * @param jedis
     */
    public void returnJedis(Jedis jedis){
        jedis.close();
    }


}
