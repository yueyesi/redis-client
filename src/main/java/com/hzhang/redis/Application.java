

package com.hzhang.redis;


import com.hzhang.redis.lock.RedisLock;
import com.hzhang.redis.service.JedisService;
import com.hzhang.redis.utils.RedisUtil;
import com.hzhang.redis.utils.RedisUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;


@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		ApplicationContext applicationContext =SpringApplication.run(Application.class, args);
//        JedisService jedisService = applicationContext.getBean(JedisService.class);
//         jedisService.demo();

        // 分布式锁测试
        RedisLock redisLock = applicationContext.getBean(RedisLock.class);
        String lockValue =redisLock.getLock("hzhang-lock1",10000);
        System.out.println(lockValue);

        String l =redisLock.getLock("hzhang-lock1",10000);
        System.out.println(l);

    }
}