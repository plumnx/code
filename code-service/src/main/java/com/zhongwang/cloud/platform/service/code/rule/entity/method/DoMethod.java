package com.zhongwang.cloud.platform.service.code.rule.entity.method;

@FunctionalInterface
public interface DoMethod<T> {

    T method(String serialUnionValueFormat, String serialUnionValue, String showUnionValueFormat, String showUnionValue) throws Exception;

}
