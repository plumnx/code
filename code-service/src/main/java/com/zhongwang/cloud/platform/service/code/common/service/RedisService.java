package com.zhongwang.cloud.platform.service.code.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public void flushDb(){
        redisTemplate.execute((RedisCallback<Object>) connection -> {
            connection.flushDb();
            return "ok";
        });
    }

    public Object getKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

}
