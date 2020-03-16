package com.zhongwang.cloud.platform.service.code.common.lock.impl;

import com.zhongwang.cloud.platform.service.code.common.lock.DistributedLocker;
import com.zhongwang.cloud.platform.service.code.common.lock.LockAndReturnMethod;
import com.zhongwang.cloud.platform.service.code.common.lock.LockMethod;
import com.zhongwang.cloud.platform.service.code.config.bean.Lock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@EnableConfigurationProperties({Lock.class})
public class DistributedLockerImpl<T> implements DistributedLocker<T> {

    private final RedissonClient redissonClient;

    @Autowired
    private Lock lock;

    @Value("${spring.redis.prefix:${spring.application.name}}:")
    private String prefix;

    @Autowired
    public DistributedLockerImpl(@Qualifier("redissonSingle") RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void lock(LockMethod lockMethod, String lockKey) throws Exception {
        this.lock(lockMethod, lockKey, lock.getTime());
    }

    @Override
    public void lock(LockMethod lockMethod, String lockKey, int timeout) throws Exception {
        this.lock(lockMethod, lockKey, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void lock(LockMethod lockMethod, String lockKey, int timeout, TimeUnit unit) throws Exception {
        RLock rLock = null;
        try {
//            synchronized(this) {
                rLock = getRLock(lockKey);
                rLock.lock(timeout, unit);
                lockMethod.doMethod();
//            }
        } finally {
            if (rLock != null) {
                rLock.unlock();
            }
        }
    }

    @Override
    public boolean isLocked(String lockKey) {
        RLock lock = getRLock(lockKey);
        return lock.isLocked();
    }

    @Override
    public T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey) throws Exception {
        return this.lockAndReturn(lockAndReturnMethod, lockKey, lock.getTime());
    }

    @Override
    public T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey, int timeout) throws Exception {
        return this.lockAndReturn(lockAndReturnMethod, lockKey, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey, int timeout, TimeUnit unit) throws Exception {
        RLock lock = null;
        try {
//            synchronized(this) {
                lock = getRLock(lockKey);
                lock.lock(timeout, unit);
                T t = lockAndReturnMethod.doMethod();
                return t;
//            }
        } finally {
            if (lock != null) {
                lock.unlock();
            }
        }
    }

    private RLock getRLock(String lockKey) {
        return redissonClient.getLock(prefix + lockKey);
    }

}
