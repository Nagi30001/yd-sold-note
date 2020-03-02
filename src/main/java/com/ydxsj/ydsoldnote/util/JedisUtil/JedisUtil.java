package com.ydxsj.ydsoldnote.util.JedisUtil;

import com.ydxsj.ydsoldnote.config.redis.JedisPoolUtil;


public class JedisUtil {

    public static String REDIS_HOST;
    public static Integer REDIS_PORT;
    public static String REDIS_PASSWORD;
    public static JedisPoolUtil jedisPoolUtil = new JedisPoolUtil();


    static {
//        ResourceBundle bundle= ResourceBundle.getBundle("application");
//        REDIS_HOST = bundle.getString("jedis.config.host");
//        REDIS_PORT = Integer.valueOf(bundle.getString("jedis.config.port"));
//        REDIS_PASSWORD = bundle.getString("jedis.config.password");
    }






}
