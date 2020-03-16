package com.zhongwang.cloud.platform.service.code.common.lock;

import java.util.concurrent.TimeUnit;

public interface DistributedLocker<T> {

    void lock(LockMethod lockMethod, String lockKey) throws Exception;

    void lock(LockMethod lockMethod, String lockKey, int timeout) throws Exception;

    void lock(LockMethod lockMethod, String lockKey, int timeout, TimeUnit unit) throws Exception;

    boolean isLocked(String lockKey);

    T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey) throws Exception;

    T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey, int timeout) throws Exception;

    T lockAndReturn(LockAndReturnMethod<T> lockAndReturnMethod, String lockKey, int timeout, TimeUnit unit) throws Exception;

}
