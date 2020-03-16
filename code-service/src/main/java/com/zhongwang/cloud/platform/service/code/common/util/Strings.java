package com.zhongwang.cloud.platform.service.code.common.util;

public class Strings {

    public static String like(String text) {
        if(!com.google.common.base.Strings.isNullOrEmpty(text)) {
            return "%" + text + "%";
        }
        return "";
    }

}
