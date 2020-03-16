package com.zhongwang.cloud.platform.service.code.common.util;

import com.zhongwang.cloud.platform.service.code.common.CodeConst;

import static com.zhongwang.cloud.platform.service.code.common.CodeConst.CODE_CACHE_SERIAL_TITLE;

public class Caches {

    /**
     * 生成流水号的 redis 设置
     */
    public static class CodeSerial {

        /**
         * 分布式锁的key值
         * @return
         */
        public static String key(String... arguments) {
            return String.join(CodeConst.SYS_COMMON_STATUS.SYMBOL_1, arguments);
        }

        public static String forCurrentHash(String key) {
            return CODE_CACHE_SERIAL_TITLE + key;
        }

        public static String forCurrentHashKey(String key) {
            return key + ":1";
        }

        public static String forLimitHashKey(String key) {
            return key + ":2";
        }
    }

}
