//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.zhongwang.cloud.platform.service.code.exception;

import com.zhongwang.cloud.platform.bamboo.common.exception.AbstractException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.stream.Collectors;

import static com.zhongwang.cloud.platform.service.code.exception.CodeInvalidParametersException.Error.PROPERTY_NULL_OR_FORMAT_WRONG;

/**
 * 验证参数格式
 */
public class CodeInvalidParametersException extends AbstractException implements CodeExceptionDefinition {

    private static final long serialVersionUID = 7492256284473132709L;

    @Override
    public String messageResourceBaseName() {
        return codeCommonMessageResourceBaseName();
    }

    @Override
    public String moduleName() {
        return codeCommonModuleName();
    }

    public CodeInvalidParametersException(List<FieldError> fieldErrors) {
        super(PROPERTY_NULL_OR_FORMAT_WRONG,
                fieldErrors.stream().
                        map(DefaultMessageSourceResolvable::getDefaultMessage).
                        collect(Collectors.joining(", ")));
    }

    public CodeInvalidParametersException(String message) {
        super(PROPERTY_NULL_OR_FORMAT_WRONG, message);
    }

    public CodeInvalidParametersException(String message, Throwable cause, Object... arguments) {
        super(PROPERTY_NULL_OR_FORMAT_WRONG, message, cause, arguments);
    }

    public CodeInvalidParametersException(Error error) {
        super(error);
    }

    public CodeInvalidParametersException(Error error, String message) {
        super(error, message);
    }

    public CodeInvalidParametersException(Error error, String message, Object... arguments) {
        super(error, message, arguments);
    }

    public static void check(boolean condition, Error error, String message) throws CodeInvalidParametersException {
        if (!condition)
            throw new CodeInvalidParametersException(error, message);
    }

    public static void check(boolean condition, Error error) throws CodeInvalidParametersException {
        if (!condition)
            throw new CodeInvalidParametersException(error);
    }

    public static void check(boolean condition, Error error, String message, Object... arguments) throws CodeInvalidParametersException {
        if (!condition)
            throw new CodeInvalidParametersException(error, message, arguments);
    }

    public static void check(boolean condition, AbstractException abstractException) throws AbstractException {
        if (!condition) {
            throw abstractException;
        }
    }

    public enum Error {
        PROPERTY_NULL_OR_FORMAT_WRONG,
        REQUEST_PARAMETERS_WRONG_OR_EMPTY,

        RULE_SYSTEM_CODE_NOT_EXIST,

        RULE_DETAIL_NOT_EMPTY,
        RULE_DETAIL_SECTION_TYPE_NOT_CORRECT,
        RULE_DETAIL_SECTION_VALUE_NOT_CORRECT,
        RULE_DETAIL_SECTION_LENGTH_NOT_CORRECT,
        RULE_DETAIL_SUPPLY_TYPE_NOT_CORRECT,
        RULE_DETAIL_SUPPLY_CHAR_NOT_CORRECT,
        RULE_DETAIL_DATE_FORMAT_NOT_CORRECT,
        RULE_DETAIL_FLAG_SERIAL_NOT_CORRECT,
        RULE_DETAIL_FLAG_SHOW_NOT_CORRECT,
        RULE_DETAIL_ORDER_NOT_RIGHT,
        RULE_DETAIL_NOT_CORRECT,
        RULE_DETAIL_SERIAL_ONLY_ONE,
    }

}
