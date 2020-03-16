package com.zhongwang.cloud.platform.service.code.common.lock;

@FunctionalInterface
public interface LockMethod {

    void doMethod() throws Exception;

}
