package com.hzhang.redis.lock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;

import java.util.List;
import java.util.UUID;

@Component
public class RedisLock {
    @Autowired
    private JedisPool jedisPool;

    /**
     * 获得锁
     * @param key
     * @param timeOut
     * @return
     */
    public String getLock(String key, int timeOut) {
        String value = UUID.randomUUID().toString();
        long endTime = System.currentTimeMillis() + timeOut;
        try {
            // 在一定时间内一直去尝试获得锁
            while (System.currentTimeMillis() < endTime) {
                Jedis jedis = jedisPool.getResource();
                if (jedis.setnx(key, value) == 1) {
                    jedis.expire(key,timeOut);
                    return value;
                }
                // ttl检测过期时间，未设置则去设置
                if(jedis.ttl(key)== -1){
                    jedis.expire(key,timeOut);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * 释放锁
     * @param key
     * @param value
     * @return
     */
    public  boolean releaseLock(String key,String value){
        Jedis jedis = jedisPool.getResource();
        while (true){
            jedis.watch(key);
            // watch用来监控多个key,一旦key被修改或删除， watch后面的事务将不会执行
            if(value.equals(jedis.get(key))){
                Transaction transaction =jedis.multi();
                transaction.del(key);
                List<Object> result= transaction.exec();
                if(result==null){
                    continue;
                }
                return true;
            }
            jedis.unwatch();
        }
    }
}
