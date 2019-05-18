package com.hzhang.redis.service;

import com.hzhang.redis.User;
import com.hzhang.redis.prefix.RedisKey;
import com.hzhang.redis.utils.RedisUtil;
import com.hzhang.redis.utils.RedisUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class JedisServiceImpl implements JedisService {
    @Autowired
    private RedisUtils redisUtils;
    /**
     * demo
     */
    @Override
    public void baseDemo() {
        // User
        User user = new User("hzhang","123456",28);
        redisUtils.set(RedisKey.redisKey,"userBean",user);
        User resultUser =redisUtils.get(RedisKey.redisKey,"userBean",User.class);
        System.out.println(resultUser);


        // ArrayList<User>
        List<User> userList = new ArrayList<User>(){{
            add(new User("hzhang","123456",28));
            add(new User("xiaowang","123456",27));
            add(new User("xiaoli","123456",286));
            add(new User("xiaoli","123456",22));
        }};
        redisUtils.set(RedisKey.redisKey,"arraylist_user",userList);
        System.out.println(redisUtils.getList(RedisKey.redisKey,"arraylist_user",User.class));

        // Map
        Map<String,Object>  userMap = new HashMap<String,Object>(){{
            put("hzhang","123456");
            put("xiaowang","12313");
        }};
        redisUtils.set(RedisKey.redisKey,"userMap",userMap);
        Map<String,Object> resultUserMap = redisUtils.get(RedisKey.redisKey, "userMap", Map.class);
        System.out.println(resultUserMap);

        // arrayList<Map>>
        List<Map<String,Object>> userMapList = new ArrayList<Map<String,Object>>(){{
            add(new HashMap<String,Object>(){{
                put("username","hzhang");
                put("age","25");
            }});
            add(new HashMap<String,Object>(){{
                put("username","qyq");
                put("age","24");
            }});
        }};
        redisUtils.set(RedisKey.redisKey,"userMapList",userMapList);
        List<Map> resultUserMapList = redisUtils.getList(RedisKey.redisKey, "userMapList", Map.class);
        System.out.println(resultUserMapList);
    }
}
