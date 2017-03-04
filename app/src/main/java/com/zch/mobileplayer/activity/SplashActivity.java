/*
 * SplashActivity      2017-03-03
 * Copyright (c) 2017 jufuns. All right reserved.
 *
 */
package com.zch.mobileplayer.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;

import com.zch.mobileplayer.R;

/**
 * 启动页
 *
 * @author zch
 * @version 1.0.0
 * @since 2017-03-03
 */
public class SplashActivity extends BaseActivity {

    private boolean mIsStartMain;//是否已启动了MainActivity，避免多次启动
    private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startMainActivity();
            }
        }, 2000);

    }

    @Override
    protected void onDestroy() {
        //把所有的消息和回调移除
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        //把所有的消息和回调移除（onDestroy执行不确定，因此这里需执行一遍）
        handler.removeCallbacksAndMessages(null);
        super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        startMainActivity();
        return super.onTouchEvent(event);
    }

    private void startMainActivity() {
        if (!mIsStartMain) {
            mIsStartMain = true;
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

}