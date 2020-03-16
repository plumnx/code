//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.exception;

import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;

/**
 * 持久化异常
 */
public class CodePersistentException extends AbstractException implements CodeExceptionDefinition {

    private static final long serialVersionUID = -2511670823647568947L;

    @Override
    public String messageResourceBaseName() {
        return codeCommonMessageResourceBaseName();
    }

    @Override
    public String moduleName() {
        return codeCommonModuleName();
    }

    public CodePersistentException(Enum<?> code) {
        super(code);
    }

    public enum Error {
        DAO_RULE_SERIAL_SAVE_FAIL,
        DAO_QUERY_RULE_SERIAL_FAIL;
    }
}
