package com.zch.mobileplayer.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 展示toast的工具类
 * Created by zch on 2017/3/3.
 */
public class ToastUtils {

    /**
     * 长时间的toast提醒用户
     *
     * @param context 上下文对象。
     * @param msg     提醒的信息。
     */
    public static void showToastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * 短时间的toast提醒用户
     *
     * @param context 上下文对象。
     * @param msg     提醒的信息。
     */
    public static void showToastShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

}
