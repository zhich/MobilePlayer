package com.zch.mobileplayer.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.zch.mobileplayer.entity.Lyric;
import com.zch.mobileplayer.utils.DensityUtils;

import java.util.ArrayList;

/**
 * 用于显示歌词的View
 * Created by zch on 2017/3/6.
 */

public class LyricView extends TextView {

    private ArrayList<Lyric> mLyricList;//歌词列表
    private Paint mPaint;
    private Paint mWhitePaint;
    private int mWidth;
    private int mHeight;
    private int mIndex;//歌词列表中的索引，是第几句歌词
    private float mTextHeight;//每行的高
    private int mCurPos;//当前播放进度
    private float mSleepTime;//高亮显示的时间或者休眠时间
    private float mTimePoint;//时间戳，什么时刻到高亮哪句歌词

    public LyricView(Context context) {
        this(context, null);
    }

    public LyricView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setLyricList(ArrayList<Lyric> lyricList) {
        this.mLyricList = lyricList;
    }

    private void initView(Context context) {
        mTextHeight = DensityUtils.dp2px(context, 18);

        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        mPaint.setTextSize(DensityUtils.sp2px(context, 16));
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);//设置居中对齐

        mWhitePaint = new Paint();
        mWhitePaint.setColor(Color.WHITE);
        mWhitePaint.setTextSize(DensityUtils.sp2px(context, 16));
        mWhitePaint.setAntiAlias(true);
        mWhitePaint.setTextAlign(Paint.Align.CENTER);//设置居中对齐
    }

}
