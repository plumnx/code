//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.exception;

import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;

/**
 * 编码格式生成异常
 */
public class CodeRuleBuilderException extends AbstractException implements CodeExceptionDefinition {

    private static final long serialVersionUID = 5246201568502343853L;

    @Override
    public String messageResourceBaseName() {
        return codeCommonMessageResourceBaseName();
    }

    @Override
    public String moduleName() {
        return codeCommonModuleName();
    }

    public CodeRuleBuilderException(Enum<?> code) {
        super(code);
    }

    public enum Error {
        NOT_FOUND_SERIAL_RULE,
        NOT_FOUND_RULE_BUILDER,
        NOT_FOUND_RULE_DATE_FORMAT,
        NOT_FOUND_RULE_VALUE;
    }
}
