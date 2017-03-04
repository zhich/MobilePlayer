package com.zch.mobileplayer.entity;

import java.io.Serializable;

/**
 * 音频或视频实体
 * Created by zch on 2017/3/4.
 */

public class MediaItem implements Serializable {
    public static final long serialVersionUID = 1L;

    public String name;//文件在sdcard的名称
    public long duration;//文件总时长
    public long size;//文件总大小
    public String data;//文件的绝对地址
    public String artist;//艺术家
    public String desc;
    public String imageUrl;

}
