package com.zch.mobileplayer.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

/**
 * 时间工具类
 * Created by zch on 2017/3/4.
 */

public class TimeUtils {

    /**
     * 把毫秒转换成 1:20:30 这种形式
     *
     * @param timeMs
     * @return
     */
    public static String str2Time(long timeMs) {
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());

        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;

        long minutes = (totalSeconds / 60) % 60;

        long hours = totalSeconds / 3600;

        formatBuilder.setLength(0);
        if (hours > 0) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds)
                    .toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 得到系统时间
     *
     * @return
     */
    public static String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }
}
