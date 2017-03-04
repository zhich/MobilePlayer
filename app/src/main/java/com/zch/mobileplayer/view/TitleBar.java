package com.zch.mobileplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * 自定义标题栏
 * Created by zch on 2017/3/3.
 */

public class TitleBar extends LinearLayout {

    private Context mContext;

    public TitleBar(Context context) {
        this(context, null);
    }

    public TitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }
}
