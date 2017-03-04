package com.zch.mobileplayer.pager;

import android.content.Context;
import android.view.View;

/**
 * 主页面中的四个页面基类
 * Created by zch on 2017/3/3.
 */
public abstract class BasePager {

    public final Context mContext;
    public View mRootView;//子页面实例

    public boolean mIsInitData;

    public BasePager(Context context) {
        this.mContext = context;
        mRootView = initView();
    }

    protected abstract View initView();

    public void initData() {
    }
}
