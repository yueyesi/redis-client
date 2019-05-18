package com.hzhang.redis.utils;

import com.alibaba.fastjson.JSON;
import com.hzhang.redis.prefix.KeyPrefix;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Redis工具类
 */
@Component
public class RedisUtils {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(RedisUtils.class);
    @Autowired
    private JedisPool jedisPool;

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    /**
     * 对象转化为string存储
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }

    /**
     * string转化为bean
     *
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

    /**
     * String转化为List<Bean>
     *
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */

    public static <T> List<T> stringToArrayBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return new ArrayList<T>();
        }
        return JSON.parseArray(str, clazz);
    }

    /**
     * 获取arrayList<bean>
     */
    public <T> List<T> getList(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            String str = jedis.get(realKey);
            return stringToArrayBean(str, clazz);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 获取当个对象
     */
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 设置对象
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 设置失效时间
     *
     * @param key
     * @param value
     * @return
     */
    public Long setnx(String key, String value) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.setnx(key, value);
        } catch (Exception e) {
            log.error("expire key:{} error", key, e);
            jedisPool.returnResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;

    }

    /**
     * 设置key的有效期，单位是秒
     *
     * @param key
     * @param exTime
     * @return
     */
    public Long expire(String key, int exTime) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.expire(key, exTime);
        } catch (Exception e) {
            log.error("expire key:{} error", key, e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;
    }


    public String getset(String key, String value) {
        Jedis jedis = null;
        String result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.getSet(key, value);
        } catch (Exception e) {
            log.error("expire key:{} error", key, e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;
    }


    /**
     * 判断key是否存在
     */
    public <T> boolean exists(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除
     */
    public boolean delete(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + ":" + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    public Long del(String key) {
        Jedis jedis = null;
        Long result = null;
        try {
            jedis = jedisPool.getResource();
            result = jedis.del(key);
        } catch (Exception e) {
            log.error("del key:{} error", key, e);
            jedisPool.returnBrokenResource(jedis);
            return result;
        }
        jedisPool.returnResource(jedis);
        return result;
    }


    public boolean delete(KeyPrefix prefix) {
        if (prefix == null) {
            return false;
        }
        List<String> keys = scanKeys(prefix.getPrefix());
        if (keys == null || keys.size() <= 0) {
            return true;
        }
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.del(keys.toArray(new String[0]));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    public List<String> scanKeys(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            List<String> keys = new ArrayList<String>();
            String cursor = "0";
            ScanParams sp = new ScanParams();
            sp.match("*" + key + "*");
            sp.count(100);
            do {
                ScanResult<String> ret = jedis.scan(cursor, sp);
                List<String> result = ret.getResult();
                if (result != null && result.size() > 0) {
                    keys.addAll(result);
                }
                //再处理cursor
                cursor = ret.getStringCursor();
            } while (!cursor.equals("0"));
            return keys;
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


}

