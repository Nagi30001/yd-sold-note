package com.ydxsj.ydsoldnote.service.impl;

import com.ydxsj.ydsoldnote.config.redis.JedisPoolUtil;
import com.ydxsj.ydsoldnote.service.JedisUtilService;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class JedisUtilServiceImpl implements JedisUtilService {

    private static JedisPoolUtil jedisPoolUtil = new JedisPoolUtil("49.234.210.89", 6379, "rk123321");
    private static Jedis jedis = jedisPoolUtil.borrowJedis();

//    public static  String getii(){
////        jedis
//    }








}
