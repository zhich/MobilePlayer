package com.zch.mobileplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 *
 * @author zch 2017-1-14
 */
public class DateUtils {

    /**
     * 默认格式
     */
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 自定义的一些格式
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 日期类型转字符串
     *
     * @param date
     */
    public static String date2Str(Date date) {
        return DEFAULT_DATE_FORMAT.format(date);
    }

    /**
     * 日期类型转字符串
     *
     * @param date
     * @param format
     */
    public static String date2Str(Date date, SimpleDateFormat format) {
        return format.format(date);
    }


    /**
     * 毫秒值转字符串
     *
     * @param timeMillis
     */
    public static String long2Str(long timeMillis) {
        return DEFAULT_DATETIME_FORMAT.format(new Date(timeMillis));
    }

    /**
     * 毫秒值转字符串
     *
     * @param timeMillis
     * @param format
     */
    public static String long2Str(long timeMillis, SimpleDateFormat format) {
        return format.format(new Date(timeMillis));
    }

}
