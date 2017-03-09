package com.zch.mobileplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.zch.mobileplayer.entity.Lyric;
import com.zch.mobileplayer.utils.DensityUtils;
import com.zch.mobileplayer.utils.ListUtils;

import java.util.ArrayList;

/**
 * 用于显示歌词的View
 * Created by zch on 2017/3/6.
 */

public class LyricView extends android.support.v7.widget.AppCompatTextView {

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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (ListUtils.isEmpty(mLyricList)) {
            canvas.drawText("没有歌词", mWidth / 2, mHeight / 2, mPaint);
            return;
        }
        float push = 0;//往上推移
        if (mSleepTime == 0) {
            push = 0;
        } else {
            //平移
            //这一句所花的时间 ：休眠时间 = 移动的距离 ： 总距离（行高）
            //移动的距离 =  (这一句所花的时间 ：休眠时间)* 总距离（行高）
            float delta = ((mCurPos - mTimePoint) / mSleepTime) * mTextHeight;

            //屏幕的的坐标 = 行高 + 移动的距离
            push = mTextHeight + delta;
        }
        canvas.translate(0, -push);

        //绘制歌词
        //绘制当前句
        String currentText = mLyricList.get(mIndex).content;
        canvas.drawText(currentText, mWidth / 2, mHeight / 2, mPaint);

        //绘制前面部分
        float tempY = mHeight / 2;//Y轴的中间坐标
        for (int i = mIndex - 1; i >= 0; i--) {
            String preContent = mLyricList.get(i).content;
            tempY -= mTextHeight;
            if (tempY < 0) {
                break;
            }
            canvas.drawText(preContent, mWidth / 2, tempY, mWhitePaint);
        }

        //绘制后面部分
        tempY = mHeight / 2;//Y轴的中间坐标
        for (int i = mIndex + 1, size = mLyricList.size(); i < size; i++) {
            String nextContent = mLyricList.get(i).content;
            tempY += mTextHeight;
            if (tempY > mHeight) {
                break;
            }
            canvas.drawText(nextContent, mWidth / 2, tempY, mWhitePaint);
        }
    }

    /**
     * 根据当前播放的位置，找出该高亮显示哪句歌词
     *
     * @param currentPosition
     */
    public void setHighligtLyric(int currentPosition) {
        this.mCurPos = currentPosition;
        if (ListUtils.isEmpty(mLyricList)) {
            return;
        }
        for (int i = 0, size = mLyricList.size(); i < size; i++) {
            if (currentPosition < mLyricList.get(i).timePoiont) {
                int tempIndex = i - 1;
                if (currentPosition >= mLyricList.get(tempIndex).timePoiont) {
                    //当前正在播放的哪句歌词
                    mIndex = tempIndex;
                    mSleepTime = mLyricList.get(mIndex).sleepTime;
                    mTimePoint = mLyricList.get(mIndex).timePoiont;
                }
            }
        }
        invalidate();//重新绘制
    }

}
