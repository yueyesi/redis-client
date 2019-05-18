[toc]
## 客户端选择
1. jedis
2. spring-boot-starter-data-redis

前者是redis官方推荐的客户端，后者是spring提供的，对jedis的简单封装

前者性能高，推荐使用前者

## redis基本操作
### jedis客户端、连接池配置
#### application.properties
```
#redis配置开始
# Redis数据库索引（默认为0）
spring.redis.database=0
# Redis服务器地址
spring.redis.host=39.108.75.44
# Redis服务器连接端口
spring.redis.port=6379
# 连接池最大连接数（使用负值表示没有限制）
spring.redis.jedis.pool.max-active=1024
# 连接池最大阻塞等待时间（使用负值表示没有限制）
spring.redis.jedis.pool.max-wait=10000
# 连接池中的最大空闲连接
spring.redis.jedis.pool.max-idle=200
# 连接池中的最小空闲连接
spring.redis.jedis.pool.min-idle=0
# 连接超时时间（毫秒）
spring.redis.timeout=10000
#redis配置结束
spring.redis.block-when-exhausted=true
```
#### RedisConfig
```
@Configuration
@PropertySource("classpath:application.properties")
public class RedisConfig {

    private static Logger log = LoggerFactory.getLogger(RedisConfig.class);
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.port}")
    private int port;

    @Value("${spring.redis.timeout}")
    private int timeout;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private long maxWaitMillis;

//    @Value("${spring.redis.password}")
//    private String password;

    @Value("${spring.redis.block-when-exhausted}")
    private boolean blockWhenExhausted;

    @Bean
    public JedisPool redisPoolFactory() throws Exception {
        log.info("JedisPool注入成功！！");
        log.info("redis地址：" + host + ":" + port);
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        // 连接耗尽时是否阻塞, false报异常,ture阻塞直到超时, 默认true
        jedisPoolConfig.setBlockWhenExhausted(blockWhenExhausted);
        // 是否启用pool的jmx管理功能, 默认true
        jedisPoolConfig.setJmxEnabled(true);
        JedisPool jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout);
        return jedisPool;
    }
}

```
### set、get对象
将对象序列化为json存储到redis
```
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
```
### set、get数组
```

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
```