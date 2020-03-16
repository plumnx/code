package com.zhongwang.cloud.platform.service.code.common;

import lombok.Getter;

import java.util.stream.Stream;

/**
 * 静态类
 */
public class CodeConst {

    /**
     * 系统变量枚举
     */
    public enum SysVariableEnum {
        CURRENT_DATE("system.current.date", "系统日期");

        private String code;
        private String name;

        SysVariableEnum(String code, String name) {
            this.code = code;
            this.name = name;
        }

        public String getCode() {
            return this.code;
        }

        public String getName() {
            return this.name;
        }

    }

    /**
     * 规则占位符
     */
    public static final class RULE_TAG {
        // 显示规则占位符
        public static final String IS_SHOW_NOT_SERIAL = "<SHOW_CODE>";
        // 流水规则占位符
        public static final String RULE_TYPE_IS_SERIAL = "<SERIAL_CODE>";
        // 隐藏流水编码
        public static final String EMPTY_SHOW_UNION_VALUE = "<SHOW_UNION_VALUE_IS_EMPTY>";
        // 隐藏流水编码格式
        public static final String EMPTY_SHOW_UNION_VALUE_FORMAT = "<SHOW_UNION_VALUE_FORMAT_IS_EMPTY>";
    }

    /**
     * 补位方式
     */
    public enum SupplyType {
        NO_TRIM("0"),
        LEFT_TRIM("1"),
        RIGHT_TRIM("2");

        @Getter
        private String value;

        SupplyType(String value) {
            this.value = value;
        }

        public static boolean isExist(String value) {
            return of(value) != null;
        }

        public static SupplyType of(String value) {
            return Stream.of(values()).filter((trimMethod) -> trimMethod.getValue().equals(value)).findFirst().orElse(null);
        }

    }

    /**
     * 是否流水
     */
    public enum IsSerial {
        YES(1),
        NO(0);

        @Getter
        private int value;

        IsSerial(int value) {
            this.value = value;
        }

        public static boolean isExist(int value) {
            return of(value) != null;
        }

        public static IsSerial of(int value) {
            return Stream.of(values()).filter((isSerial) -> isSerial.getValue() == value).findFirst().orElse(null);
        }
    }

    /**
     * 是否显示
     */
    public enum IsShow {
        YES(1),
        NO(0);

        @Getter
        private int value;

        IsShow(int value) {
            this.value = value;
        }

        public static boolean isExist(int value) {
            return of(value) != null;
        }

        public static IsShow of(int value) {
            return Stream.of(values()).filter((isShow) -> isShow.getValue() == value).findFirst().orElse(null);
        }
    }

    public enum SystemCode {
        MES;

        public static boolean isExist(String systemCode) {
            return Stream.of(values()).anyMatch(systemCodeEnum -> systemCodeEnum.toString().equals(systemCode));
        }

    }

    /**
     * 系统标志位
     */
    public static final class SYS_COMMON_STATUS {
        public static final Short VALID = (short) 1;
        public static final Short INVALID = (short) 0;
        public static final Short DELETE = (short) 1;
        public static final Short NOT_DELETE = (short) 0;
        public static final Long D_FLAG_SORT = (long) 999;
        public static final String STR_TRUE = "true";
        public static final String STR_FALSE = "true";
        public static final Short NOT_RECEIVE = (short) 0;
        public static final Short RECEIVED = (short) 1;
        public static final String SYMBOL_1 = "-";
        public static final String SYMBOL_2 = "0";
        public static final String ORDER_SORT_DEFAULT = "modifyTime";
    }

    public static final String CODE_CACHE_HEADER_TITLE = "code";
    public static final String CODE_CACHE_SERIAL_TITLE = "code-serial-";

    public enum CodeSerialStrategy {
        DB,
        DB_CACHE,
        CACHE;

        public static boolean isCache(String enumName) {
            if (CodeSerialStrategy.CACHE.name().equals(enumName)) {
                return true;
            }
            return false;
        }

        public static CodeSerialStrategy of(String enumName) {
            return Stream.of(CodeSerialStrategy.values()).filter(codeSerialStrategy -> {
                return codeSerialStrategy.name().equalsIgnoreCase(enumName);
            }).findAny().orElse(null);
        }

    }

}
