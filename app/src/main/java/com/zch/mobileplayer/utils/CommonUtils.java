package com.zch.mobileplayer.utils;

import android.content.Context;
import android.net.TrafficStats;

/**
 * 通用工具类
 * Created by zch on 2017/3/4.
 */

public class CommonUtils {

    private static long lastTotalRxBytes = 0;
    private static long lastTimeStamp = 0;

    /**
     * 判断是否是网络的资源
     *
     * @param uri
     * @return
     */
    public static boolean isNetUri(String uri) {
        if (null == uri) {
            return false;
        }
        if (uri.toLowerCase().startsWith("http") || uri.toLowerCase().startsWith("rtsp") || uri.toLowerCase().startsWith("mms")) {
            return true;
        }
        return false;
    }

    /**
     * 得到网络速度
     * 每隔两秒调用一次
     *
     * @param context
     * @return
     */
    public static String getNetSpeed(Context context) {
        String netSpeed = "0 kb/s";
        long nowTotalRxBytes = TrafficStats.getUidRxBytes(context.getApplicationInfo().uid) == TrafficStats.UNSUPPORTED ? 0 : (TrafficStats.getTotalRxBytes() / 1024);//转为KB;
        long nowTimeStamp = System.currentTimeMillis();
        long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));//毫秒转换

        lastTimeStamp = nowTimeStamp;
        lastTotalRxBytes = nowTotalRxBytes;
        netSpeed = String.valueOf(speed) + " kb/s";
        return netSpeed;
    }

}
