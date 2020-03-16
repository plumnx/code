package com.zhongwang.cloud.platform.service.code.exception;

/**
 * 编码服务异常公共定义接口
 */
public interface CodeExceptionDefinition {

    default String codeCommonMessageResourceBaseName() {
        return "exception.code";
    }

    default String codeCommonModuleName() {
        return "CODE";
    }

}
