package com.ydxsj.ydsoldnote.config.redis;

public class RedisPoolConfig {
    //最大连接数
    public static int CONNECTION_MAX_TOTAL = 100;

    //最大空闲连接数
    public static int CONNECTION_MAX_IDLE = 50;

    //初始化连接数（最小空闲连接数）
    public static int CONNECTION_MIN_IDLE = 10;

    //等待连接的最大等待时间
    public static int CONNECTION_MAX_WAIT = 2000;

    //borrow前 是否进行alidate操作，设置为true意味着borrow的均可用
    public static boolean TEST_ON_BORROW = true;

    //return前 是否进行alidate操作
    public static boolean TEST_ON_RETURN = true;
}
