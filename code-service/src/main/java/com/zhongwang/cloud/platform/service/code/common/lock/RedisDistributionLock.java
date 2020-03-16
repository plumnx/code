package com.zhongwang.cloud.platform.service.code.common.lock;

import com.zhongwang.cloud.platform.service.code.config.bean.Lock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties(Lock.class)
@Slf4j
public class RedisDistributionLock {

    @Autowired
    private Lock distributionLock;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 加锁
     *
     * @param lockKey
     * @return
     */
    public Long lock(String lockKey) throws InterruptedException {
        final Random r = new Random();
        while (true) {
            Long lockedTime = System.currentTimeMillis() + distributionLock.getTime() + 1;
            if ((Boolean) redisTemplate.execute((RedisCallback<Boolean>) connection -> connection.setNX(lockKey.getBytes(), lockedTime.toString().getBytes()))) {
                redisTemplate.expire(lockKey, distributionLock.getTime(), TimeUnit.MILLISECONDS);
                return lockedTime;
            } else {
                Long currExpireTime = (Long) redisTemplate.opsForValue().get(lockKey);
                if (currExpireTime != null && currExpireTime < System.currentTimeMillis()) {
                    Long lockedTimeInRedis = (Long) redisTemplate.opsForValue().getAndSet(lockKey, lockedTime);
                    if (lockedTimeInRedis != null && lockedTimeInRedis.equals(currExpireTime)) {
                        redisTemplate.expire(lockKey, distributionLock.getTime(), TimeUnit.MILLISECONDS);
                        return lockedTime;
                    }
                }
            }
            try {
                Thread.sleep(1, r.nextInt(500));
            } catch (InterruptedException e) {
                log.error(e.getMessage());
                throw e;
            }
        }
    }

    /**
     * 解锁
     *
     * @param lockKey
     * @param lockVal
     */
    public void unlock(String lockKey, long lockVal) {
        Long currExpireTime = (Long) redisTemplate.opsForValue().get(lockKey);
        if (currExpireTime != null && currExpireTime == lockVal) {
            redisTemplate.delete(lockKey);
        }
    }

}