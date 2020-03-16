package com.zhongwang.cloud.platform.service.code.common.lock;

@FunctionalInterface
public interface LockAndReturnMethod<T> {

    T doMethod() throws Exception;

}
