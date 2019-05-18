package com.hzhang.redis.prefix;

public interface KeyPrefix {

    int expireSeconds();

    String getPrefix();

}
