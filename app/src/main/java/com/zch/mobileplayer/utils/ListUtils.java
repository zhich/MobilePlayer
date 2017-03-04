package com.zch.mobileplayer.utils;

import java.util.List;

/**
 * List工具类
 * Created by zch on 2017/3/4.
 */

public class ListUtils {

    /**
     * 判断List是否为空
     *
     * @param sourceList
     * @param <V>
     * @return
     */
    public static <V> boolean isEmpty(List<V> sourceList) {
        return (null == sourceList || sourceList.size() == 0);
    }

}
