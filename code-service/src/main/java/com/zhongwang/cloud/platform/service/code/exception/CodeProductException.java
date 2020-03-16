//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.exception;

import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;

import static com.zhongwang.cloud.platform.service.code.exception.CodeProductException.Error.PRODUCT_NULL;

/**
 * 验证参数格式
 */
public class CodeProductException extends AbstractException implements CodeExceptionDefinition {

    private static final long serialVersionUID = 8736657106852381661L;

    @Override
    public String messageResourceBaseName() {
        return codeCommonMessageResourceBaseName();
    }

    @Override
    public String moduleName() {
        return codeCommonModuleName();
    }

    public CodeProductException(String message) {
        super(PRODUCT_NULL, message);
    }

    public CodeProductException(String message, Throwable cause, Object... arguments) {
        super(PRODUCT_NULL, message, cause, arguments);
    }

    public enum Error {
        PRODUCT_NULL;
    }

}
