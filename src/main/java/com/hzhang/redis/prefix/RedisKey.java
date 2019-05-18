package com.hzhang.redis.prefix;

public class RedisKey extends BasePrefix {
    public static final int expireTime = 60;
    public RedisKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static RedisKey redisKey = new RedisKey(expireTime, "redis");
}
