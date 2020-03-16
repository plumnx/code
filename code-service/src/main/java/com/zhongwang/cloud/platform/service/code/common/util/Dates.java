package com.zhongwang.cloud.platform.service.code.common.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期帮助类
 */
public final class Dates {

    public static final String YYYY_MM_DD_T_HH_MM_SS_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String UTC = "UTC";

    /**
     * 文本转日期
     * @param date
     * @param parsePattern
     * @return
     * @throws ParseException
     */
    public static Date parseDate(String date, String parsePattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(parsePattern);
        return sdf.parse(date);
    }


    /**
     * 日期转文本
     * @param date
     * @param parsePattern
     * @return
     */
    public static String parseText(Date date, String parsePattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(parsePattern);
        return sdf.format(date);
    }

    /**
     * UTC格式字符串转日期
     * @return
     */
    public static Date parseUTCTxtToDate(String utcDateTxt) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(YYYY_MM_DD_T_HH_MM_SS_Z);
//        df.setTimeZone(TimeZone.getTimeZone(UTC));
        return df.parse(utcDateTxt);
    }

}
